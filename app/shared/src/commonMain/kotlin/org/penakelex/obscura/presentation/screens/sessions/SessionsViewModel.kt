package org.penakelex.obscura.presentation.screens.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.penakelex.obscura.domain.exception.AuthException
import org.penakelex.obscura.domain.usecase.auth.session.ListSessionsUseCase
import org.penakelex.obscura.domain.usecase.auth.session.RevokeSessionUseCase
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.UiMessageMapper

class SessionsViewModel(
    private val listSessionsUseCase: ListSessionsUseCase,
    private val revokeSessionUseCase: RevokeSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionsUiState())
    val uiState: StateFlow<SessionsUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    init {
        loadSessions()
    }

    private fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sessions = listSessionsUseCase()
                _uiState.update {
                    it.copy(
                        sessions = sessions,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            } catch (e: AuthException) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false)
                }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, isRefreshing = false)
                }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
            }
        }
    }

    fun onRefresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadSessions()
    }

    fun onRevokeSessionClick(sessionId: String) {
        val session = _uiState.value.sessions.find { it.id == sessionId }
        if (session?.isCurrent == true) {
            viewModelScope.launch {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Warning.PasswordRecoveryUnavailable,
                    )
                )
            }
            return
        }
        _uiState.update { it.copy(pendingRevokeSessionId = sessionId) }
    }

    fun onRevokeConfirmed() {
        val sessionId = _uiState.value.pendingRevokeSessionId ?: return
        viewModelScope.launch {
            try {
                revokeSessionUseCase(sessionId)
                _uiState.update { state ->
                    state.copy(
                        sessions = state.sessions.filter { it.id != sessionId },
                        pendingRevokeSessionId = null,
                    )
                }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.SessionRevoked,
                    )
                )
            } catch (e: AuthException) {
                _uiState.update { it.copy(pendingRevokeSessionId = null) }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(pendingRevokeSessionId = null) }
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
            }
        }
    }

    fun onRevokeDismissed() {
        _uiState.update { it.copy(pendingRevokeSessionId = null) }
    }
}