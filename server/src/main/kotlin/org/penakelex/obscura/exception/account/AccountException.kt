package org.penakelex.obscura.exception.account

import io.ktor.http.HttpStatusCode
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.exception.ObscuraException

sealed class AccountException(
    errorCode: String,
    httpStatus: HttpStatusCode,
    message: String
) : ObscuraException(errorCode, httpStatus, message) {
    class InvalidCurrentPassword : AccountException(
        errorCode = ErrorCodes.Account.INVALID_CURRENT_PASSWORD,
        httpStatus = HttpStatusCode.Unauthorized,
        message = "Current password is incorrect"
    )

    class NewEmailAlreadyTaken(email: String) : AccountException(
        errorCode = ErrorCodes.Account.NEW_EMAIL_ALREADY_TAKEN,
        httpStatus = HttpStatusCode.Conflict,
        message = "Email '$email' is already in use"
    )

    class PasswordSameAsCurrent : AccountException(
        errorCode = ErrorCodes.Account.PASSWORD_SAME_AS_CURRENT,
        httpStatus = HttpStatusCode.BadRequest,
        message = "New password must differ from current password"
    )
}