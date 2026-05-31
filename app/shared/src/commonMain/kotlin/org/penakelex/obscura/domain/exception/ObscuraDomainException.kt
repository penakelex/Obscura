package org.penakelex.obscura.domain.exception

sealed class ObscuraDomainException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class DecryptionException(
        val noteId: String,
        cause: Throwable
    ) : ObscuraDomainException(
        message = "Failed to decrypt note $noteId: ${cause.message}",
        cause = cause
    )

    class NoteNotFoundException(
        val noteId: String
    ) : ObscuraDomainException("Note with id '$noteId' not found")
}