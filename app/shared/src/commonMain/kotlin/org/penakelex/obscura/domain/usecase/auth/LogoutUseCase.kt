package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.repository.AuthRepository

class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() {
        authRepository.logout()
    }
}