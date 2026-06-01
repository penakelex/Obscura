package org.penakelex.obscura.data.storage

internal object StorageConfig {

    object Android {
        const val APP_DIR_NAME: String = "obscura_session"
        const val SESSION_FILE_NAME: String = "session.json"
    }

    object Desktop {
        const val APP_DIR_NAME: String = ".obscura"
        const val SESSION_FILE_NAME: String = "session.json"
        const val POSIX_PERMISSIONS: String = "rw-------"
    }

    object Log {
        const val TAG: String = "TokenStorage"
    }
}