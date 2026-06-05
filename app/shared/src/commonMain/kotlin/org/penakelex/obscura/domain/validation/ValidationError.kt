package org.penakelex.obscura.domain.validation

sealed class ValidationError(val field: String, val message: String) {
    class EmailBlank :
        ValidationError("email", "Email must not be blank")

    class EmailTooLong(val maxLength: Int) : ValidationError(
        "email",
        "Email exceeds $maxLength characters"
    )

    class EmailInvalidFormat :
        ValidationError("email", "Invalid email format")

    class PasswordTooShort(val minLength: Int) : ValidationError(
        "password",
        "Password must be at least $minLength characters"
    )

    class PasswordTooLong(val maxLength: Int) : ValidationError(
        "password",
        "Password must not exceed $maxLength characters"
    )

    class CurrentPasswordBlank : ValidationError(
        "currentPassword",
        "Current password is required"
    )

    class PasswordsMatch : ValidationError(
        "newPassword",
        "New password must differ from current password"
    )

    class ConfirmPasswordMismatch : ValidationError(
        "confirmPassword",
        "Passwords do not match"
    )

    class ContentBlank :
        ValidationError("content", "Note content must not be blank")

    class ContentTooLong(val maxLength: Int) : ValidationError(
        "content",
        "Content exceeds $maxLength characters"
    )

    class InvalidTimestamp :
        ValidationError("timestamp", "Timestamp must be non-negative")

    class DeviceInfoTooLong(val maxLength: Int) : ValidationError(
        "deviceInfo",
        "Device info must not exceed $maxLength characters"
    )
}