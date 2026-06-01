package org.penakelex.obscura.domain.usecase.note

import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.domain.repository.NoteRepository

class GetNoteUseCase(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(id: String): Note {
        require(id.isNotBlank()) { "Note ID must not be blank" }
        return noteRepository.getById(id)
    }
}