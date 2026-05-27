package org.penakelex.obscura.exception.resource

import io.ktor.http.HttpStatusCode
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.exception.ObscuraException

sealed class NotFoundException(
    errorCode: String,
    message: String
) : ObscuraException(errorCode, HttpStatusCode.NotFound, message) {
    class UserNotFound(userId: String) : NotFoundException(
        errorCode = ErrorCodes.Resources.USER_NOT_FOUND,
        message = "User with id '$userId' not found"
    )

    class NoteNotFound(noteId: String) : NotFoundException(
        errorCode = ErrorCodes.Resources.NOTE_NOT_FOUND,
        message = "Note with id '$noteId' not found"
    )
}