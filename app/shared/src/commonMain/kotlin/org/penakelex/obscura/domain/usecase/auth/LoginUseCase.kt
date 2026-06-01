package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.validation.InputValidator

class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        deviceInfo: String? = null
    ) {
        val errors = InputValidator.validateEmail(email) +
                InputValidator.validatePassword(password)

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        authRepository.login(
            email = email.trim().lowercase(),
            password = password,
            deviceInfo = deviceInfo
        )
    }
}