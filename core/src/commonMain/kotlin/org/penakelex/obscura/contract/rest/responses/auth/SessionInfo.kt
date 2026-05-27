package org.penakelex.obscura.contract.rest.responses.auth

import kotlinx.serialization.Serializable

@Serializable
data class SessionInfo(
    val id: String,
    val deviceInfo: String?,
    val createdAt: Long,
    val expiresAt: Long,
    val isCurrent: Boolean
)