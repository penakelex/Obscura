package org.penakelex.obscura.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.settings.ThemeMode
import org.penakelex.obscura.domain.usecase.auth.session.LogoutAllUseCase
import org.penakelex.obscura.domain.usecase.auth.session.LogoutUseCase
import org.penakelex.obscura.domain.usecase.auth.session.ObserveSessionUseCase
import org.penakelex.obscura.domain.usecase.settings.GetSettingsUseCase
import org.penakelex.obscura.domain.usecase.settings.SetDefaultCipherTypeUseCase
import org.penakelex.obscura.domain.usecase.settings.SetThemeModeUseCase
import org.penakelex.obscura.domain.usecase.settings.ToggleAutoSyncUseCase
import org.penakelex.obscura.domain.usecase.sync.SyncNotesRestUseCase
import org.penakelex.obscura.presentation.navigation.NavRoute
import org.penakelex.obscura.presentation.navigation.Navigator
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.UiMessageMapper

class SettingsViewModel(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setDefaultCipherTypeUseCase: SetDefaultCipherTypeUseCase,
    private val toggleAutoSyncUseCase: ToggleAutoSyncUseCase,
    private val syncNotesRestUseCase: SyncNotesRestUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val logoutAllUseCase: LogoutAllUseCase,
    private val observeSession: ObserveSessionUseCase,
    private val navigator: Navigator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    private var syncJob: Job? = null

    init {
        observeSettings()
        observeAuthState()
    }

    private fun observeSettings() {
        getSettingsUseCase.observe()
            .onEach { settings ->
                _uiState.update {
                    it.copy(
                        themeMode = settings.themeMode,
                        defaultCipherType = settings.defaultCipherType,
                        isAutoSyncEnabled = settings.isAutoSyncEnabled,
                        lastSyncTimestamp = settings.lastSyncTimestamp,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeAuthState() {
        observeSession()
            .onEach { sessionState ->
                _uiState.update {
                    it.copy(
                        isAuthenticated =
                            sessionState is SessionState.Authenticated,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { setThemeModeUseCase(mode) }
    }

    fun onCipherSelected(cipher: CipherType) {
        viewModelScope.launch { setDefaultCipherTypeUseCase(cipher) }
    }

    fun onAutoSyncToggled(enabled: Boolean) {
        viewModelScope.launch { toggleAutoSyncUseCase(enabled) }
    }

    fun onSyncNowClick() {
        if (_uiState.value.isSyncing) return
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            try {
                val success = syncNotesRestUseCase()
                if (success) {
                    _events.send(
                        UiEvent.ShowSnackbar(
                            message = UiMessage.Success.SyncSuccessful,
                        )
                    )
                }
            } catch (_: SyncException.Unauthenticated) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.SyncUnauthenticated,
                    )
                )
            } catch (e: SyncException) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            } finally {
                _uiState.update { it.copy(isSyncing = false) }
            }
        }
    }

    fun onAccountClick() {
        val sessionState = observeSession().value
        if (sessionState is SessionState.Authenticated) {
            navigator.navigate(NavRoute.Main.Account)
        } else {
            navigator.navigate(NavRoute.Auth.Login)
        }
    }

    fun onSessionsClick() {
        val sessionState = observeSession().value
        if (sessionState is SessionState.Authenticated) {
            navigator.navigate(NavRoute.Main.Sessions)
        } else {
            navigator.navigate(NavRoute.Auth.Login)
        }
    }

    fun onLoginClick() {
        navigator.navigate(NavRoute.Auth.Login)
    }

    fun onRegisterClick() {
        navigator.navigate(NavRoute.Auth.Register)
    }

    fun onLogoutConfirmed() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.LogoutSuccessful,
                    )
                )
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
            }
        }
    }

    fun onLogoutAllConfirmed() {
        viewModelScope.launch {
            try {
                logoutAllUseCase()
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.LogoutSuccessful,
                    )
                )
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
            }
        }
    }

    override fun onCleared() {
        syncJob?.cancel()
    }
}