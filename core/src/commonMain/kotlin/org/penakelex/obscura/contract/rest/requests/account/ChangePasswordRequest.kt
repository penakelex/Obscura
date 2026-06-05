package org.penakelex.obscura.contract.rest.requests.account

import kotlinx.serialization.Serializable
import org.penakelex.obscura.contract.rest.common.auth.KeysetData

@Serializable
data class ChangePasswordRequest(
    val currentAuthHash: String,
    val newAuthHash: String,
    val newKeyset: KeysetData,
)