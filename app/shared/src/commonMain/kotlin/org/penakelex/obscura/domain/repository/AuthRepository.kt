package org.penakelex.obscura.domain.repository

import kotlinx.coroutines.flow.StateFlow
import org.penakelex.obscura.domain.model.auth.SessionInfo
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.model.auth.UserProfile

interface AuthRepository {
    val sessionState: StateFlow<SessionState>
    fun isLoggedIn(): Boolean
    suspend fun register(email: String, password: String)
    suspend fun login(
        email: String,
        password: String,
        deviceInfo: String? = null
    )
    suspend fun logout()
    suspend fun logoutAll(): Int
    suspend fun getProfile(): UserProfile
    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    )
    suspend fun changeEmail(
        currentPassword: String,
        newEmail: String
    )
    suspend fun deleteAccount(currentPassword: String)
    suspend fun listSessions(): List<SessionInfo>
    suspend fun revokeSession(sessionId: String)
}