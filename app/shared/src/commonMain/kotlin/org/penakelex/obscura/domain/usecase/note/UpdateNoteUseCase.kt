package org.penakelex.obscura.domain.usecase.note

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.repository.NoteRepository
import org.penakelex.obscura.domain.validation.InputValidator

class UpdateNoteUseCase(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(
        id: String,
        content: String,
        cipherType: CipherType
    ) {
        require(id.isNotBlank()) { "Note ID must not be blank" }

        val errors = InputValidator.validateNoteContent(content)
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        noteRepository.update(
            id = id,
            content = content,
            cipherType = cipherType
        )
    }
}