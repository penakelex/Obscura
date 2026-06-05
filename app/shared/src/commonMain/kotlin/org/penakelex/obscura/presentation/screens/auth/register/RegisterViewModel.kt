package org.penakelex.obscura.presentation.screens.auth.register

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
import org.penakelex.obscura.domain.usecase.auth.RegisterUseCase
import org.penakelex.obscura.domain.validation.ValidationError
import org.penakelex.obscura.presentation.navigation.NavRoute
import org.penakelex.obscura.presentation.navigation.Navigator
import org.penakelex.obscura.presentation.util.error.UiError
import org.penakelex.obscura.presentation.util.error.UiErrorMapper
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.UiMessageMapper

class RegisterViewModel(
    private val registerUseCase: RegisterUseCase,
    private val navigator: Navigator,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            val confirmError = if (it.confirmPassword.isNotBlank() &&
                it.confirmPassword != password
            ) {
                UiErrorMapper.mapForField(
                    ValidationException(
                        ValidationError.ConfirmPasswordMismatch()
                    ),
                    "confirmPassword"
                )
            } else null
            it.copy(
                password = password,
                passwordError = null,
                confirmPasswordError = confirmError
            )
        }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update {
            val error = if (confirmPassword.isNotBlank() &&
                confirmPassword != it.password
            ) {
                UiErrorMapper.mapForField(
                    ValidationException(
                        ValidationError.ConfirmPasswordMismatch()
                    ),
                    "confirmPassword"
                )
            } else null
            it.copy(
                confirmPassword = confirmPassword,
                confirmPasswordError = error
            )
        }
    }

    fun onDeviceInfoChange(deviceInfo: String) {
        _uiState.update { state ->
            val deviceInfoError = UiErrorMapper.mapForField(
                ValidationException(
                    buildList {
                        addAll(
                            org.penakelex.obscura.domain.validation
                                .InputValidator.validateDeviceInfo(
                                    deviceInfo
                                )
                        )
                    }
                ),
                "deviceInfo"
            )
            state.copy(
                deviceInfo = deviceInfo,
                deviceInfoError = deviceInfoError
            )
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.isLoading || !state.isRegisterEnabled) return
        if (state.password != state.confirmPassword) {
            _uiState.update {
                it.copy(
                    confirmPasswordError = UiErrorMapper.mapForField(
                        ValidationException(ValidationError.ConfirmPasswordMismatch()),
                        "confirmPassword"
                    )
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                registerUseCase(
                    email = state.email,
                    password = state.password,
                    deviceInfo = state.deviceInfo.takeIf { it.isNotBlank() }
                )
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.RegisterSuccessful,
                    )
                )
            } catch (e: ValidationException) {
                handleValidationErrors(e)
            } catch (e: AuthException) {
                _events.send(
                    UiEvent.ShowSnackbar(message = UiMessageMapper.map(e))
                )
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onLoginClick() {
        navigator.navigate(
            route = NavRoute.Auth.Login
        ) {
            launchSingleTop = true
            popUpTo(NavRoute.Auth.Register) { inclusive = true }
        }
    }

    fun onBackClick() {
        navigator.navigate(NavRoute.Main.NotesList) {
            popUpTo(NavRoute.Auth.Register) { inclusive = true }
            launchSingleTop = true
        }
    }

    private fun handleValidationErrors(e: ValidationException) {
        val allErrors = UiErrorMapper.mapAll(e)
        val emailError = allErrors.firstOrNull {
            it is UiError.EmailBlank ||
                    it is UiError.EmailTooLong ||
                    it is UiError.EmailInvalidFormat
        }
        val passwordError = allErrors.firstOrNull {
            it is UiError.PasswordTooShort ||
                    it is UiError.PasswordTooLong
        }
        val confirmPasswordError = allErrors.firstOrNull {
            it is UiError.ConfirmPasswordMismatch
        }
        val deviceInfoError = allErrors.firstOrNull {
            it is UiError.DeviceInfoTooLong
        }
        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
                    ?: it.confirmPasswordError,
                deviceInfoError = deviceInfoError,
            )
        }
    }
}