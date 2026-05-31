package org.penakelex.obscura.config

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class ServerConfig(root: Config = ConfigFactory.load()) {
    val database: Database
    val security: Security
    val validation: Validation
    val network: Network
    val jobs: Jobs
    val server: ServerSettings

    init {
        val obscura = root.getConfig("obscura")

        val db = obscura.getConfig("database")
        database = Database(
            url = if (db.hasPath("url")) db.getString("url") else null,
            driver = db.getString("driver"),
            user = if (db.hasPath("user")) db.getString("user") else null,
            password = if (db.hasPath("password")) db.getString("password") else null,
            autoMigrate = db.getBoolean("auto-migrate"),
            poolSize = db.getInt("pool-size"),
            connectionTimeoutSeconds = db.getInt("connection-timeout-seconds"),
            maxLifetimeSeconds = db.getInt("max-lifetime-seconds"),
            logSql = db.getBoolean("log-sql")
        )

        val securityConfig = obscura.getConfig("security")
        val session = securityConfig.getConfig("session")
        val password = securityConfig.getConfig("password")
        val hashParameters = password.getConfig("hash-parameters")
        val rateLimit = securityConfig.getConfig("rate-limit")
        security = Security(
            session = Security.Session(
                tokenLengthBytes = session.getInt("token-length-bytes"),
                expirationDays = session.getLong("expiration-days"),
                hashAlgorithm = session.getString("hash-algorithm")
            ),
            password = Security.Password(
                hashLength = password.getInt("hash-length"),
                algorithm = password.getString("algorithm"),
                hashParameters = Security.Password.HashParams(
                    iterations = hashParameters.getInt("iterations"),
                    memory = hashParameters.getInt("memory"),
                    parallelism = hashParameters.getInt("parallelism"),
                    outputLength = hashParameters.getInt("output-length"),
                    logRounds = hashParameters.getInt("log-rounds"),
                    workFactor = hashParameters.getInt("work-factor"),
                    resources = hashParameters.getInt("resources"),
                    hmacAlgorithm = hashParameters.getString("hmac-algorithm")
                )
            ),
            rateLimit = Security.RateLimit(
                maxRequests = rateLimit.getInt("max-requests"),
                windowMinutes = rateLimit.getInt("window-minutes"),
            ),
        )

        val validationConfig = obscura.getConfig("validation")
        val paginationConfig = validationConfig.getConfig("pagination")
        val syncConfig = validationConfig.getConfig("sync")
        val noteConfig = validationConfig.getConfig("note")
        validation = Validation(
            emailMaxLength = validationConfig.getInt("email.max-length"),
            passwordMinLength = validationConfig.getInt("password.min-length"),
            passwordMaxLength = validationConfig.getInt("password.max-length"),
            deviceInfoMaxLength = validationConfig.getInt("device-info.max-length"),
            pagination = Validation.Pagination(
                maxPageSize = paginationConfig.getInt("max-page-size"),
                defaultPageSize = paginationConfig.getInt("default-page-size")
            ),
            sync = Validation.Sync(
                maxChangesPerPayload = syncConfig.getInt("max-changes-per-payload")
            ),
            note = Validation.Note(
                maxEncryptedSizeBytes = noteConfig.getInt("max-encrypted-size-bytes")
            )
        )

        val networkConfig = obscura.getConfig("server")
        val shutdownConfig = networkConfig.getConfig("shutdown")
        val grpcConfig = networkConfig.getConfig("grpc")
        network = Network(
            host = networkConfig.getString("host"),
            port = networkConfig.getInt("port"),
            grpcPort = networkConfig.getInt("grpc-port")
        )
        server = ServerSettings(
            shutdown = ServerSettings.Shutdown(
                gracePeriodSeconds = shutdownConfig.getInt("grace-period-seconds"),
                timeoutSeconds = shutdownConfig.getInt("timeout-seconds")
            ),
            grpc = ServerSettings.Grpc(
                terminationTimeoutSeconds = grpcConfig.getInt("termination-timeout-seconds")
            )
        )

        val jobsConfig = obscura.getConfig("jobs")
        jobs = Jobs(
            enabled = jobsConfig.getBoolean("enabled"),
            sessionCleanupIntervalHours = jobsConfig.getInt("session-cleanup-interval-hours"),
            notesCleanupIntervalHours = jobsConfig.getInt("notes-cleanup-interval-hours"),
            notesRetentionDays = jobsConfig.getInt("notes-retention-days")
        )
    }

    data class Database(
        val url: String?,
        val driver: String,
        val user: String?,
        val password: String?,
        val autoMigrate: Boolean,
        val poolSize: Int,
        val connectionTimeoutSeconds: Int,
        val maxLifetimeSeconds: Int,
        val logSql: Boolean
    )

    data class Security(
        val session: Session,
        val password: Password,
        val rateLimit: RateLimit,
    ) {
        data class Session(
            val tokenLengthBytes: Int,
            val expirationDays: Long,
            val hashAlgorithm: String
        )
        data class Password(
            val hashLength: Int,
            val algorithm: String,
            val hashParameters: HashParams
        ) {
            data class HashParams(
                val iterations: Int,
                val memory: Int,
                val parallelism: Int,
                val outputLength: Int,
                val logRounds: Int,
                val workFactor: Int,
                val resources: Int,
                val hmacAlgorithm: String
            )
        }
        data class RateLimit(
            val maxRequests: Int,
            val windowMinutes: Int
        )
    }

    data class Validation(
        val emailMaxLength: Int,
        val passwordMinLength: Int,
        val passwordMaxLength: Int,
        val deviceInfoMaxLength: Int,
        val pagination: Pagination,
        val sync: Sync,
        val note: Note
    ) {
        data class Pagination(
            val maxPageSize: Int,
            val defaultPageSize: Int
        )
        data class Sync(
            val maxChangesPerPayload: Int
        )
        data class Note(
            val maxEncryptedSizeBytes: Int
        )
    }

    data class Network(
        val host: String,
        val port: Int,
        val grpcPort: Int
    )

    data class ServerSettings(
        val shutdown: Shutdown,
        val grpc: Grpc
    ) {
        data class Shutdown(
            val gracePeriodSeconds: Int,
            val timeoutSeconds: Int
        )
        data class Grpc(
            val terminationTimeoutSeconds: Int
        )
    }

    data class Jobs(
        val enabled: Boolean,
        val sessionCleanupIntervalHours: Int,
        val notesCleanupIntervalHours: Int,
        val notesRetentionDays: Int
    )
}