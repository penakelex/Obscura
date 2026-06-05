package org.penakelex.obscura.data.storage.exception

sealed class GuestKeyStorageException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class SaveFailed(cause: Throwable) : GuestKeyStorageException(
        message = "Failed to save guest keyset: ${cause.message}",
        cause = cause,
    )

    class ClearFailed(cause: Throwable) : GuestKeyStorageException(
        message = "Failed to clear guest keyset: ${cause.message}",
        cause = cause,
    )
}