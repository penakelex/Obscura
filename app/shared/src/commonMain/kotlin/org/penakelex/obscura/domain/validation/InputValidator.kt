package org.penakelex.obscura.domain.validation

object InputValidator {
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    object Limits {
        const val EMAIL_MAX_LENGTH = 255
        const val PASSWORD_MIN_LENGTH = 8
        const val PASSWORD_MAX_LENGTH = 128
        const val NOTE_CONTENT_MAX_LENGTH = 100_000
    }

    fun validateEmail(email: String): List<ValidationError> =
        buildList {
            when {
                email.isBlank() -> add(ValidationError.EmailBlank())
                email.length > Limits.EMAIL_MAX_LENGTH ->
                    add(ValidationError.EmailTooLong(Limits.EMAIL_MAX_LENGTH))

                !EMAIL_REGEX.matches(email) ->
                    add(ValidationError.EmailInvalidFormat())
            }
        }

    fun validatePassword(password: String): List<ValidationError> =
        buildList {
            when {
                password.length < Limits.PASSWORD_MIN_LENGTH ->
                    add(ValidationError.PasswordTooShort(Limits.PASSWORD_MIN_LENGTH))

                password.length > Limits.PASSWORD_MAX_LENGTH ->
                    add(ValidationError.PasswordTooLong(Limits.PASSWORD_MAX_LENGTH))
            }
        }

    fun validateNoteContent(content: String): List<ValidationError> =
        buildList {
            when {
                content.isBlank() -> add(ValidationError.ContentBlank())
                content.length > Limits.NOTE_CONTENT_MAX_LENGTH ->
                    add(ValidationError.ContentTooLong(Limits.NOTE_CONTENT_MAX_LENGTH))
            }
        }
}