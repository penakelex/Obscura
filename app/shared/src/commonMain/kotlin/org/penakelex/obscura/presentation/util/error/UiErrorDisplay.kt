package org.penakelex.obscura.presentation.util.error

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiError.toDisplayString(): String = when (this) {
    UiError.EmailBlank ->
        stringResource(Res.string.validation_email_blank)

    is UiError.EmailTooLong ->
        stringResource(
            Res.string.validation_email_too_long,
            maxLength
        )

    UiError.EmailInvalidFormat ->
        stringResource(Res.string.validation_email_invalid_format)

    is UiError.PasswordTooShort ->
        stringResource(
            Res.string.validation_password_too_short,
            minLength
        )

    is UiError.PasswordTooLong ->
        stringResource(
            Res.string.validation_password_too_long,
            maxLength
        )

    UiError.CurrentPasswordBlank ->
        stringResource(Res.string.validation_current_password_blank)

    UiError.PasswordsMatch ->
        stringResource(Res.string.validation_passwords_match)

    UiError.ConfirmPasswordMismatch ->
        stringResource(Res.string.validation_confirm_password_mismatch)

    UiError.ContentBlank ->
        stringResource(Res.string.validation_content_blank)

    is UiError.ContentTooLong ->
        stringResource(
            Res.string.validation_content_too_long,
            maxLength
        )

    is UiError.DeviceInfoTooLong ->
        stringResource(
            Res.string.validation_device_info_too_long,
            maxLength
        )

    UiError.InvalidTimestamp ->
        stringResource(Res.string.validation_invalid_timestamp)
}