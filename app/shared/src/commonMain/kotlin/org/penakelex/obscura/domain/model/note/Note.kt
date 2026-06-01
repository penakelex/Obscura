package org.penakelex.obscura.domain.model.note

import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus

data class Note(
    val id: String,
    val content: String,
    val cipherType: CipherType,
    val updatedAt: Long,
    val syncStatus: SyncStatus,
    val isDeleted: Boolean
)