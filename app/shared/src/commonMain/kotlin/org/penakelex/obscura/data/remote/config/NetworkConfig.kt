package org.penakelex.obscura.data.remote.config

object NetworkConfig {
    object Rest {
        const val HOST: String = "localhost"
        const val PORT: Int = 8080
        const val BASE_URL: String = "http://$HOST:$PORT"
        const val TIMEOUT_MILLIS: Long = 15_000L

        object Paths {
            const val AUTH_REGISTER = "/api/auth/register"
            const val AUTH_LOGIN = "/api/auth/login"
            const val AUTH_LOGOUT = "/api/auth/logout"
            const val AUTH_LOGOUT_ALL = "/api/auth/logout/all"
            const val AUTH_ME = "/api/auth/me"
            const val AUTH_PASSWORD = "/api/auth/password"
            const val AUTH_EMAIL = "/api/auth/email"
            const val AUTH_ACCOUNT = "/api/auth/account"
            const val AUTH_SESSIONS = "/api/auth/sessions"

            const val NOTES_LIST = "/api/notes"
            const val NOTES_SYNC = "/api/notes/sync"
            const val NOTES_DELTA = "/api/notes/delta"
        }
    }

    object Grpc {
        const val HOST: String = "localhost"
        const val PORT: Int = 50051
        const val USE_TLS: Boolean = false
        const val MAX_INBOUND_MESSAGE_SIZE: Int = 4 * 1024 * 1024
    }
}