package org.penakelex.obscura.domain.model.sync

import org.penakelex.obscura.domain.model.note.SyncableNote

data class SyncRequest(
    val localChanges: List<SyncableNote>,
    val lastSyncTimestamp: Long
)
