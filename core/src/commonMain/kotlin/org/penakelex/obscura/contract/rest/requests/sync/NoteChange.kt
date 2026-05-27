package org.penakelex.obscura.contract.rest.requests.sync

import kotlinx.serialization.Serializable

@Serializable
data class NoteChange(
    val id: String,
    val encryptedData: String,
    val cipherType: Int,
    val updatedAt: Long,
    val isDeleted: Boolean
)