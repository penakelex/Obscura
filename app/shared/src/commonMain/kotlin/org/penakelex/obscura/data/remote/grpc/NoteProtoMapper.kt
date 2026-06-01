package org.penakelex.obscura.data.remote.grpc

import com.google.protobuf.ByteString
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus
import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.proto.NoteProto

object NoteProtoMapper {
    fun SyncableNote.toProto(): NoteProto = NoteProto.newBuilder()
        .setId(id)
        .setEncryptedData(ByteString.copyFrom(encryptedData))
        .setCipherType(cipherType.id)
        .setUpdatedAt(updatedAt)
        .setIsDeleted(isDeleted)
        .build()

    fun NoteProto.toDomain(): SyncableNote = SyncableNote(
        id = id,
        encryptedData = encryptedData.toByteArray(),
        cipherType = CipherType.fromIdOrFallback(cipherType),
        updatedAt = updatedAt,
        isDeleted = isDeleted,
        version = 1,
        syncStatus = SyncStatus.SYNCED
    )

    fun List<SyncableNote>.toProtoList(): List<NoteProto> =
        map { it.toProto() }

    fun List<NoteProto>.toDomainList(): List<SyncableNote> =
        map { it.toDomain() }
}