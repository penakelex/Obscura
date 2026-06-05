package org.penakelex.obscura.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus
import org.penakelex.obscura.data.local.entity.NoteEntity
import org.penakelex.obscura.domain.model.note.NoteSyncState

@Dao
interface NoteDao {
    @Query(
        """
        SELECT * FROM notes
        WHERE isDeleted = 0
        ORDER BY updatedAt DESC
        """
    )
    fun observeActiveNotes(): Flow<List<NoteEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM notes 
        WHERE isLocalOnly = 1 AND isDeleted = 0
        """
    )
    fun observeLocalOnlyCount(): Flow<Int>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getById(id: String): NoteEntity?

    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE syncStatus != :syncedStatus
        ORDER BY updatedAt ASC
        """
    )
    suspend fun getPendingChanges(
        syncedStatus: SyncStatus = SyncStatus.SYNCED,
    ): List<NoteEntity>

    @Query(
        """
    SELECT COUNT(*) FROM notes
    WHERE syncStatus != :syncedStatus AND isLocalOnly = 0
    """
    )
    suspend fun getPendingCount(
        syncedStatus: SyncStatus = SyncStatus.SYNCED
    ): Int

    @Query(
        """
        SELECT id, syncStatus, updatedAt, isDeleted FROM notes
        WHERE id IN (:ids)
        """
    )
    suspend fun getSyncStates(ids: List<String>): List<NoteSyncState>

    @Query(
        "SELECT * FROM notes WHERE isLocalOnly = 1 AND isDeleted = 0"
    )
    suspend fun getLocalOnlyNotes(): List<NoteEntity>

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Transaction
    @Upsert
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Query(
        """
        UPDATE notes
        SET isDeleted = 1, syncStatus = :pendingStatus,
        updatedAt = :timestamp, version = version + 1
        WHERE id = :id
    """
    )
    suspend fun softDelete(
        id: String,
        timestamp: Long,
        pendingStatus: SyncStatus = SyncStatus.PENDING
    )

    @Query(
        """
        UPDATE notes
        SET syncStatus = :syncedStatus, updatedAt = :timestamp
        WHERE id = :id
    """
    )
    suspend fun markSynced(
        id: String,
        timestamp: Long,
        syncedStatus: SyncStatus = SyncStatus.SYNCED
    )

    @Query(
        """
        UPDATE notes
        SET isLocalOnly = 0, syncStatus = :pendingStatus, 
            updatedAt = :timestamp
        WHERE id = :id
        """
    )
    suspend fun markAsSyncable(
        id: String,
        timestamp: Long,
        pendingStatus: SyncStatus = SyncStatus.PENDING
    )

    @Query(
        """
        UPDATE notes
        SET isDeleted = 0, syncStatus = :pendingStatus,
            updatedAt = :timestamp, version = version + 1
        WHERE id = :id
        """
    )
    suspend fun restore(
        id: String,
        timestamp: Long,
        pendingStatus: SyncStatus = SyncStatus.PENDING,
    )

    @Transaction
    @Query(
        """
        UPDATE notes
        SET encryptedData = :encryptedData,
            cipherType = :cipherType,
            updatedAt = :updatedAt,
            isDeleted = :isDeleted,
            syncStatus = :syncedStatus,
            version = version + 1
        WHERE id = :id
        """
    )
    suspend fun resolveConflict(
        id: String,
        encryptedData: ByteArray,
        cipherType: CipherType,
        updatedAt: Long,
        isDeleted: Boolean,
        syncedStatus: SyncStatus = SyncStatus.SYNCED
    )

    @Query(
        """
        DELETE FROM notes
        WHERE isDeleted = 1 AND syncStatus = :syncedStatus
        """
    )
    suspend fun purgeDeleted(
        syncedStatus: SyncStatus = SyncStatus.SYNCED,
    ): Int

    @Transaction
    @Query("DELETE FROM notes")
    suspend fun deleteAll()
}