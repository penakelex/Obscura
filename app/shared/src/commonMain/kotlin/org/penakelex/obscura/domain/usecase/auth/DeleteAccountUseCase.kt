package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.ValidationError

class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(currentPassword: String) {
        if (currentPassword.isBlank()) {
            throw ValidationException(
                ValidationError.CurrentPasswordBlank()
            )
        }

        authRepository.deleteAccount(currentPassword)
    }
}