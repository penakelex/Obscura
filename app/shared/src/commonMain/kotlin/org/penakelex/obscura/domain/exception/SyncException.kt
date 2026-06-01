package org.penakelex.obscura.domain.exception

sealed class SyncException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class Unauthenticated(cause: Throwable) : SyncException(
        "Session expired or revoked — re-login required",
        cause
    )

    class ServerUnavailable(cause: Throwable) : SyncException(
        "Server is unreachable",
        cause
    )

    class Timeout(cause: Throwable) : SyncException(
        "Sync request timed out",
        cause
    )

    class InvalidPayload(cause: Throwable) : SyncException(
        "Invalid sync payload: ${cause.message}",
        cause
    )

    class Unknown(cause: Throwable) : SyncException(
        "Unknown sync error: ${cause.message}",
        cause
    )
}