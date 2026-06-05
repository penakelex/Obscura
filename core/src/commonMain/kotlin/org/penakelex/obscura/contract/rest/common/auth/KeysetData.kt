package org.penakelex.obscura.contract.rest.common.auth

import kotlinx.serialization.Serializable

@Serializable
data class KeysetData(
    val salt: String,
    val encryptedKeyset: String
)