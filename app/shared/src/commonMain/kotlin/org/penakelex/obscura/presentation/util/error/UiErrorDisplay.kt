package org.penakelex.obscura.presentation.util.error

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiError.toDisplayString(): String = when (this) {
    UiError.InvalidCredentials ->
        stringResource(Res.string.error_invalid_credentials)
    is UiError.EmailAlreadyRegistered ->
        stringResource(Res.string.error_email_already_registered, email)
    UiError.SessionExpired ->
        stringResource(Res.string.error_session_expired)
    UiError.SessionNotFound ->
        stringResource(Res.string.error_session_not_found)
    UiError.NetworkError ->
        stringResource(Res.string.error_network)
    is UiError.ServerError ->
        stringResource(Res.string.error_server, statusCode)

    UiError.SyncUnauthenticated ->
        stringResource(Res.string.error_sync_unauthenticated)
    UiError.ServerUnavailable ->
        stringResource(Res.string.error_server_unavailable)
    UiError.SyncTimeout ->
        stringResource(Res.string.error_sync_timeout)
    UiError.InvalidPayload ->
        stringResource(Res.string.error_invalid_payload)
    UiError.UnknownSyncError ->
        stringResource(Res.string.error_sync_unknown)

    UiError.DecryptionFailed ->
        stringResource(Res.string.error_decryption_failed)
    is UiError.NoteNotFound ->
        stringResource(Res.string.error_note_not_found)

    UiError.EmailBlank ->
        stringResource(Res.string.validation_email_blank)
    is UiError.EmailTooLong ->
        stringResource(Res.string.validation_email_too_long, maxLength)
    UiError.EmailInvalidFormat ->
        stringResource(Res.string.validation_email_invalid_format)
    is UiError.PasswordTooShort ->
        stringResource(Res.string.validation_password_too_short, minLength)
    is UiError.PasswordTooLong ->
        stringResource(Res.string.validation_password_too_long, maxLength)
    UiError.CurrentPasswordBlank ->
        stringResource(Res.string.validation_current_password_blank)
    UiError.PasswordsMatch ->
        stringResource(Res.string.validation_passwords_match)
    UiError.ContentBlank ->
        stringResource(Res.string.validation_content_blank)
    is UiError.ContentTooLong ->
        stringResource(Res.string.validation_content_too_long, maxLength)
    UiError.InvalidTimestamp ->
        stringResource(Res.string.validation_invalid_timestamp)

    is UiError.Unknown -> message
        ?: stringResource(Res.string.error_unknown)
}