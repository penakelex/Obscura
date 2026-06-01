package org.penakelex.obscura.domain.usecase.note

import org.penakelex.obscura.domain.repository.NoteRepository

class DeleteNoteUseCase(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(id: String) {
        require(id.isNotBlank()) { "Note ID must not be blank" }
        noteRepository.delete(id)
    }
}