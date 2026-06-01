package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator
import org.penakelex.obscura.domain.validation.ValidationError

class ChangeEmailUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        currentPassword: String,
        newEmail: String
    ) {
        val errors = buildList {
            if (currentPassword.isBlank()) {
                add(ValidationError.CurrentPasswordBlank())
            }

            addAll(InputValidator.validateEmail(newEmail))
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        authRepository.changeEmail(
            currentPassword = currentPassword,
            newEmail = newEmail.trim().lowercase()
        )
    }
}