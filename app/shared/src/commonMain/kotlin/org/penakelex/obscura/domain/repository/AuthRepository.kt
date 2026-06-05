package org.penakelex.obscura.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.penakelex.obscura.contract.rest.common.auth.KeysetData
import org.penakelex.obscura.domain.model.auth.SessionInfo
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.model.auth.UserProfile

interface AuthRepository {
    val sessionState: StateFlow<SessionState>

    fun isLoggedIn(): Boolean

    suspend fun getChallenge(email: String): String
    suspend fun getCurrentKeyset(): KeysetData?

    suspend fun register(
        email: String,
        authHash: String,
        deviceInfo: String?,
        keyset: KeysetData,
    )

    suspend fun login(
        email: String,
        authHash: String,
        deviceInfo: String? = null,
    ): KeysetData

    suspend fun logout()

    suspend fun logoutAll(): Int

    suspend fun getProfile(): UserProfile

    suspend fun changePassword(
        currentAuthHash: String,
        newAuthHash: String,
        newKeyset: KeysetData,
    )

    suspend fun changeEmail(
        currentAuthHash: String,
        newEmail: String,
    )

    suspend fun deleteAccount(currentAuthHash: String)

    suspend fun listSessions(): List<SessionInfo>

    suspend fun revokeSession(sessionId: String)
}