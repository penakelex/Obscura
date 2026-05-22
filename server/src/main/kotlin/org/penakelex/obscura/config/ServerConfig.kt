package org.penakelex.obscura.config

import com.typesafe.config.ConfigFactory

object ServerConfig {
    val database: Database
    val security: Security
    val network: Network
    val jobs: Jobs

    init {
        val root = ConfigFactory.load().getConfig("obscura")

        val db = root.getConfig("database")
        database = Database(
            url = db.getString("url"),
            driver = db.getString("driver"),
            user = db.getString("user"),
            password = db.getString("password"),
            autoMigrate = db.getBoolean("auto-migrate"),
            poolSize = db.getInt("pool-size"),
            connectionTimeoutSeconds = db.getInt("connection-timeout-seconds"),
            maxLifetimeSeconds = db.getInt("max-lifetime-seconds"),
            logSql = db.getBoolean("log-sql")
        )

        val sec = root.getConfig("security")
        security = Security(
            session = Security.Session(
                tokenLengthBytes = sec.getConfig("session")
                    .getInt("token-length-bytes"),
                expirationDays = sec.getConfig("session")
                    .getLong("expiration-days"),
                hashAlgorithm = sec.getConfig("session")
                    .getString("hash-algorithm")
            ),
            password = Security.Password(
                hashLength = sec.getConfig("password")
                    .getInt("hash-length"),
                algorithm = sec.getConfig("password")
                    .getString("algorithm")
            ),
            defaultCipherType = sec.getConfig("cipher")
                .getInt("default-type")
        )

        val net = root.getConfig("server")
        network = Network(
            host = net.getString("host"),
            port = net.getInt("port"),
            grpcPort = net.getInt("grpc-port")
        )

        val jobsCfg = root.getConfig("jobs")
        jobs = Jobs(
            enabled = jobsCfg.getBoolean("enabled"),
            sessionCleanupIntervalHours = jobsCfg.getInt("session-cleanup-interval-hours")
        )
    }

    data class Database(
        val url: String,
        val driver: String,
        val user: String,
        val password: String,
        val autoMigrate: Boolean,
        val poolSize: Int,
        val connectionTimeoutSeconds: Int,
        val maxLifetimeSeconds: Int,
        val logSql: Boolean
    )

    data class Security(
        val session: Session,
        val password: Password,
        val defaultCipherType: Int
    ) {
        data class Session(
            val tokenLengthBytes: Int,
            val expirationDays: Long,
            val hashAlgorithm: String
        )

        data class Password(
            val hashLength: Int,
            val algorithm: String
        )
    }

    data class Network(
        val host: String,
        val port: Int,
        val grpcPort: Int
    )

    data class Jobs(
        val enabled: Boolean,
        val sessionCleanupIntervalHours: Int
    )
}