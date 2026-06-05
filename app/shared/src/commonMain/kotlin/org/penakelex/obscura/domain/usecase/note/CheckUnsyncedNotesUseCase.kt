package org.penakelex.obscura.domain.usecase.note

import org.penakelex.obscura.domain.repository.NoteRepository

class CheckUnsyncedNotesUseCase(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(): Int =
        noteRepository.getPendingCount()
}