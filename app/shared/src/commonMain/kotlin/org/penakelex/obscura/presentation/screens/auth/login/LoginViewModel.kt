package org.penakelex.obscura.presentation.screens.auth.login

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
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.usecase.auth.authentication.LoginUseCase
import org.penakelex.obscura.presentation.navigation.NavRoute
import org.penakelex.obscura.presentation.navigation.Navigator
import org.penakelex.obscura.presentation.util.error.UiErrorMapper
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessageMapper

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val navigator: Navigator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onDeviceInfoChange(deviceInfo: String) {
        _uiState.update {
            it.copy(deviceInfo = deviceInfo.takeIf { d -> d.isNotBlank() })
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.isLoading || !state.isLoginEnabled) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                loginUseCase(
                    email = state.email,
                    password = state.password,
                    deviceInfo = state.deviceInfo,
                )
            } catch (e: ValidationException) {
                handleValidationErrors(e)
            } catch (e: AuthException) {
                _events.send(
                    UiEvent.ShowSnackbar(message = UiMessageMapper.map(e))
                )
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(message = UiMessageMapper.map(e))
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRegisterClick() {
        navigator.navigate(NavRoute.Auth.Register) {
            launchSingleTop = true
        }
    }

    fun onBackClick() {
        navigator.navigate(NavRoute.Main.NotesList) {
            popUpTo(NavRoute.Auth.Register) { inclusive = true }
            launchSingleTop = true
        }
    }

    private fun handleValidationErrors(e: ValidationException) {
        val emailError = UiErrorMapper.mapForField(e, "email")
        val passwordError = UiErrorMapper.mapForField(e, "password")
        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
            )
        }
    }
}