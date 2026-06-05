package org.penakelex.obscura.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.penakelex.obscura.contract.rest.common.auth.KeysetData
import org.penakelex.obscura.contract.rest.requests.account.ChangeEmailRequest
import org.penakelex.obscura.contract.rest.requests.account.ChangePasswordRequest
import org.penakelex.obscura.contract.rest.requests.account.DeleteAccountRequest
import org.penakelex.obscura.contract.rest.requests.auth.AuthRequest
import org.penakelex.obscura.contract.rest.requests.auth.ChallengeRequest
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.mapper.AuthMapper.toDomain
import org.penakelex.obscura.data.mapper.AuthMapper.toDomainList
import org.penakelex.obscura.data.mapper.AuthMapper.toSessionData
import org.penakelex.obscura.data.remote.http.ApiException
import org.penakelex.obscura.data.remote.http.AuthApiClient
import org.penakelex.obscura.data.storage.TokenStorage
import org.penakelex.obscura.domain.exception.AuthException
import org.penakelex.obscura.domain.model.auth.SessionInfo
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.model.auth.UserProfile
import org.penakelex.obscura.domain.repository.AuthRepository
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AuthRepositoryImpl(
    private val authApiClient: AuthApiClient,
    private val tokenStorage: TokenStorage,
    private val cryptoProvider: CryptoProvider,
) : AuthRepository {
    private val logger = Logger.withTag(LOG_TAG)
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _sessionState =
        MutableStateFlow<SessionState>(SessionState.Loading)
    override val sessionState: StateFlow<SessionState> =
        _sessionState.asStateFlow()

    init {
        tokenStorage.sessionFlow.onEach { sessionData ->
            _sessionState.value = when {
                sessionData == null -> SessionState.Unauthenticated
                sessionData.isExpired(
                    Clock.System.now().toEpochMilliseconds()
                ) -> {
                    logger.w {
                        "Session expired for user: ${sessionData.userId}"
                    }
                    safeClearToken()
                    SessionState.Unauthenticated
                }

                else -> SessionState.Authenticated(
                    userId = sessionData.userId,
                    expiresAt = sessionData.expiresAt
                )
            }
        }.launchIn(scope)
    }

    override fun isLoggedIn(): Boolean =
        _sessionState.value is SessionState.Authenticated

    override suspend fun getChallenge(email: String): String {
        return try {
            authApiClient.challenge(ChallengeRequest(email)).salt
        } catch (e: ApiException) {
            throw mapApiException(e)
        }
    }

    override suspend fun getCurrentKeyset(): KeysetData? {
        val session = tokenStorage.sessionFlow.value
        val encryptedKeyset = session?.encryptedKeyset
        val salt = session?.salt
        return if (encryptedKeyset != null && salt != null) {
            KeysetData(
                salt = salt,
                encryptedKeyset = encryptedKeyset,
            )
        } else {
            null
        }
    }

    override suspend fun register(
        email: String,
        authHash: String,
        deviceInfo: String?,
        keyset: KeysetData,
    ) {
        val response = authApiClient.register(
            AuthRequest(
                email = email,
                authHash = authHash,
                deviceInfo = deviceInfo,
                keyset = keyset,
            )
        )
        tokenStorage.save(response.toSessionData())
        logger.i { "User registered: $email" }
    }

    override suspend fun login(
        email: String,
        authHash: String,
        deviceInfo: String?,
    ): KeysetData {
        return try {
            val response = authApiClient.login(
                AuthRequest(
                    email = email,
                    authHash = authHash,
                    deviceInfo = deviceInfo,
                )
            )
            tokenStorage.save(response.toSessionData())
            logger.i { "User logged in: $email" }
            response.keyset ?: throw AuthException.KeysetNotFound()
        } catch (e: ApiException) {
            throw mapApiException(e)
        }
    }

    override suspend fun logout() {
        val token = currentTokenOrNull()
        if (token != null) {
            try {
                authApiClient.logout(token)
                logger.i { "Logged out from server" }
            } catch (e: ApiException) {
                logger.w(e) {
                    "Server logout failed, clearing token locally"
                }
            }
        }
        safeClearToken()
    }

    override suspend fun logoutAll(): Int {
        val token = requireToken()
        return try {
            val response = authApiClient.logoutAll(token)
            safeClearToken()
            logger.i {
                "Logged out from all devices: ${response.revokedCount}"
            }
            response.revokedCount
        } catch (e: ApiException) {
            if (e is ApiException.Unauthorized) {
                safeClearToken()
            }
            throw mapApiException(e)
        }
    }

    override suspend fun getProfile(): UserProfile {
        val token = requireToken()
        return try {
            authApiClient.getProfile(token).toDomain()
        } catch (e: ApiException) {
            handleUnauthorized(e)
            throw mapApiException(e)
        }
    }

    override suspend fun changePassword(
        currentAuthHash: String,
        newAuthHash: String,
        newKeyset: KeysetData,
    ) {
        val token = requireToken()
        try {
            authApiClient.changePassword(
                token = token,
                request = ChangePasswordRequest(
                    currentAuthHash = currentAuthHash,
                    newAuthHash = newAuthHash,
                    newKeyset = newKeyset,
                )
            )
            safeClearToken()
            logger.i { "Password changed, all sessions revoked" }
        } catch (e: ApiException) {
            handleUnauthorized(e)
            throw mapApiException(e)
        }
    }

    override suspend fun changeEmail(
        currentAuthHash: String,
        newEmail: String,
    ) {
        val token = requireToken()
        try {
            authApiClient.changeEmail(
                token = token,
                request = ChangeEmailRequest(
                    currentAuthHash = currentAuthHash,
                    newEmail = newEmail,
                )
            )
            logger.i { "Email changed to: $newEmail" }
        } catch (e: ApiException) {
            handleUnauthorized(e)
            throw mapApiException(e)
        }
    }

    override suspend fun deleteAccount(currentAuthHash: String) {
        val token = requireToken()
        try {
            authApiClient.deleteAccount(
                token = token,
                request = DeleteAccountRequest(
                    currentAuthHash = currentAuthHash,
                )
            )
            safeClearToken()
            logger.i { "Account deleted" }
        } catch (e: ApiException) {
            handleUnauthorized(e)
            throw mapApiException(e)
        }
    }

    override suspend fun listSessions(): List<SessionInfo> {
        val token = requireToken()
        return try {
            authApiClient.listSessions(token).sessions.toDomainList()
        } catch (e: ApiException) {
            handleUnauthorized(e)
            throw mapApiException(e)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun revokeSession(sessionId: String) {
        val token = requireToken()

        val currentSessionId = findCurrentSessionId(token)
        if (currentSessionId != null && currentSessionId == sessionId) {
            throw IllegalArgumentException(
                "Cannot revoke current session via revokeSession(). Use logout() instead."
            )
        }

        val uuid = Uuid.parse(sessionId)
        try {
            authApiClient.revokeSession(token, uuid)
            logger.i { "Session revoked: $sessionId" }
        } catch (e: ApiException) {
            handleUnauthorized(e)
            throw mapApiException(e)
        }
    }

    private fun currentTokenOrNull(): String? =
        tokenStorage.sessionFlow.value?.token

    private fun requireToken(): String {
        val session = tokenStorage.sessionFlow.value
        return when {
            session == null -> throw AuthException.SessionNotFound()
            session.isExpired(
                Clock.System.now().toEpochMilliseconds()
            ) -> throw AuthException.SessionExpired()

            else -> session.token
        }
    }

    private suspend fun safeClearToken() {
        try {
            cryptoProvider.reset()
            tokenStorage.clear()
        } catch (e: Exception) {
            logger.e(e) { "Failed to clear session" }
        }
    }

    private suspend fun handleUnauthorized(e: ApiException) {
        if (e is ApiException.Unauthorized) {
            logger.w { "Unauthorized response — auto-logout" }
            safeClearToken()
        }
    }

    private suspend fun findCurrentSessionId(token: String): String? =
        try {
            authApiClient.listSessions(token).sessions
                .firstOrNull { it.isCurrent }
                ?.id
        } catch (e: Exception) {
            logger.w(e) { "Failed to find current session ID" }
            null
        }

    private fun mapApiException(e: ApiException): AuthException =
        when (e) {
            is ApiException.Unauthorized -> AuthException.SessionNotFound()
            is ApiException.BadRequest -> {
                val code = e.errorResponse.code
                when {
                    code?.contains("INVALID_CREDENTIALS") == true ->
                        AuthException.InvalidCredentials()

                    code?.contains("EMAIL_ALREADY_REGISTERED") == true ->
                        AuthException.EmailAlreadyRegistered(
                            e.errorResponse.details?.firstOrNull()?.field
                                ?: "unknown"
                        )

                    else -> AuthException.ServerError(
                        400,
                        e.message ?: "Bad request"
                    )
                }
            }

            is ApiException.Conflict ->
                AuthException.EmailAlreadyRegistered("unknown")

            is ApiException.Network ->
                AuthException.NetworkError(e.cause ?: e)

            is ApiException.Server -> AuthException.ServerError(
                e.statusCode, e.message ?: "Server error"
            )

            is ApiException.NotFound -> AuthException.SessionNotFound()
            is ApiException.Unknown ->
                AuthException.NetworkError(e.cause ?: e)
        }

    private companion object {
        const val LOG_TAG = "AuthRepository"
    }
}