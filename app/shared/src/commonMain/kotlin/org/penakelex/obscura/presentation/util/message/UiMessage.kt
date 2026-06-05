package org.penakelex.obscura.presentation.util.message

sealed interface UiMessage {
    sealed interface Success : UiMessage {
        data object LoginSuccessful : Success
        data object RegisterSuccessful : Success
        data object LogoutSuccessful : Success
        data object SyncSuccessful : Success
        data object NoteSaved : Success
        data object NoteRestored : Success
        data class NotesDeleted(val count: Int) : Success
        data object PasswordChanged : Success
        data object EmailChanged : Success
        data object AccountDeleted : Success
        data object SessionRevoked : Success
    }

    sealed interface Error : UiMessage {
        data object InvalidCredentials : Error
        data class EmailAlreadyRegistered(val email: String) : Error
        data object SessionExpired : Error
        data object SessionNotFound : Error

        data object NetworkError : Error
        data class ServerError(val statusCode: Int) : Error
        data object ServerUnavailable : Error

        data object SyncUnauthenticated : Error
        data object SyncTimeout : Error
        data object InvalidPayload : Error
        data object UnknownSyncError : Error

        data object KeysetDecryptionFailed : Error

        data object DecryptionFailed : Error
        data class NoteNotFound(val noteId: String) : Error

        data class Unknown(val message: String?) : Error
    }

    sealed interface Warning : UiMessage {
        data class CorruptedNotesSkipped(val count: Int) : Warning
        data object PasswordRecoveryUnavailable : Warning
        data object SyncRequiresAuth : Warning
        data object AccountRequiresAuth : Warning
        data object SessionsRequiresAuth : Warning
    }
}