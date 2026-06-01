package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.repository.AuthRepository

class LogoutAllUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Int = authRepository.logoutAll()
}