package org.penakelex.obscura.data.mapper

import org.penakelex.obscura.data.local.entity.NoteEntity
import org.penakelex.obscura.domain.model.note.SyncableNote

object NoteMapper {
    fun NoteEntity.toSyncableNote(): SyncableNote = SyncableNote(
        id = id,
        encryptedData = encryptedData,
        cipherType = cipherType,
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        version = version,
        syncStatus = syncStatus,
        isLocalOnly = isLocalOnly,
    )

    fun SyncableNote.toEntity(): NoteEntity = NoteEntity(
        id = id,
        encryptedData = encryptedData,
        cipherType = cipherType,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        version = version,
        isDeleted = isDeleted,
        isLocalOnly = isLocalOnly,
    )

    fun List<NoteEntity>.toSyncableNotes(): List<SyncableNote> =
        map { it.toSyncableNote() }

    fun List<SyncableNote>.toEntities(): List<NoteEntity> =
        map { it.toEntity() }
}