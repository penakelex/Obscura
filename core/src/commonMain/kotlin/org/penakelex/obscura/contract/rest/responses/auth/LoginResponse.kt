package org.penakelex.obscura.contract.rest.responses.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val expiresAt: String,
    val userId: String
)