package org.penakelex.obscura.exception.auth

import io.ktor.http.HttpStatusCode
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.exception.ObscuraException

sealed class AuthException(
    errorCode: String,
    httpStatus: HttpStatusCode,
    message: String
) : ObscuraException(errorCode, httpStatus, message) {
    class InvalidCredentials : AuthException(
        errorCode = ErrorCodes.Auth.INVALID_CREDENTIALS,
        httpStatus = HttpStatusCode.Unauthorized,
        message = "Invalid email or password"
    )

    class EmailAlreadyRegistered(email: String) : AuthException(
        errorCode = ErrorCodes.Auth.EMAIL_ALREADY_REGISTERED,
        httpStatus = HttpStatusCode.Conflict,
        message = "Email '$email' is already registered"
    )

    class SessionExpired : AuthException(
        errorCode = ErrorCodes.Auth.SESSION_EXPIRED,
        httpStatus = HttpStatusCode.Unauthorized,
        message = "Session has expired"
    )

    class SessionNotFound : AuthException(
        errorCode = ErrorCodes.Auth.SESSION_NOT_FOUND,
        httpStatus = HttpStatusCode.Unauthorized,
        message = "Invalid or revoked session"
    )
}