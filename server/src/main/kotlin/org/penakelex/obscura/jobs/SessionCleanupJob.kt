package org.penakelex.obscura.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.repository.SessionRepository
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.hours

class SessionCleanupJob(
    private val config: ServerConfig.Jobs,
    private val sessionRepository: SessionRepository
) {
    private val logger =
        LoggerFactory.getLogger(SessionCleanupJob::class.java)

    fun start(scope: CoroutineScope): Job? {
        if (!config.enabled) {
            logger.info("Background jobs are disabled — SessionCleanupJob will not start")
            return null
        }
        val intervalHours = config.sessionCleanupIntervalHours
        logger.info(
            "Starting SessionCleanupJob with interval of {} hours",
            intervalHours
        )

        return scope.launch {
            while (isActive) {
                runCleanup()
                delay(intervalHours.hours)
            }
        }
    }

    private suspend fun runCleanup(): Int = try {
        val deleted = sessionRepository.deleteExpiredAndInactive()
        if (deleted > 0) {
            logger.info(
                "Session cleanup completed: removed {} expired/inactive sessions",
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