package org.penakelex.obscura.presentation.util.error

import org.penakelex.obscura.domain.exception.AuthException
import org.penakelex.obscura.domain.exception.ObscuraDomainException
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.exception.ValidationException
import org.penakelex.obscura.domain.validation.ValidationError

object UiErrorMapper {
    fun map(throwable: Throwable): UiError = when (throwable) {
        is ValidationException -> mapValidation(throwable.errors.first())
        is AuthException -> mapAuth(throwable)
        is SyncException -> mapSync(throwable)
        is ObscuraDomainException -> mapDomain(throwable)
        else -> UiError.Unknown(throwable.message)
    }

    fun mapAll(throwable: Throwable): List<UiError> =
        when (throwable) {
            is ValidationException -> throwable.errors.map(::mapValidation)
            else -> listOf(map(throwable))
        }

    fun mapForField(throwable: Throwable, field: String): UiError? =
        (throwable as? ValidationException)
            ?.errors
            ?.firstOrNull { it.field == field }
            ?.let(::mapValidation)

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
            is ValidationError.ContentBlank -> UiError.ContentBlank
            is ValidationError.ContentTooLong ->
                UiError.ContentTooLong(error.maxLength)

            is ValidationError.InvalidTimestamp ->
                UiError.InvalidTimestamp
        }

    private fun mapAuth(e: AuthException): UiError = when (e) {
        is AuthException.InvalidCredentials ->
            UiError.InvalidCredentials

        is AuthException.EmailAlreadyRegistered ->
            UiError.EmailAlreadyRegistered(e.email)

        is AuthException.SessionExpired -> UiError.SessionExpired
        is AuthException.SessionNotFound -> UiError.SessionNotFound
        is AuthException.NetworkError -> UiError.NetworkError
        is AuthException.ServerError ->
            UiError.ServerError(e.statusCode)
    }

    private fun mapSync(e: SyncException): UiError = when (e) {
        is SyncException.Unauthenticated -> UiError.SyncUnauthenticated
        is SyncException.ServerUnavailable -> UiError.ServerUnavailable
        is SyncException.Timeout -> UiError.SyncTimeout
        is SyncException.InvalidPayload -> UiError.InvalidPayload
        is SyncException.Unknown -> UiError.UnknownSyncError
    }

    private fun mapDomain(e: ObscuraDomainException): UiError =
        when (e) {
            is ObscuraDomainException.DecryptionException ->
                UiError.DecryptionFailed

            is ObscuraDomainException.NoteNotFoundException ->
                UiError.NoteNotFound(e.noteId)
        }
}