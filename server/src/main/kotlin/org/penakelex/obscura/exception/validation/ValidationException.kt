package org.penakelex.obscura.exception.validation

import io.ktor.http.HttpStatusCode
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.responses.common.FieldError
import org.penakelex.obscura.exception.ObscuraException

sealed class ValidationException(
    errorCode: String,
    message: String,
    val fieldErrors: List<FieldError> = emptyList()
) : ObscuraException(errorCode, HttpStatusCode.BadRequest, message) {
    class EmailBlank : ValidationException(
        errorCode = ErrorCodes.Validation.EMAIL_BLANK,
        message = "Email must not be blank",
        fieldErrors = listOf(
            FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_BLANK,
                "Email is required"
            )
        )
    )

    class EmailTooLong(maxLength: Int) : ValidationException(
        errorCode = ErrorCodes.Validation.EMAIL_TOO_LONG,
        message = "Email exceeds maximum length of $maxLength characters",
        fieldErrors = listOf(
            FieldError(
                "email",
                ErrorCodes.Validation.EMAIL_TOO_LONG,
                "Max $maxLength characters"
            )
        )
    )

    class InvalidEmailFormat(email: String) : ValidationException(
        errorCode = ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
        message = "Invalid email format: $email",
        fieldErrors = listOf(
            FieldError(
                "email",
                ErrorCodes.Validation.INVALID_EMAIL_FORMAT,
                "Invalid format"
            )
        )
    )

    class PasswordTooShort(minLength: Int) : ValidationException(
        errorCode = ErrorCodes.Validation.PASSWORD_TOO_SHORT,
        message = "Password must be at least $minLength characters",
        fieldErrors = listOf(
            FieldError(
                "password",
                ErrorCodes.Validation.PASSWORD_TOO_SHORT,
                "Min $minLength characters"
            )
        )
    )

    class PasswordTooLong(maxLength: Int) : ValidationException(
        errorCode = ErrorCodes.Validation.PASSWORD_TOO_LONG,
        message = "Password must not exceed $maxLength characters",
        fieldErrors = listOf(
            FieldError(
                "password",
                ErrorCodes.Validation.PASSWORD_TOO_LONG,
                "Max $maxLength characters"
            )
        )
    )

    class MultipleFields(errors: List<FieldError>) :
        ValidationException(
            errorCode = ErrorCodes.Validation.MULTIPLE_FIELDS_INVALID,
            message = "Validation failed for ${errors.size} field(s)",
            fieldErrors = errors
        )
}