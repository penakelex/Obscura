package org.penakelex.obscura.presentation.screens.account

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
import org.penakelex.obscura.domain.usecase.auth.account.ChangeEmailUseCase
import org.penakelex.obscura.domain.usecase.auth.account.ChangePasswordUseCase
import org.penakelex.obscura.domain.usecase.auth.account.DeleteAccountUseCase
import org.penakelex.obscura.domain.usecase.auth.account.GetProfileUseCase
import org.penakelex.obscura.domain.usecase.auth.session.LogoutAllUseCase
import org.penakelex.obscura.domain.usecase.auth.session.LogoutUseCase
import org.penakelex.obscura.domain.usecase.note.CheckUnsyncedNotesUseCase
import org.penakelex.obscura.presentation.util.error.UiError
import org.penakelex.obscura.presentation.util.error.UiErrorMapper
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.UiMessageMapper

class AccountViewModel(
    private val getProfileUseCase: GetProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val changeEmailUseCase: ChangeEmailUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val logoutAllUseCase: LogoutAllUseCase,
    private val checkUnsyncedNotesUseCase: CheckUnsyncedNotesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events: Flow<UiEvent> = _events.receiveAsFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            try {
                val profile = getProfileUseCase()
                _uiState.update {
                    it.copy(
                        email = profile.email,
                        isLoading = false,
                    )
                }
            } catch (e: AuthException) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onChangePasswordClick() {
        _uiState.update { it.copy(isPasswordDialogVisible = true) }
    }

    fun onChangeEmailClick() {
        _uiState.update { it.copy(isEmailDialogVisible = true) }
    }

    fun onDeleteAccountClick() {
        _uiState.update { it.copy(isDeleteDialogVisible = true) }
    }

    fun onLogoutClick() {
        viewModelScope.launch {
            val pendingCount = checkUnsyncedNotesUseCase()
            _uiState.update {
                it.copy(
                    isLogoutDialogVisible = true,
                    pendingNotesCount = pendingCount,
                )
            }
        }
    }

    fun onLogoutAllClick() {
        viewModelScope.launch {
            val pendingCount = checkUnsyncedNotesUseCase()
            _uiState.update {
                it.copy(
                    isLogoutAllDialogVisible = true,
                    pendingNotesCount = pendingCount,
                )
            }
        }
    }

    fun onPasswordDialogDismiss() {
        _uiState.update { it.copy(isPasswordDialogVisible = false) }
    }

    fun onEmailDialogDismiss() {
        _uiState.update { it.copy(isEmailDialogVisible = false) }
    }

    fun onDeleteDialogDismiss() {
        _uiState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun onLogoutDialogDismiss() {
        _uiState.update { it.copy(isLogoutDialogVisible = false) }
    }

    fun onLogoutAllDialogDismiss() {
        _uiState.update { it.copy(isLogoutAllDialogVisible = false) }
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

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        onFieldErrors: (
            currentPasswordError: UiError?,
            newPasswordError: UiError?
        ) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            try {
                changePasswordUseCase(
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                )
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.PasswordChanged,
                    )
                )
                _uiState.update {
                    it.copy(
                        isPasswordDialogVisible = false,
                        isOperationInProgress = false,
                    )
                }
            } catch (e: ValidationException) {
                val currentError = UiErrorMapper.mapForField(
                    e, "currentPassword"
                )
                val newError =
                    UiErrorMapper.mapForField(e, "newPassword")
                onFieldErrors(currentError, newError)
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            } catch (e: AuthException) {
                handleAuthError(e)
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            }
        }
    }

    fun changeEmail(
        currentPassword: String,
        newEmail: String,
        onFieldErrors: (
            currentPasswordError: UiError?,
            emailError: UiError?
        ) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            try {
                changeEmailUseCase(
                    currentPassword = currentPassword,
                    newEmail = newEmail,
                )
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.EmailChanged,
                    )
                )
                loadProfile()
                _uiState.update {
                    it.copy(
                        isEmailDialogVisible = false,
                        isOperationInProgress = false,
                    )
                }
            } catch (e: ValidationException) {
                val currentError = UiErrorMapper.mapForField(
                    e, "currentPassword"
                )
                val emailError = UiErrorMapper.mapForField(e, "email")
                onFieldErrors(currentError, emailError)
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            } catch (e: AuthException) {
                handleAuthError(e)
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            }
        }
    }

    fun deleteAccount(
        currentPassword: String,
        onFieldError: (UiError?) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOperationInProgress = true) }
            try {
                deleteAccountUseCase(currentPassword = currentPassword)
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Success.AccountDeleted,
                    )
                )
                _uiState.update {
                    it.copy(
                        isDeleteDialogVisible = false,
                        isOperationInProgress = false,
                    )
                }
            } catch (e: ValidationException) {
                val error =
                    UiErrorMapper.mapForField(e, "currentPassword")
                onFieldError(error)
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            } catch (e: AuthException) {
                handleAuthError(e)
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            } catch (e: Exception) {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.Unknown(e.message),
                    )
                )
                _uiState.update {
                    it.copy(isOperationInProgress = false)
                }
            }
        }
    }

    private suspend fun handleAuthError(e: AuthException) {
        when (e) {
            is AuthException.InvalidCredentials -> {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessage.Error.InvalidCredentials,
                    )
                )
            }
            is AuthException.SessionExpired,
            is AuthException.SessionNotFound -> {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            }
            else -> {
                _events.send(
                    UiEvent.ShowSnackbar(
                        message = UiMessageMapper.map(e),
                    )
                )
            }
        }
    }
}