package org.penakelex.obscura.domain.model.note

data class NotesResult(
    val notes: List<Note>,
    val corruptedNoteIds: List<String>
)