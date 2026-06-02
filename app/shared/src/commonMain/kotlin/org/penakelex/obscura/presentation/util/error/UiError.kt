package org.penakelex.obscura.presentation.util.error

sealed interface UiError {
    data object InvalidCredentials : UiError
    data class EmailAlreadyRegistered(val email: String) : UiError
    data object SessionExpired : UiError
    data object SessionNotFound : UiError
    data object NetworkError : UiError
    data class ServerError(val statusCode: Int) : UiError

    data object SyncUnauthenticated : UiError
    data object ServerUnavailable : UiError
    data object SyncTimeout : UiError
    data object InvalidPayload : UiError
    data object UnknownSyncError : UiError

    data object DecryptionFailed : UiError
    data class NoteNotFound(val noteId: String) : UiError

    data object EmailBlank : UiError
    data class EmailTooLong(val maxLength: Int) : UiError
    data object EmailInvalidFormat : UiError
    data class PasswordTooShort(val minLength: Int) : UiError
    data class PasswordTooLong(val maxLength: Int) : UiError
    data object CurrentPasswordBlank : UiError
    data object PasswordsMatch : UiError
    data object ContentBlank : UiError
    data class ContentTooLong(val maxLength: Int) : UiError
    data object InvalidTimestamp : UiError

    data class Unknown(val message: String?) : UiError
}