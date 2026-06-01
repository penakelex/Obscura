package org.penakelex.obscura.domain.exception

sealed class AuthException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class InvalidCredentials : AuthException(
        "Invalid email or password"
    )

    class EmailAlreadyRegistered(val email: String) : AuthException(
        "Email '$email' is already registered"
    )

    class SessionExpired : AuthException("Session has expired")

    class SessionNotFound : AuthException(
        "Session is invalid or has been revoked"
    )

    class NetworkError(cause: Throwable) : AuthException(
        "Network error: ${cause.message}",
        cause
    )

    class ServerError(
        val statusCode: Int,
        message: String
    ) : AuthException("Server error ($statusCode): $message")
}