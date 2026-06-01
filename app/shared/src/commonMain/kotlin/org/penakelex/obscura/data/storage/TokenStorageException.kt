package org.penakelex.obscura.data.storage

sealed class TokenStorageException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class SaveFailed(cause: Throwable) : TokenStorageException(
        message = "Failed to save session: ${cause.message}",
        cause = cause,
    )

    class ClearFailed(cause: Throwable) : TokenStorageException(
        message = "Failed to clear session: ${cause.message}",
        cause = cause,
    )
}