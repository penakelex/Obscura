package org.penakelex.obscura.domain.usecase.note

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.model.note.NotesResult
import org.penakelex.obscura.domain.repository.NoteRepository

class ObserveNotesUseCase(
    private val noteRepository: NoteRepository,
) {
    operator fun invoke(): Flow<NotesResult> =
        noteRepository.observeNotes()
}