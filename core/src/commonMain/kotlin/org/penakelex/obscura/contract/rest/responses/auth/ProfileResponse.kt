package org.penakelex.obscura.contract.rest.responses.auth

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val userId: String,
    val email: String
)