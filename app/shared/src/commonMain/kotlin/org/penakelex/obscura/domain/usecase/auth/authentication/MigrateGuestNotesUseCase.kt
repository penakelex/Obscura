package org.penakelex.obscura.domain.usecase.auth.authentication

import co.touchlab.kermit.Logger
import org.penakelex.obscura.data.crypto.CryptoProvider
import org.penakelex.obscura.data.crypto.GuestCryptoManager
import org.penakelex.obscura.data.local.dao.NoteDao
import org.penakelex.obscura.data.local.entity.NoteEntity
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus
import kotlin.time.Clock

class MigrateGuestNotesUseCase(
    private val noteDao: NoteDao,
    private val cryptoProvider: CryptoProvider,
    private val guestCryptoManager: GuestCryptoManager,
) {
    private val logger = Logger.withTag("MigrateGuestNotes")

    data class MigrationResult(
        val migratedCount: Int,
        val failedCount: Int,
    )

    suspend fun decryptGuestNotes(): List<DecryptedGuestNote> {
        val guestNotes = noteDao.getLocalOnlyNotes()
        if (guestNotes.isEmpty()) return emptyList()

        logger.i { "Found ${guestNotes.size} guest notes to migrate" }
        return guestNotes.mapNotNull { entity ->
            try {
                val plaintext = cryptoProvider.decrypt(entity.encryptedData, entity.cipherType)
                DecryptedGuestNote(
                    id = entity.id,
                    plaintext = plaintext,
                    cipherType = entity.cipherType,
                )
            } catch (e: Exception) {
                logger.e(e) { "Failed to decrypt guest note ${entity.id}" }
                null
            }
        }
    }

    suspend fun reEncryptAndSave(notes: List<DecryptedGuestNote>): MigrationResult {
        var migrated = 0
        var failed = 0
        val now = Clock.System.now().toEpochMilliseconds()

        for (note in notes) {
            try {
                val newEncrypted = cryptoProvider.encrypt(
                    note.plaintext,
                    note.cipherType
                )

                val updatedEntity = NoteEntity(
                    id = note.id,
                    encryptedData = newEncrypted,
                    cipherType = note.cipherType,
                    updatedAt = now,
                    syncStatus = SyncStatus.PENDING,
                    version = 1,
                    isDeleted = false,
                    isLocalOnly = false,
                )

                noteDao.upsert(updatedEntity)
                migrated++
            } catch (e: Exception) {
                logger.e(e) { "Failed to re-encrypt note ${note.id}" }
                failed++
            }
        }

        guestCryptoManager.clearGuestMode()

        logger.i { "Migration complete: $migrated migrated, $failed failed" }
        return MigrationResult(migrated, failed)
    }

    data class DecryptedGuestNote(
        val id: String,
        val plaintext: ByteArray,
        val cipherType: CipherType,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is DecryptedGuestNote) return false
            return id == other.id &&
                    plaintext.contentEquals(other.plaintext) &&
                    cipherType == other.cipherType
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + plaintext.contentHashCode()
            result = 31 * result + cipherType.hashCode()
            return result
        }
    }
}