package org.penakelex.obscura.domain.repository

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.domain.model.note.NoteSyncState
import org.penakelex.obscura.domain.model.note.NotesResult
import org.penakelex.obscura.domain.model.note.SyncableNote

interface NoteRepository {
    fun observeNotes(): Flow<NotesResult>
    suspend fun getById(id: String): Note
    suspend fun create(
        content: String,
        cipherType: CipherType = CipherType.DEFAULT
    ): String
    suspend fun update(
        id: String,
        content: String,
        cipherType: CipherType
    )
    suspend fun delete(id: String)
    suspend fun getPendingChanges(): List<SyncableNote>
    suspend fun getPendingCount(): Int
    suspend fun markSynced(id: String)
    suspend fun applyServerChanges(notes: List<SyncableNote>)
    suspend fun getSyncStates(ids: List<String>): List<NoteSyncState>
    suspend fun restore(id: String)
    suspend fun resolveConflict(
        id: String,
        serverEncryptedData: ByteArray,
        serverCipherType: CipherType,
        serverUpdatedAt: Long,
        serverIsDeleted: Boolean
    )

    suspend fun purgeDeleted(): Int
    suspend fun clearAll()
}