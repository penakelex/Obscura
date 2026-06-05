package org.penakelex.obscura.presentation.util.message

import org.penakelex.obscura.domain.exception.AuthException
import org.penakelex.obscura.domain.exception.ObscuraDomainException
import org.penakelex.obscura.domain.exception.SyncException

object UiMessageMapper {
    fun map(throwable: Throwable): UiMessage = when (throwable) {
        is AuthException -> mapAuth(throwable)
        is SyncException -> mapSync(throwable)
        is ObscuraDomainException -> mapDomain(throwable)
        else -> UiMessage.Error.Unknown(throwable.message)
    }

    private fun mapAuth(e: AuthException): UiMessage.Error = when (e) {
        is AuthException.InvalidCredentials ->
            UiMessage.Error.InvalidCredentials
        is AuthException.EmailAlreadyRegistered ->
            UiMessage.Error.EmailAlreadyRegistered(e.email)
        is AuthException.SessionExpired ->
            UiMessage.Error.SessionExpired
        is AuthException.SessionNotFound ->
            UiMessage.Error.SessionNotFound
        is AuthException.NetworkError ->
            UiMessage.Error.NetworkError
        is AuthException.KeysetNotFound ->
            UiMessage.Error.KeysetDecryptionFailed
        is AuthException.KeysetDecryptionFailed ->
            UiMessage.Error.KeysetDecryptionFailed
        is AuthException.ServerError ->
            UiMessage.Error.ServerError(e.statusCode)
    }

    private fun mapSync(e: SyncException): UiMessage.Error = when (e) {
        is SyncException.Unauthenticated ->
            UiMessage.Error.SyncUnauthenticated
        is SyncException.ServerUnavailable ->
            UiMessage.Error.ServerUnavailable
        is SyncException.Timeout ->
            UiMessage.Error.SyncTimeout
        is SyncException.InvalidPayload ->
            UiMessage.Error.InvalidPayload
        is SyncException.Unknown ->
            UiMessage.Error.UnknownSyncError
    }

    private fun mapDomain(e: ObscuraDomainException): UiMessage.Error =
        when (e) {
            is ObscuraDomainException.DecryptionException ->
                UiMessage.Error.DecryptionFailed
            is ObscuraDomainException.NoteNotFoundException ->
                UiMessage.Error.NoteNotFound(e.noteId)
        }
}