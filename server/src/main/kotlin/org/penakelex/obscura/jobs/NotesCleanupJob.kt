package org.penakelex.obscura.jobs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.repository.NoteRepository
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.hours

class NotesCleanupJob(
    private val jobsConfig: ServerConfig.Jobs,
    private val noteRepository: NoteRepository
) {
    private val logger = LoggerFactory.getLogger(NotesCleanupJob::class.java)

    fun start(scope: CoroutineScope): Job? {
        if (!jobsConfig.enabled) {
            logger.info(
                "Background jobs are disabled — NotesCleanupJob will not start"
            )
            return null
        }

        val intervalHours = jobsConfig.notesCleanupIntervalHours
        val retentionDays = jobsConfig.notesRetentionDays

        logger.info(
            "Starting NotesCleanupJob: interval={}h, retention={}d",
            intervalHours, retentionDays
        )

        return scope.launch {
            while (isActive) {
                runCleanup(retentionDays)
                delay(intervalHours.hours)
            }
        }
    }

    private suspend fun runCleanup(retentionDays: Int): Int = try {
        val deleted = noteRepository.purgeOldDeletedNotes(retentionDays)
        if (deleted > 0) {
            logger.info(
                "Notes cleanup completed: purged {} soft-deleted notes older than {} days",
                deleted, retentionDays
            )
        } else {
            logger.debug("Notes cleanup completed: nothing to purge")
        }
        deleted
    } catch (e: Exception) {
        logger.error("Failed to clean up notes: {}", e.message, e)
        0
    }
}