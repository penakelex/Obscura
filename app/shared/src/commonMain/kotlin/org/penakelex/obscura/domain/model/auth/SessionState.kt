package org.penakelex.obscura.domain.model.auth

sealed interface SessionState {
    data object Loading : SessionState

    data object Unauthenticated : SessionState

    data class Authenticated(
        val userId: String,
        val expiresAt: Long
    ) : SessionState
}