package org.penakelex.obscura.domain.usecase.sync

import co.touchlab.kermit.Logger
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.gateway.SyncGateway
import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.model.sync.SyncResult
import org.penakelex.obscura.domain.model.sync.SyncResultStatus
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository

class SyncNotesUseCase(
    private val noteRepository: NoteRepository,
    private val syncGateway: SyncGateway,
    private val authRepository: AuthRepository,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke(): Boolean {
        if (!authRepository.isLoggedIn()) {
            logger.d { "Skipping sync: user not logged in" }
            return false
        }

        return try {
            val result = executeSyncRound()
            logger.i {
                "Sync completed: applied ${result.appliedCount} local, " +
                        "received ${result.receivedCount} server changes"
            }
            true
        } catch (e: SyncException.Unauthenticated) {
            logger.w(e) { "Auth error during sync — forcing logout" }
            authRepository.logout()
            false
        } catch (e: SyncException) {
            logger.e(e) { "Sync failed: ${e.message}" }
            false
        }
    }

    private suspend fun executeSyncRound(): SyncRoundResult {
        val pendingChanges = noteRepository.getPendingChanges()
        val lastSyncTimestamp = loadLastSyncTimestamp()

        logger.d {
            "Sync round: ${pendingChanges.size} pending changes, " +
                    "lastSyncTs=$lastSyncTimestamp"
        }

        val result =
            syncGateway.sync(pendingChanges, lastSyncTimestamp)

        when (result.status) {
            SyncResultStatus.SUCCESS,
            SyncResultStatus.CONFLICT_RESOLVED -> {
                applyServerChanges(result)
                markLocalAsSynced(pendingChanges)
                saveLastSyncTimestamp(result.newSyncTimestamp)
            }

            SyncResultStatus.PARTIAL -> {
                logger.w { "Partial sync — applying what we got" }
                applyServerChanges(result)
                saveLastSyncTimestamp(result.newSyncTimestamp)
            }

            SyncResultStatus.AUTH_ERROR -> {
                throw SyncException.Unauthenticated(
                    IllegalStateException("Server returned AUTH_ERROR")
                )
            }
        }

        return SyncRoundResult(
            appliedCount = pendingChanges.size,
            receivedCount = result.serverChanges.size
        )
    }

    private suspend fun applyServerChanges(result: SyncResult) {
        val localStates = noteRepository
            .getSyncStates(result.serverChanges.map { it.id })
            .associateBy { it.id }

        for (serverNote in result.serverChanges) {
            val localState = localStates[serverNote.id]
            when {
                localState == null -> {
                    noteRepository.applyServerChanges(
                        listOf(
                            serverNote
                        )
                    )
                }

                serverNote.updatedAt > localState.updatedAt -> {
                    noteRepository.resolveConflict(
                        id = serverNote.id,
                        serverEncryptedData = serverNote.encryptedData,
                        serverCipherType = serverNote.cipherType,
                        serverUpdatedAt = serverNote.updatedAt
                    )
                }

                else -> {
                    logger.d {
                        "Ignoring older server note ${serverNote.id}"
                    }
                }
            }
        }
    }

    private suspend fun markLocalAsSynced(
        changes: List<SyncableNote>
    ) {
        changes.forEach { noteRepository.markSynced(it.id) }
        if (changes.any { it.isDeleted }) {
            noteRepository.purgeDeleted()
        }
    }

    private var lastSyncTimestamp: Long = 0L
    private fun loadLastSyncTimestamp(): Long = lastSyncTimestamp
    private fun saveLastSyncTimestamp(ts: Long) {
        lastSyncTimestamp = ts
    }

    private data class SyncRoundResult(
        val appliedCount: Int,
        val receivedCount: Int
    )

    private companion object {
        const val LOG_TAG = "SyncNotesUseCase"
    }
}