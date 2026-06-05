package org.penakelex.obscura.presentation.screens.auth.login

import org.penakelex.obscura.presentation.util.error.UiError

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val deviceInfo: String? = null,
    val isLoading: Boolean = false,
    val emailError: UiError? = null,
    val passwordError: UiError? = null,
) {
    val isLoginEnabled: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                !isLoading &&
                emailError == null &&
                passwordError == null
}