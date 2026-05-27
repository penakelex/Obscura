package org.penakelex.obscura.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.tables.Sessions
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

object SessionCleanupJob {
    private val logger =
        LoggerFactory.getLogger(SessionCleanupJob::class.java)

    fun start(scope: CoroutineScope): Job? {
        if (!ServerConfig.jobs.enabled) {
            logger.info(
                "Background jobs are disabled in config — " +
                        "SessionCleanupJob will not start"
            )
            return null
        }

        val intervalHours =
            ServerConfig.jobs.sessionCleanupIntervalHours
        logger.info(
            "Starting SessionCleanupJob with interval of {} hours",
            intervalHours
        )

        return scope.launch {
            while (isActive) {
                delay(intervalHours.hours)
                runCleanup()
            }
        }
    }

    private suspend fun runCleanup(): Int = try {
        val deleted = withContext(Dispatchers.IO) {
            suspendTransaction {
                val now = Clock.System.now()
                Sessions.deleteWhere {
                    (Sessions.expiresAt less now) or
                            (Sessions.isActive eq false)
                }
            }
        }

        if (deleted > 0) {
            logger.info(
                "Session cleanup completed: " +
                        "removed {} expired/inactive sessions",
                deleted
            )
        } else {
            logger.debug("Session cleanup completed: nothing to remove")
        }
        deleted
    } catch (e: Exception) {
        logger.error("Failed to clean up sessions: {}", e.message, e)
        0
    }
}