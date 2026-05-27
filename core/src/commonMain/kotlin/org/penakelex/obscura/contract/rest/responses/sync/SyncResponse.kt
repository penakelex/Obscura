package org.penakelex.obscura.contract.rest.responses.sync

import kotlinx.serialization.Serializable
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse

@Serializable
data class SyncResponse(
    val serverChanges: List<NoteResponse>,
    val newSyncTimestamp: Long,
    val appliedCount: Int,
    val conflictsResolved: Int
)