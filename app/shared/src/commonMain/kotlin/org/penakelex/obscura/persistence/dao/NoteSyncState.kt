package org.penakelex.obscura.persistence.dao

import org.penakelex.obscura.persistence.SyncStatus

data class NoteSyncState(
    val id: String,
    val syncStatus: SyncStatus,
    val updatedAt: Long,
    val isDeleted: Boolean,
)