package org.penakelex.obscura.data.storage

sealed class AccountKeyStorageException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class SaveFailed(cause: Throwable) : AccountKeyStorageException(
        message = "Failed to save account master key: ${cause.message}",
        cause = cause,
    )

    class ClearFailed(cause: Throwable) : AccountKeyStorageException(
        message = "Failed to clear account master key: ${cause.message}",
        cause = cause,
    )
}