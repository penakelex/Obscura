package org.penakelex.obscura.contract.rest.responses.notes

import kotlinx.serialization.Serializable

@Serializable
data class NotesListResponse(
    val notes: List<NoteResponse>,
    val totalCount: Int,
    val limit: Int,
    val offset: Int,
    val hasMore: Boolean
)