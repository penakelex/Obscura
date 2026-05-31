package org.penakelex.obscura.domain

import org.penakelex.obscura.crypto.CipherType
import org.penakelex.obscura.persistence.SyncStatus

data class Note(
    val id: String,
    val content: String,
    val cipherType: CipherType,
    val updatedAt: Long,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean
)