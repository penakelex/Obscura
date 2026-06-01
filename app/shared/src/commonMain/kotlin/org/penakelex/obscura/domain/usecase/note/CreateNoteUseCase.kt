package org.penakelex.obscura.domain.usecase.note

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.repository.NoteRepository
import org.penakelex.obscura.domain.validation.InputValidator

class CreateNoteUseCase(
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(
        content: String,
        cipherType: CipherType = CipherType.DEFAULT
    ): String {
        val errors = InputValidator.validateNoteContent(content)
        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        return noteRepository.create(
            content = content,
            cipherType = cipherType
        )
    }
}