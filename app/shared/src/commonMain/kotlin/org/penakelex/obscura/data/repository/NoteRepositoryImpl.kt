package org.penakelex.obscura.data.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.penakelex.obscura.data.crypto.CryptoException
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.local.dao.NoteDao
import org.penakelex.obscura.data.local.entity.NoteEntity
import org.penakelex.obscura.data.mapper.NoteMapper.toEntities
import org.penakelex.obscura.data.mapper.NoteMapper.toSyncableNotes
import org.penakelex.obscura.domain.exception.ObscuraDomainException
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.domain.model.note.NoteSyncState
import org.penakelex.obscura.domain.model.note.NotesResult
import org.penakelex.obscura.domain.model.common.SyncStatus
import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.repository.AuthRepository
import org.penakelex.obscura.domain.repository.NoteRepository
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val cryptoProvider: CryptoProvider,
    private val authRepository: AuthRepository,
) : NoteRepository {
    private val logger = Logger.withTag(LOG_TAG)

    override fun observeNotes(): Flow<NotesResult> =
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

    override suspend fun getById(id: String): Note {
        val entity = noteDao.getById(id)
            ?: throw ObscuraDomainException.NoteNotFoundException(id)
        return entity.toNote()
    }

    override suspend fun create(
        content: String,
        cipherType: CipherType,
    ): String {
        ensureInitialized()
        val id = Uuid.random().toString()
        val encryptedData = cryptoProvider.encrypt(
            content.encodeToByteArray(),
            cipherType,
        )
        val isLocalOnly = !authRepository.isLoggedIn()

        val entity = NoteEntity(
            id = id,
            encryptedData = encryptedData,
            cipherType = cipherType,
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            syncStatus = if (isLocalOnly) SyncStatus.SYNCED else SyncStatus.PENDING,
            version = 1,
            isDeleted = false,
            isLocalOnly = isLocalOnly,
        )
        noteDao.upsert(entity)
        logger.d { "Created note: $id (cipher=$cipherType, localOnly=$isLocalOnly)" }
        return id
    }

    override suspend fun update(
        id: String,
        content: String,
        cipherType: CipherType
    ) {
        ensureInitialized()

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

    override suspend fun delete(id: String) {
        noteDao.softDelete(
            id = id,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        logger.d { "Soft-deleted note: $id" }
    }

    override suspend fun getPendingChanges(): List<SyncableNote> =
        noteDao.getPendingChanges()
            .filter { !it.isLocalOnly }
            .toSyncableNotes()

    override suspend fun markSynced(id: String) {
        noteDao.markSynced(
            id = id,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
    }

    override suspend fun applyServerChanges(notes: List<SyncableNote>) {
        noteDao.upsertAll(notes.toEntities())
        logger.i { "Applied ${notes.size} server changes" }
    }

    override suspend fun getSyncStates(
        ids: List<String>,
    ): List<NoteSyncState> = noteDao.getSyncStates(ids)

    override suspend fun restore(id: String) {
        val existing = noteDao.getById(id)
            ?: throw ObscuraDomainException.NoteNotFoundException(id)

        if (!existing.isDeleted) {
            logger.d { "Note $id is not deleted, skipping restore" }
            return
        }

        noteDao.restore(
            id = id,
            timestamp = Clock.System.now().toEpochMilliseconds()
        )
        logger.d { "Restored note: $id" }
    }

    override suspend fun resolveConflict(
        id: String,
        serverEncryptedData: ByteArray,
        serverCipherType: CipherType,
        serverUpdatedAt: Long,
        serverIsDeleted: Boolean
    ) {
        noteDao.resolveConflict(
            id = id,
            encryptedData = serverEncryptedData,
            cipherType = serverCipherType,
            updatedAt = serverUpdatedAt,
            isDeleted = serverIsDeleted
        )
        logger.i {
            "Conflict resolved for note: $id (accepted server version, deleted=$serverIsDeleted)"
        }
    }

    override suspend fun purgeDeleted() {
        noteDao.purgeDeleted()
    }

    private fun ensureInitialized() {
        if (!cryptoProvider.isInitialized) {
            throw CryptoException.NotInitialized()
        }
    }

    private fun NoteEntity.toNote(): Note {
        val decryptedContent = try {
            cryptoProvider.decrypt(encryptedData, cipherType)
                .decodeToString()
        } catch (e: CryptoException) {
            e.printStackTrace()

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
            isDeleted = isDeleted,
            isLocalOnly = isLocalOnly,
        )
    }

    private companion object {
        const val LOG_TAG = "NoteRepository"
    }
}