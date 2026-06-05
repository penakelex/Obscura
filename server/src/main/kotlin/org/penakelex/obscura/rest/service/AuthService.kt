package org.penakelex.obscura.rest.service

import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.common.auth.KeysetData
import org.penakelex.obscura.contract.rest.requests.account.ChangeEmailRequest
import org.penakelex.obscura.contract.rest.requests.account.ChangePasswordRequest
import org.penakelex.obscura.contract.rest.requests.account.DeleteAccountRequest
import org.penakelex.obscura.contract.rest.requests.auth.AuthRequest
import org.penakelex.obscura.contract.rest.responses.auth.ChallengeResponse
import org.penakelex.obscura.contract.rest.responses.auth.ProfileResponse
import org.penakelex.obscura.contract.rest.responses.auth.SessionInfo
import org.penakelex.obscura.contract.rest.responses.auth.SessionResponse
import org.penakelex.obscura.contract.rest.responses.auth.SessionsListResponse
import org.penakelex.obscura.contract.rest.responses.common.FieldError
import org.penakelex.obscura.contract.rest.responses.common.SuccessResponse
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.db.repository.UserKeysetRepository
import org.penakelex.obscura.db.repository.UserRepository
import org.penakelex.obscura.exception.account.AccountException
import org.penakelex.obscura.exception.auth.AuthException
import org.penakelex.obscura.exception.resource.NotFoundException
import org.penakelex.obscura.exception.validation.ValidationException
import org.penakelex.obscura.security.PasswordHasher
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class AuthService(
    private val userRepository: UserRepository,
    private val sessionRepository: SessionRepository,
    private val noteRepository: NoteRepository,
    private val userKeysetRepository: UserKeysetRepository,
    private val passwordHasher: PasswordHasher,
    private val validationConfig: ServerConfig.Validation
) {
    suspend fun getChallenge(email: String): ChallengeResponse {
        validateEmail(email)
        val normalizedEmail = email.trim().lowercase()

        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw NotFoundException.UserNotFound(normalizedEmail)

        val keyset = userKeysetRepository.findByUserId(user.id)
            ?: throw NotFoundException.KeysetNotFound(normalizedEmail)

        return ChallengeResponse(
            salt = Base64.encode(keyset.salt)
        )
    }

    suspend fun register(request: AuthRequest): SessionResponse {
        validateRegisterInput(
            request.email,
            request.authHash,
            request.deviceInfo
        )

        val normalizedEmail = request.email.trim().lowercase()
        val keyset = request.keyset
            ?: throw ValidationException.MultipleFields(
                listOf(
                    FieldError(
                        "keyset",
                        ErrorCodes.Validation.MULTIPLE_FIELDS_INVALID,
                        "Keyset data is required for registration"
                    )
                )
            )

        val hash = passwordHasher.hash(request.authHash)
        val userId = userRepository.create(normalizedEmail, hash)
            ?: throw AuthException.EmailAlreadyRegistered(normalizedEmail)

        userKeysetRepository.upsertKeyset(
            userId = userId,
            salt = Base64.decode(keyset.salt),
            encryptedKeyset = Base64.decode(keyset.encryptedKeyset),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )

        val sessionResult =
            sessionRepository.create(userId, request.deviceInfo)

        return SessionResponse(
            token = sessionResult.rawToken,
            expiresAt = sessionResult.expiresAt.toEpochMilliseconds(),
            userId = userId.toString(),
            keyset = keyset
        )
    }

    suspend fun login(request: AuthRequest): SessionResponse {
        validateLoginInput(
            request.email,
            request.authHash,
            request.deviceInfo
        )

        val normalizedEmail = request.email.trim().lowercase()

        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw AuthException.InvalidCredentials()

        if (!passwordHasher.verify(request.authHash, user.passwordHash)) {
            throw AuthException.InvalidCredentials()
        }

        val storedKeyset = userKeysetRepository.findByUserId(user.id)
            ?: throw NotFoundException.KeysetNotFound(normalizedEmail)

        val sessionResult =
            sessionRepository.create(user.id, request.deviceInfo)

        return SessionResponse(
            token = sessionResult.rawToken,
            expiresAt = sessionResult.expiresAt.toEpochMilliseconds(),
            userId = user.id.toString(),
            keyset = KeysetData(
                salt = Base64.encode(storedKeyset.salt),
                encryptedKeyset = Base64.encode(storedKeyset.encryptedKeyset)
            )
        )
    }

    suspend fun listSessions(
        userId: Uuid,
        currentSessionId: Uuid
    ): SessionsListResponse {
        val sessions = sessionRepository.findAllActiveByUserId(userId)
            .filter { it.expiresAt > Clock.System.now() }
            .map { session ->
                SessionInfo(
                    id = session.id.toString(),
                    deviceInfo = session.deviceInfo,
                    createdAt = session.createdAt.toEpochMilliseconds(),
                    expiresAt = session.expiresAt.toEpochMilliseconds(),
                    isCurrent = session.id == currentSessionId
                )
            }
            .sortedByDescending { it.createdAt }
        return SessionsListResponse(
            sessions = sessions,
            totalCount = sessions.size
        )
    }

    suspend fun revokeSessionById(
        userId: Uuid,
        sessionId: Uuid,
        currentSessionId: Uuid
    ): SuccessResponse {
        if (sessionId == currentSessionId) {
            throw ValidationException.CannotRevokeCurrentSession()
        }
        val session = sessionRepository
            .findActiveByIdAndUser(sessionId, userId)
            ?: throw NotFoundException.SessionNotFound(sessionId.toString())
        sessionRepository.revoke(session.id)
        return SuccessResponse("Session revoked")
    }

    suspend fun logout(sessionId: Uuid): SuccessResponse {
        sessionRepository.revoke(sessionId)
        return SuccessResponse("Session revoked")
    }

    suspend fun logoutAllSessions(userId: Uuid): Int =
        sessionRepository.revokeAllByUserId(userId)

    suspend fun getProfile(userId: Uuid): ProfileResponse {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())
        return ProfileResponse(
            userId = userId.toString(),
            email = user.email
        )
    }

    suspend fun changePassword(
        userId: Uuid,
        request: ChangePasswordRequest
    ): SuccessResponse {
        validateAuthHash(request.currentAuthHash)
        validateAuthHash(request.newAuthHash)

        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!passwordHasher.verify(
                request.currentAuthHash,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        if (request.newAuthHash == request.currentAuthHash) {
            throw AccountException.PasswordSameAsCurrent()
        }

        val newHash = passwordHasher.hash(request.newAuthHash)
        userRepository.updatePassword(userId, newHash)

        userKeysetRepository.upsertKeyset(
            userId = userId,
            salt = Base64.decode(request.newKeyset.salt),
            encryptedKeyset = Base64.decode(
                request.newKeyset.encryptedKeyset
            ),
            updatedAt = Clock.System.now().toEpochMilliseconds()
        )

        sessionRepository.revokeAllByUserId(userId)

        return SuccessResponse("Password changed successfully")
    }

    suspend fun deleteAccount(
        userId: Uuid,
        request: DeleteAccountRequest
    ): SuccessResponse {
        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!passwordHasher.verify(
                request.currentAuthHash,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        sessionRepository.deleteAllByUserId(userId)
        noteRepository.deleteAllByUserId(userId)
        userKeysetRepository.deleteByUserId(userId)
        userRepository.delete(userId)

        return SuccessResponse(
            "Account and all associated data deleted"
        )
    }

    suspend fun changeEmail(
        userId: Uuid,
        request: ChangeEmailRequest
    ): SuccessResponse {
        validateEmail(request.newEmail)
        validateAuthHash(request.currentAuthHash)

        val normalizedNewEmail = request.newEmail.trim().lowercase()

        val user = userRepository.findById(userId)
            ?: throw NotFoundException.UserNotFound(userId.toString())

        if (!passwordHasher.verify(
                request.currentAuthHash,
                user.passwordHash
            )
        ) {
            throw AccountException.InvalidCurrentPassword()
        }

        if (normalizedNewEmail == user.email.lowercase()) {
            return SuccessResponse("Email unchanged")
        }

        val existingUser =
            userRepository.findByEmail(normalizedNewEmail)
        if (existingUser != null) {
            throw AccountException.NewEmailAlreadyTaken(
                normalizedNewEmail
            )
        }

        userRepository.updateEmail(userId, normalizedNewEmail)

        return SuccessResponse("Email changed successfully")
    }

    private fun validateRegisterInput(
        email: String,
        authHash: String,
        deviceInfo: String?
    ) {
        val errors = mutableListOf<FieldError>()
        when {
            email.isBlank() -> errors += FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_BLANK,
                "Email is required"
            )
            email.length > validationConfig.emailMaxLength ->
                errors += FieldError(
                    "email",
                    ErrorCodes.Validation.EMAIL_TOO_LONG,
                    "Max ${validationConfig.emailMaxLength} characters"
                )
            !EMAIL_REGEX.matches(email) -> errors += FieldError(
                "email",
                ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
                "Invalid format"
            )
        }
        when {
            authHash.isBlank() -> errors += FieldError(
                "authHash",
                ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                "Auth hash is required"
            )
            authHash.length < validationConfig.passwordMinLength ->
                errors += FieldError(
                    "authHash",
                    ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                    "Min ${validationConfig.passwordMinLength} characters"
                )
            authHash.length > validationConfig.passwordMaxLength ->
                errors += FieldError(
                    "authHash",
                    ErrorCodes.Validation.PASSWORD_TOO_LONG,
                    "Max ${validationConfig.passwordMaxLength} characters"
                )
        }
        if (deviceInfo != null &&
            deviceInfo.length > validationConfig.deviceInfoMaxLength
        ) {
            errors += FieldError(
                "deviceInfo",
                ErrorCodes.Validation.DEVICE_INFO_TOO_LONG,
                "Max ${validationConfig.deviceInfoMaxLength} characters"
            )
        }
        if (errors.isNotEmpty()) {
            throw ValidationException.MultipleFields(errors)
        }
    }

    private fun validateLoginInput(
        email: String,
        authHash: String,
        deviceInfo: String?
    ) {
        val errors = mutableListOf<FieldError>()
        if (email.isBlank()) {
            errors += FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_BLANK,
                "Email is required"
            )
        } else if (!EMAIL_REGEX.matches(email)) {
            errors += FieldError(
                "email",
                ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
                "Invalid format"
            )
        }
        if (authHash.isBlank()) {
            errors += FieldError(
                "authHash",
                ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                "Auth hash is required"
            )
        } else if (authHash.length < validationConfig.passwordMinLength) {
            errors += FieldError(
                "authHash",
                ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                "Min ${validationConfig.passwordMinLength} characters"
            )
        }
        if (deviceInfo != null && deviceInfo.length >
            validationConfig.deviceInfoMaxLength
        ) {
            errors += FieldError(
                "deviceInfo",
                ErrorCodes.Validation.DEVICE_INFO_TOO_LONG,
                "Max ${validationConfig.deviceInfoMaxLength} characters"
            )
        }
        if (errors.isNotEmpty()) {
            throw ValidationException.MultipleFields(errors)
        }
    }

    private fun validateEmail(email: String) {
        when {
            email.isBlank() -> throw ValidationException.EmailBlank()
            email.length > validationConfig.emailMaxLength ->
                throw ValidationException.EmailTooLong(
                    validationConfig.emailMaxLength
                )
            !EMAIL_REGEX.matches(email) ->
                throw ValidationException.InvalidEmailFormat(email)
        }
    }

    private fun validateAuthHash(authHash: String) {
        when {
            authHash.isBlank() ->
                throw ValidationException.PasswordTooShort(
                    validationConfig.passwordMinLength
                )
            authHash.length < validationConfig.passwordMinLength ->
                throw ValidationException.PasswordTooShort(
                    validationConfig.passwordMinLength
                )
            authHash.length > validationConfig.passwordMaxLength ->
                throw ValidationException.PasswordTooLong(
                    validationConfig.passwordMaxLength
                )
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}