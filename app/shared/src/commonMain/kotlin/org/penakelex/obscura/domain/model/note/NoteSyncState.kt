package org.penakelex.obscura.domain.model.note

import org.penakelex.obscura.domain.model.common.SyncStatus

data class NoteSyncState(
    val id: String,
    val syncStatus: SyncStatus,
    val updatedAt: Long,
    val isDeleted: Boolean,
)