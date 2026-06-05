package org.penakelex.obscura.data.storage

internal object StorageConfig {
    object Android {
        const val APP_DIR_NAME: String = "obscura_session"
        const val SESSION_FILE_NAME: String = "session.json"
        const val GUEST_KEYSET_FILE_NAME: String = "guest_keyset.enc"
        const val GUEST_MASTER_KEY_FILE_NAME: String =
            "guest_master.key"
        const val ACCOUNT_MASTER_KEY_FILE_NAME: String =
            "account_master.key"
    }

    object Desktop {
        const val APP_DIR_NAME: String = ".obscura"
        const val SESSION_FILE_NAME: String = "session.json"
        const val GUEST_KEYSET_FILE_NAME: String = "guest_keyset.enc"
        const val GUEST_MASTER_KEY_FILE_NAME: String =
            "guest_master.key"
        const val ACCOUNT_KEY_FILE_NAME: String = "account_master.key"
        const val POSIX_PERMISSIONS: String = "rw-------"
    }

    object Log {
        const val TAG: String = "TokenStorage"
        const val GUEST_KEY_TAG: String = "GuestKeyStorage"
        const val ACCOUNT_KEY_TAG: String = "AccountKeyStorage"
    }
}