package org.penakelex.obscura.contract.rest.requests.account

import kotlinx.serialization.Serializable

@Serializable
data class DeleteAccountRequest(
    val currentPassword: String
)