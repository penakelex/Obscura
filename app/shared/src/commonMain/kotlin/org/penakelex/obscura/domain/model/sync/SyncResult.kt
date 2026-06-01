package org.penakelex.obscura.domain.model.sync

import org.penakelex.obscura.domain.model.note.SyncableNote

data class SyncResult(
    val serverChanges: List<SyncableNote>,
    val newSyncTimestamp: Long,
    val status: SyncResultStatus
)