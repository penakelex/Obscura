package org.penakelex.obscura.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.db.tables.Users
import org.slf4j.LoggerFactory
import kotlin.time.Clock

object DatabaseManager {
    private val logger =
        LoggerFactory.getLogger(DatabaseManager::class.java)
    private val startedAt = Clock.System.now()
    private var dataSource: HikariDataSource? = null

    fun init(serverConfigDatabase: ServerConfig.Database): Database {
        val config = HikariConfig().apply {
            jdbcUrl = serverConfigDatabase.url
            driverClassName = serverConfigDatabase.driver
            username = serverConfigDatabase.user
            password = serverConfigDatabase.password
            maximumPoolSize = serverConfigDatabase.poolSize
            connectionTimeout =
                serverConfigDatabase.connectionTimeoutSeconds * 1000L
            maxLifetime =
                serverConfigDatabase.maxLifetimeSeconds * 1000L
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validationTimeout = 5000
        }

        val ds = HikariDataSource(config)
        dataSource = ds

        val databaseConfig = if (serverConfigDatabase.logSql) {
            logger.info("SQL logging is ENABLED — all queries will be logged via SLF4J")
            DatabaseConfig { sqlLogger = Slf4jSqlDebugLogger }
        } else {
            DatabaseConfig { sqlLogger = null }
        }

        val database =
            Database.connect(ds, databaseConfig = databaseConfig)

        if (serverConfigDatabase.autoMigrate) {
            logger.info("Auto-migration enabled: checking schema...")
            try {
                val statements = transaction(database) {
                    MigrationUtils.statementsRequiredForDatabaseMigration(
                        Users, Sessions, Notes
                    )
                }

                if (statements.isEmpty()) {
                    logger.info("Database schema is up to date.")
                } else {
                    logger.info(
                        "Applying {} migration statements...",
                        statements.size
                    )
                    transaction(database) {
                        statements.forEach { statement ->
                            logger.debug("Executing: {}", statement)
                            exec(statement)
                        }
                    }
                    logger.info("Database schema updated successfully.")
                }
            } catch (e: Exception) {
                logger.error(
                    "Failed to migrate database: {}",
                    e.message,
                    e
                )
                throw e
            }
        }

        return database
    }

    fun isHealthy(): Boolean {
        val ds = dataSource ?: return false

        if (ds.isClosed) {
            return false
        }

        return try {
            ds.connection.use { connection -> connection.isValid(2) }
        } catch (e: Exception) {
            logger.warn("Health check failed: {}", e.message)
            false
        }
    }

    fun uptimeSeconds(): Long =
        (Clock.System.now() - startedAt).inWholeSeconds

    fun close() {
        dataSource?.let { ds ->
            if (!ds.isClosed) {
                logger.info("Shutting down HikariCP connection pool...")
                ds.close()
                logger.info("HikariCP connection pool closed")
            }
        }
        dataSource = null
    }
}