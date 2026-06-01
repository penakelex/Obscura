package org.penakelex.obscura.domain.usecase.auth

import org.penakelex.obscura.domain.model.auth.UserProfile
import org.penakelex.obscura.domain.repository.AuthRepository

class GetProfileUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): UserProfile =
        authRepository.getProfile()
}