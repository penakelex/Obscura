package org.penakelex.obscura.domain.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class SessionData(
    val token: String,
    val userId: String,
    val expiresAt: Long,
    val encryptedKeyset: String? = null,
    val salt: String? = null,
) {
    fun isExpired(currentTimeMillis: Long): Boolean =
        currentTimeMillis >= expiresAt
}