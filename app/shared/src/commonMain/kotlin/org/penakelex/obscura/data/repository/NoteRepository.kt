package org.penakelex.obscura.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.penakelex.obscura.crypto.CipherType
import org.penakelex.obscura.crypto.CryptoException
import org.penakelex.obscura.crypto.CryptoProvider
import org.penakelex.obscura.domain.Note
import org.penakelex.obscura.domain.exception.ObscuraDomainException
import org.penakelex.obscura.persistence.SyncStatus
import org.penakelex.obscura.persistence.dao.NoteDao
import org.penakelex.obscura.persistence.dao.NoteSyncState
import org.penakelex.obscura.persistence.entity.NoteEntity
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NoteRepository(
    private val noteDao: NoteDao,
    private val cryptoProvider: CryptoProvider
) {
    private val logger = Logger.withTag(LOG_TAG)

    fun observeNotes(): Flow<NotesResult> =
        noteDao.observeActiveNotes().map { entities ->
            val notes = mutableListOf<Note>()
            val corruptedIds = mutableListOf<String>()

            for (entity in entities) {
                try {
                    notes.add(entity.toNote())
                } catch (e: ObscuraDomainException.DecryptionException) {
                    corruptedIds.add(entity.id)
                    logger.w(e) { "Skipping corrupted note: ${entity.id}" }
                }
            }

            if (corruptedIds.isNotEmpty()) {
                logger.w { "Found ${corruptedIds.size} corrupted note(s)" }
            }

            NotesResult(
                notes = notes,
                corruptedNoteIds = corruptedIds
            )
        }

    suspend fun getNoteById(id: String): Note {
        val entity = noteDao.getById(id)
            ?: throw ObscuraDomainException.NoteNotFoundException(
                id
            )
        return entity.toNote()
    }

    suspend fun createNote(
        content: String,
        cipherType: CipherType = CipherType.DEFAULT
    ): String {
        val id = Uuid.random().toString()
        val encryptedData = cryptoProvider.encrypt(
            content.encodeToByteArray(),
            cipherType
        )

        val entity = NoteEntity(
            id = id,
            encryptedData = encryptedData,
            cipherType = cipherType,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            syncStatus = SyncStatus.PENDING,
            version = 1,
            isDeleted = false
        )

        noteDao.upsert(entity)
        logger.d { "Created note: $id (cipher=$cipherType)" }
        return id
    }

    suspend fun updateNote(
        id: String,
        content: String,
        cipherType: CipherType
    ) {
        val existing = noteDao.getById(id)
            ?: throw ObscuraDomainException.NoteNotFoundException(id)

        val encryptedData = cryptoProvider.encrypt(
            content.encodeToByteArray(),
            cipherType
        )

        val updated = existing.copy(
            encryptedData = encryptedData,
            cipherType = cipherType,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            syncStatus = SyncStatus.PENDING,
            version = existing.version + 1
        )

        noteDao.upsert(updated)
        logger.d { "Updated note: $id" }
    }

    suspend fun deleteNote(id: String) {
        noteDao.softDelete(
            id = id,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        logger.d { "Soft-deleted note: $id" }
    }

    suspend fun getPendingChanges(): List<NoteEntity> =
        noteDao.getPendingChanges()

    suspend fun markSynced(id: String) {
        noteDao.markSynced(
            id = id,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    suspend fun applyServerChanges(entities: List<NoteEntity>) {
        noteDao.upsertAll(entities)
        logger.i { "Applied ${entities.size} server changes" }
    }

    suspend fun getSyncStates(ids: List<String>): List<NoteSyncState> =
        noteDao.getSyncStates(ids)

    suspend fun resolveConflict(
        id: String,
        serverEncryptedData: ByteArray,
        serverCipherType: CipherType,
        serverUpdatedAt: Long
    ) {
        noteDao.resolveConflict(
            id = id,
            encryptedData = serverEncryptedData,
            cipherType = serverCipherType,
            updatedAt = serverUpdatedAt
        )
        logger.i { "Conflict resolved for note: $id (accepted server version)" }
    }

    suspend fun purgeDeleted() {
        noteDao.purgeDeleted()
    }

    private fun NoteEntity.toNote(): Note {
        val decryptedContent = try {
            cryptoProvider.decrypt(encryptedData, cipherType)
                .decodeToString()
        } catch (e: CryptoException) {
            throw ObscuraDomainException.DecryptionException(
                noteId = id,
                cause = e
            )
        }

        return Note(
            id = id,
            content = decryptedContent,
            cipherType = cipherType,
            updatedAt = updatedAt,
            syncStatus = syncStatus,
            isDeleted = isDeleted
        )
    }

    private companion object {
        const val LOG_TAG = "NoteRepository"
    }
}