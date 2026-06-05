package org.penakelex.obscura.presentation.util.error

sealed interface UiError {
    data object EmailBlank : UiError
    data class EmailTooLong(val maxLength: Int) : UiError
    data object EmailInvalidFormat : UiError
    data class PasswordTooShort(val minLength: Int) : UiError
    data class PasswordTooLong(val maxLength: Int) : UiError
    data object CurrentPasswordBlank : UiError
    data object PasswordsMatch : UiError
    data object ConfirmPasswordMismatch : UiError
    data object ContentBlank : UiError
    data class ContentTooLong(val maxLength: Int) : UiError
    data class DeviceInfoTooLong(val maxLength: Int) : UiError
    data object InvalidTimestamp : UiError
}