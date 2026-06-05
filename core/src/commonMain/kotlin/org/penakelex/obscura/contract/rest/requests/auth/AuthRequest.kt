package org.penakelex.obscura.contract.rest.requests.auth

import kotlinx.serialization.Serializable
import org.penakelex.obscura.contract.rest.common.auth.KeysetData

@Serializable
data class AuthRequest(
    val email: String,
    val authHash: String,
    val deviceInfo: String? = null,
    val keyset: KeysetData? = null,
)