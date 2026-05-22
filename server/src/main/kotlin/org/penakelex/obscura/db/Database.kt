package org.penakelex.obscura.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.Slf4jSqlDebugLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.penakelex.obscura.config.ServerConfig
import org.slf4j.LoggerFactory

object DatabaseManager {
    private val logger =
        LoggerFactory.getLogger(DatabaseManager::class.java)

    private var dataSource: HikariDataSource? = null

    fun init(): Database {
        val config = HikariConfig().apply {
            jdbcUrl = ServerConfig.database.url
            driverClassName = ServerConfig.database.driver
            username = ServerConfig.database.user
            password = ServerConfig.database.password
            maximumPoolSize = ServerConfig.database.poolSize
            connectionTimeout =
                ServerConfig.database.connectionTimeoutSeconds * 1000L
            maxLifetime =
                ServerConfig.database.maxLifetimeSeconds * 1000L
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            connectionTestQuery = "SELECT 1"
            validationTimeout = 5000
        }

        val ds = HikariDataSource(config)
        dataSource = ds

        val databaseConfig = if (ServerConfig.database.logSql) {
            logger.info(
                "SQL logging is ENABLED " +
                        "— all queries will be logged via SLF4J"
            )
            DatabaseConfig { sqlLogger = Slf4jSqlDebugLogger }
        } else {
            DatabaseConfig { sqlLogger = null }
        }

        return Database.connect(ds, databaseConfig = databaseConfig)
    }

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