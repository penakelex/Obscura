package org.penakelex.obscura.contract.rest.responses.notes

import kotlinx.serialization.Serializable

@Serializable
data class NoteResponse(
    val id: String,
    val encryptedData: String,
    val cipherType: Int,
    val updatedAt: Long,
    val isDeleted: Boolean
)