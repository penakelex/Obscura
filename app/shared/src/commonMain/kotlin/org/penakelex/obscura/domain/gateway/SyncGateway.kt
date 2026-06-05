package org.penakelex.obscura.domain.gateway

import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.model.sync.SyncResult

interface SyncGateway {
    suspend fun sync(
        localChanges: List<SyncableNote>,
        lastSyncTimestamp: Long,
    ): SyncResult

    suspend fun resetConnection()
}