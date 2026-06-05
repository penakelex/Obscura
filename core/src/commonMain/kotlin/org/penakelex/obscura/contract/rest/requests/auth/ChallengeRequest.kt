package org.penakelex.obscura.contract.rest.requests.auth

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeRequest(
    val email: String
)