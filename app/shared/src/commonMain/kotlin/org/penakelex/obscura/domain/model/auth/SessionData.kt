package org.penakelex.obscura.domain.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class SessionData(
    val token: String,
    val userId: String,
    val expiresAt: Long
) {
    fun isExpired(currentTimeMillis: Long): Boolean =
        currentTimeMillis >= expiresAt
}