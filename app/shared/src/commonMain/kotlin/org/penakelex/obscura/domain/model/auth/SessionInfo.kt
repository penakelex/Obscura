package org.penakelex.obscura.domain.model.auth

data class SessionInfo(
    val id: String,
    val deviceInfo: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val isCurrent: Boolean
)