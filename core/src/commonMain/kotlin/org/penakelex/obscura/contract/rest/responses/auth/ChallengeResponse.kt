package org.penakelex.obscura.contract.rest.responses.auth

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResponse(
    val salt: String
)