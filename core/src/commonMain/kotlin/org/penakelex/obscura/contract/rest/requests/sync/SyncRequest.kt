package org.penakelex.obscura.contract.rest.requests.sync

import kotlinx.serialization.Serializable

@Serializable
data class SyncRequest(
    val lastSyncTimestamp: Long,
    val changes: List<NoteChange> = emptyList()
)