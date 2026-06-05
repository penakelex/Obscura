package org.penakelex.obscura.domain.usecase.auth.session

import org.penakelex.obscura.domain.model.auth.SessionInfo
import org.penakelex.obscura.domain.repository.AuthRepository

class ListSessionsUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): List<SessionInfo> =
        authRepository.listSessions()
}