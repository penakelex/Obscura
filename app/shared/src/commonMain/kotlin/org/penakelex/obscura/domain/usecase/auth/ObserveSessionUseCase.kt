package org.penakelex.obscura.domain.usecase.auth

import kotlinx.coroutines.flow.StateFlow
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.repository.AuthRepository

class ObserveSessionUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke(): StateFlow<SessionState> =
        authRepository.sessionState
}