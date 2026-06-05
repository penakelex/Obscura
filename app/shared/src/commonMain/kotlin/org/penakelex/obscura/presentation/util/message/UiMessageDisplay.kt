package org.penakelex.obscura.presentation.util.message

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun UiMessage.toDisplayString(): String = when (this) {
    UiMessage.Success.LoginSuccessful ->
        stringResource(Res.string.msg_login_successful)
    UiMessage.Success.RegisterSuccessful ->
        stringResource(Res.string.msg_register_successful)
    UiMessage.Success.LogoutSuccessful ->
        stringResource(Res.string.msg_logout_successful)
    UiMessage.Success.SyncSuccessful ->
        stringResource(Res.string.msg_sync_successful)
    UiMessage.Success.NoteSaved ->
        stringResource(Res.string.msg_note_saved)
    UiMessage.Success.NoteRestored ->
        stringResource(Res.string.msg_note_restored)
    is UiMessage.Success.NotesDeleted ->
        if (count == 1) {
            stringResource(Res.string.msg_notes_deleted_one)
        } else {
            stringResource(Res.string.msg_notes_deleted_many, count)
        }
    UiMessage.Success.PasswordChanged ->
        stringResource(Res.string.msg_password_changed)
    UiMessage.Success.EmailChanged ->
        stringResource(Res.string.msg_email_changed)
    UiMessage.Success.AccountDeleted ->
        stringResource(Res.string.msg_account_deleted)
    UiMessage.Success.SessionRevoked ->
        stringResource(Res.string.msg_session_revoked)

    UiMessage.Error.InvalidCredentials ->
        stringResource(Res.string.error_invalid_credentials)
    is UiMessage.Error.EmailAlreadyRegistered ->
        stringResource(Res.string.error_email_already_registered, email)
    UiMessage.Error.SessionExpired ->
        stringResource(Res.string.error_session_expired)
    UiMessage.Error.SessionNotFound ->
        stringResource(Res.string.error_session_not_found)
    UiMessage.Error.NetworkError ->
        stringResource(Res.string.error_network)
    is UiMessage.Error.ServerError ->
        stringResource(Res.string.error_server, statusCode)
    UiMessage.Error.ServerUnavailable ->
        stringResource(Res.string.error_server_unavailable)
    UiMessage.Error.SyncUnauthenticated ->
        stringResource(Res.string.error_sync_unauthenticated)
    UiMessage.Error.SyncTimeout ->
        stringResource(Res.string.error_sync_timeout)
    UiMessage.Error.InvalidPayload ->
        stringResource(Res.string.error_invalid_payload)
    UiMessage.Error.UnknownSyncError ->
        stringResource(Res.string.error_sync_unknown)
    UiMessage.Error.DecryptionFailed ->
        stringResource(Res.string.error_decryption_failed)
    is UiMessage.Error.NoteNotFound ->
        stringResource(Res.string.error_note_not_found)
    is UiMessage.Error.Unknown ->
        message ?: stringResource(Res.string.error_unknown)

    UiMessage.Error.KeysetDecryptionFailed ->
        stringResource(Res.string.error_keyset_decryption_failed)

    is UiMessage.Warning.CorruptedNotesSkipped ->
        stringResource(Res.string.msg_corrupted_notes_skipped, count)
    UiMessage.Warning.PasswordRecoveryUnavailable ->
        stringResource(Res.string.msg_password_recovery_unavailable)
    UiMessage.Warning.SyncRequiresAuth ->
        stringResource(Res.string.sync_requires_auth)
    UiMessage.Warning.AccountRequiresAuth ->
        stringResource(Res.string.account_requires_auth)
    UiMessage.Warning.SessionsRequiresAuth ->
        stringResource(Res.string.sessions_requires_auth)
}

suspend fun UiMessage.toDisplayMessage(): String = when (this) {
    UiMessage.Success.LoginSuccessful ->
        getString(Res.string.msg_login_successful)
    UiMessage.Success.RegisterSuccessful ->
        getString(Res.string.msg_register_successful)
    UiMessage.Success.LogoutSuccessful ->
        getString(Res.string.msg_logout_successful)
    UiMessage.Success.SyncSuccessful ->
        getString(Res.string.msg_sync_successful)
    UiMessage.Success.NoteSaved ->
        getString(Res.string.msg_note_saved)
    UiMessage.Success.NoteRestored ->
        getString(Res.string.msg_note_restored)
    is UiMessage.Success.NotesDeleted ->
        if (count == 1) {
            getString(Res.string.msg_notes_deleted_one)
        } else {
            getString(Res.string.msg_notes_deleted_many, count)
        }
    UiMessage.Success.PasswordChanged ->
        getString(Res.string.msg_password_changed)
    UiMessage.Success.EmailChanged ->
        getString(Res.string.msg_email_changed)
    UiMessage.Success.AccountDeleted ->
        getString(Res.string.msg_account_deleted)
    UiMessage.Success.SessionRevoked ->
        getString(Res.string.msg_session_revoked)

    UiMessage.Error.InvalidCredentials ->
        getString(Res.string.error_invalid_credentials)
    is UiMessage.Error.EmailAlreadyRegistered ->
        getString(Res.string.error_email_already_registered, email)
    UiMessage.Error.SessionExpired ->
        getString(Res.string.error_session_expired)
    UiMessage.Error.SessionNotFound ->
        getString(Res.string.error_session_not_found)
    UiMessage.Error.NetworkError ->
        getString(Res.string.error_network)
    is UiMessage.Error.ServerError ->
        getString(Res.string.error_server, statusCode)
    UiMessage.Error.ServerUnavailable ->
        getString(Res.string.error_server_unavailable)
    UiMessage.Error.SyncUnauthenticated ->
        getString(Res.string.error_sync_unauthenticated)
    UiMessage.Error.SyncTimeout ->
        getString(Res.string.error_sync_timeout)
    UiMessage.Error.InvalidPayload ->
        getString(Res.string.error_invalid_payload)
    UiMessage.Error.UnknownSyncError ->
        getString(Res.string.error_sync_unknown)
    UiMessage.Error.DecryptionFailed ->
        getString(Res.string.error_decryption_failed)
    is UiMessage.Error.NoteNotFound ->
        getString(Res.string.error_note_not_found)
    is UiMessage.Error.Unknown ->
        message ?: getString(Res.string.error_unknown)

    UiMessage.Error.KeysetDecryptionFailed ->
        getString(Res.string.error_keyset_decryption_failed)

    is UiMessage.Warning.CorruptedNotesSkipped ->
        getString(Res.string.msg_corrupted_notes_skipped, count)
    UiMessage.Warning.PasswordRecoveryUnavailable ->
        getString(Res.string.msg_password_recovery_unavailable)
    UiMessage.Warning.SyncRequiresAuth ->
        getString(Res.string.sync_requires_auth)
    UiMessage.Warning.AccountRequiresAuth ->
        getString(Res.string.account_requires_auth)
    UiMessage.Warning.SessionsRequiresAuth ->
        getString(Res.string.sessions_requires_auth)
}