package org.penakelex.obscura.data.repository

import org.penakelex.obscura.domain.Note

data class NotesResult(
    val notes: List<Note>,
    val corruptedNoteIds: List<String>
) {
    val hasCorruptedNotes: Boolean
        get() = corruptedNoteIds.isNotEmpty()
}