package org.penakelex.obscura.contract.rest.requests.account

import kotlinx.serialization.Serializable

@Serializable
data class ChangeEmailRequest(
    val currentAuthHash: String,
    val newEmail: String,
)