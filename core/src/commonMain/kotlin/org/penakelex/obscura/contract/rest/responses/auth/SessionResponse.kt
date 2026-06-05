package org.penakelex.obscura.contract.rest.responses.auth

import kotlinx.serialization.Serializable
import org.penakelex.obscura.contract.rest.common.auth.KeysetData

@Serializable
data class SessionResponse(
    val token: String,
    val expiresAt: Long,
    val userId: String,
    val keyset: KeysetData? = null,
)