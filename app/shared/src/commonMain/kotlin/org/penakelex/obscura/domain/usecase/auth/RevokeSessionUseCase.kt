package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.repository.AuthRepository

class RevokeSessionUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(sessionId: String) {
        require(sessionId.isNotBlank()) {
            "Session ID must not be blank"
        }
        authRepository.revokeSession(sessionId)
    }
}