package org.penakelex.obscura.contract.rest.responses.sync

import kotlinx.serialization.Serializable
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse

@Serializable
data class DeltaResponse(
    val notes: List<NoteResponse>,
    val serverTimestamp: Long,
    val sinceTimestamp: Long
)