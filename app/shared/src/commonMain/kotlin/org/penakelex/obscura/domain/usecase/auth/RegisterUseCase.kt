package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator

class RegisterUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String) {
        val errors = InputValidator.validateEmail(email) +
                InputValidator.validatePassword(password)

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        authRepository.register(
            email = email.trim().lowercase(),
            password = password
        )
    }
}