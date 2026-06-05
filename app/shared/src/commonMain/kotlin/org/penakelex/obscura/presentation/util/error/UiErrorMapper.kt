package org.penakelex.obscura.presentation.util.error

import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.validation.ValidationError

object UiErrorMapper {
    fun mapForField(throwable: Throwable, field: String): UiError? =
        (throwable as? ValidationException)
            ?.errors
            ?.firstOrNull { it.field == field }
            ?.let(::mapValidation)

    fun mapAll(throwable: Throwable): List<UiError> =
        (throwable as? ValidationException)
            ?.errors
            ?.map(::mapValidation)
            .orEmpty()

    private fun mapValidation(error: ValidationError): UiError =
        when (error) {
            is ValidationError.EmailBlank -> UiError.EmailBlank
            is ValidationError.EmailTooLong ->
                UiError.EmailTooLong(error.maxLength)

            is ValidationError.EmailInvalidFormat ->
                UiError.EmailInvalidFormat

            is ValidationError.PasswordTooShort ->
                UiError.PasswordTooShort(error.minLength)

            is ValidationError.PasswordTooLong ->
                UiError.PasswordTooLong(error.maxLength)

            is ValidationError.CurrentPasswordBlank ->
                UiError.CurrentPasswordBlank

            is ValidationError.PasswordsMatch -> UiError.PasswordsMatch
            is ValidationError.ConfirmPasswordMismatch ->
                UiError.ConfirmPasswordMismatch

            is ValidationError.ContentBlank -> UiError.ContentBlank
            is ValidationError.ContentTooLong ->
                UiError.ContentTooLong(error.maxLength)

            is ValidationError.DeviceInfoTooLong ->
                UiError.DeviceInfoTooLong(error.maxLength)

            is ValidationError.InvalidTimestamp ->
                UiError.InvalidTimestamp
        }
}