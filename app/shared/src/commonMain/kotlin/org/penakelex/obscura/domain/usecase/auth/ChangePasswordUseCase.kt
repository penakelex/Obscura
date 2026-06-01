package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator
import org.penakelex.obscura.domain.validation.ValidationError

class ChangePasswordUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String
    ) {
        val errors = buildList {
            if (currentPassword.isBlank()) {
                add(ValidationError.CurrentPasswordBlank())
            }

            addAll(InputValidator.validatePassword(newPassword))

            if (currentPassword.isNotBlank() &&
                newPassword.isNotBlank() &&
                currentPassword == newPassword
            ) {
                add(ValidationError.PasswordsMatch())
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        authRepository.changePassword(
            currentPassword = currentPassword,
            newPassword = newPassword
        )
    }
}