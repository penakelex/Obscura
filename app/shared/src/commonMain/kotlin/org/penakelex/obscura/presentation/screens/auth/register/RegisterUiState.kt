package org.penakelex.obscura.presentation.screens.auth.register

import org.penakelex.obscura.presentation.util.error.UiError

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val deviceInfo: String = "",
    val isLoading: Boolean = false,
    val emailError: UiError? = null,
    val passwordError: UiError? = null,
    val confirmPasswordError: UiError? = null,
    val deviceInfoError: UiError? = null,
) {
    val isRegisterEnabled: Boolean
        get() = email.isNotBlank() &&
                password.isNotBlank() &&
                confirmPassword.isNotBlank() &&
                !isLoading &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                deviceInfoError == null
}