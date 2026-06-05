package org.penakelex.obscura.presentation.screens.sessions

import org.penakelex.obscura.domain.model.auth.SessionInfo

data class SessionsUiState(
    val sessions: List<SessionInfo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val pendingRevokeSessionId: String? = null,
)