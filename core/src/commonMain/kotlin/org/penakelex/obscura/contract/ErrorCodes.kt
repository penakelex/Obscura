package org.penakelex.obscura.contract

object ErrorCodes {
    object Auth {
        const val INVALID_CREDENTIALS = "AUTH_INVALID_CREDENTIALS"
        const val EMAIL_ALREADY_REGISTERED =
            "AUTH_EMAIL_ALREADY_REGISTERED"
        const val SESSION_EXPIRED = "AUTH_SESSION_EXPIRED"
        const val SESSION_NOT_FOUND = "AUTH_SESSION_NOT_FOUND"
    }

    object Validation {
        const val INVALID_EMAIL_FORMAT =
            "VALIDATION_INVALID_EMAIL_FORMAT"
        const val EMAIL_TOO_LONG = "VALIDATION_EMAIL_TOO_LONG"
        const val EMAIL_BLANK = "VALIDATION_EMAIL_BLANK"
        const val PASSWORD_TOO_SHORT = "VALIDATION_PASSWORD_TOO_SHORT"
        const val PASSWORD_TOO_LONG = "VALIDATION_PASSWORD_TOO_LONG"
        const val DEVICE_INFO_TOO_LONG =
            "VALIDATION_DEVICE_INFO_TOO_LONG"
        const val CANNOT_REVOKE_CURRENT_SESSION =
            "VALIDATION_CANNOT_REVOKE_CURRENT_SESSION"
        const val SESSION_ID_REQUIRED =
            "VALIDATION_SESSION_ID_REQUIRED"
        const val MULTIPLE_FIELDS_INVALID =
            "VALIDATION_MULTIPLE_FIELDS_INVALID"
    }

    object Resources {
        const val USER_NOT_FOUND = "RES_USER_NOT_FOUND"
        const val NOTE_NOT_FOUND = "RES_NOTE_NOT_FOUND"
        const val SESSION_NOT_FOUND = "RES_SESSION_NOT_FOUND"
    }

    object Sync {
        const val CONFLICT_RESOLVED = "SYNC_CONFLICT_RESOLVED"
        const val QUOTA_EXCEEDED = "SYNC_QUOTA_EXCEEDED"
        const val PAYLOAD_TOO_LARGE = "SYNC_PAYLOAD_TOO_LARGE"
    }

    object Account {
        const val INVALID_CURRENT_PASSWORD =
            "ACCOUNT_INVALID_CURRENT_PASSWORD"
        const val NEW_EMAIL_ALREADY_TAKEN =
            "ACCOUNT_NEW_EMAIL_ALREADY_TAKEN"
        const val PASSWORD_SAME_AS_CURRENT =
            "ACCOUNT_PASSWORD_SAME_AS_CURRENT"
    }

    object System {
        const val INTERNAL_ERROR = "SYS_INTERNAL_ERROR"
    }
}