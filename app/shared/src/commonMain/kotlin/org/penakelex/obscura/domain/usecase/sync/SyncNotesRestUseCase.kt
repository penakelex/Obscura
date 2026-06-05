package org.penakelex.obscura.domain.usecase.sync

import co.touchlab.kermit.Logger
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.gateway.NotesGateway
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository

class SyncNotesRestUseCase(
    private val noteRepository: NoteRepository,
    private val authRepository: AuthRepository,
    private val settingsGateway: SettingsGateway,
    private val notesGateway: NotesGateway,
) {
    private val logger = Logger.withTag(LOG_TAG)

    suspend operator fun invoke(): Boolean {
        if (!authRepository.isLoggedIn()) {
            logger.d { "Skipping REST sync: user not logged in" }
            return false
        }

        return try {
            val result = executeSyncRound()
            logger.i {
                "REST sync completed: applied ${result.appliedCount} local, " +
                        "received ${result.receivedCount} server changes"
            }
            true
        } catch (e: SyncException.Unauthenticated) {
            logger.w(e) { "Auth error during REST sync — forcing logout" }
            authRepository.logout()
            false
        } catch (e: SyncException) {
            logger.e(e) { "REST sync failed: ${e.message}" }
            false
        }
    }

    private suspend fun executeSyncRound(): SyncRoundResult {
        val pendingChanges = noteRepository.getPendingChanges()
        val lastSyncTimestamp =
            settingsGateway.get().lastSyncTimestamp

        logger.d {
            "REST sync round: ${pendingChanges.size} pending changes, " +
                    "lastSyncTs=$lastSyncTimestamp"
        }

        val result =
            notesGateway.sync(pendingChanges, lastSyncTimestamp)

        applyServerChanges(result.serverChanges)
        markLocalAsSynced(pendingChanges)
        settingsGateway.setLastSyncTimestamp(result.newSyncTimestamp)

        return SyncRoundResult(
            appliedCount = pendingChanges.size,
            receivedCount = result.serverChanges.size
        )
    }

    private suspend fun applyServerChanges(serverNotes: List<SyncableNote>) {
        val localStates = noteRepository
            .getSyncStates(serverNotes.map { it.id })
            .associateBy { it.id }
        for (serverNote in serverNotes) {
            val localState = localStates[serverNote.id]
            when {
                localState == null -> {
                    noteRepository.applyServerChanges(
                        listOf(serverNote)
                    )
                }
                serverNote.updatedAt > localState.updatedAt -> {
                    noteRepository.resolveConflict(
                        id = serverNote.id,
                        serverEncryptedData = serverNote.encryptedData,
                        serverCipherType = serverNote.cipherType,
                        serverUpdatedAt = serverNote.updatedAt,
                        serverIsDeleted = serverNote.isDeleted
                    )
                }
                else -> {
                    logger.d {
                        "Ignoring older server note ${serverNote.id} (REST)"
                    }
                }
            }
        }
    }

    private suspend fun markLocalAsSynced(changes: List<SyncableNote>) {
        changes.forEach { noteRepository.markSynced(it.id) }
        if (changes.any { it.isDeleted }) {
            noteRepository.purgeDeleted()
        }
    }

    private data class SyncRoundResult(
        val appliedCount: Int,
        val receivedCount: Int
    )

    private companion object {
        const val LOG_TAG = "SyncNotesRestUseCase"
    }
}