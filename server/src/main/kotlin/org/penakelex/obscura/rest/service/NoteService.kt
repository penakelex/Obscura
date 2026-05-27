package org.penakelex.obscura.rest.service

import org.penakelex.obscura.contract.rest.requests.sync.SyncRequest
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse
import org.penakelex.obscura.contract.rest.responses.notes.NotesListResponse
import org.penakelex.obscura.contract.rest.responses.sync.DeltaResponse
import org.penakelex.obscura.contract.rest.responses.sync.SyncResponse
import org.penakelex.obscura.db.model.Note
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.exception.resource.NotFoundException
import org.penakelex.obscura.proto.NoteProto
import com.google.protobuf.ByteString
import org.penakelex.obscura.contract.rest.requests.sync.NoteChange
import java.util.Base64
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NoteService(
    private val noteRepository: NoteRepository
) {
    suspend fun listNotes(
        userId: Uuid,
        limit: Int,
        offset: Int,
        includeDeleted: Boolean
    ): NotesListResponse {
        val validatedLimit = limit.coerceIn(1, MAX_PAGE_SIZE)
        val validatedOffset = offset.coerceAtLeast(0)

        val page = noteRepository.findAllByUserId(
            userId = userId,
            limit = validatedLimit,
            offset = validatedOffset,
            includeDeleted = includeDeleted
        )

        return NotesListResponse(
            notes = page.notes.map { it.toResponse() },
            totalCount = page.totalCount,
            limit = validatedLimit,
            offset = validatedOffset,
            hasMore = validatedOffset + validatedLimit < page.totalCount
        )
    }

    suspend fun getNoteById(
        userId: Uuid,
        noteId: String
    ): NoteResponse {
        val uuid = parseUuidOrThrow(noteId)
        val note = noteRepository.findByIdAndUser(uuid, userId)
            ?: throw NotFoundException.NoteNotFound(noteId)
        return note.toResponse()
    }

    suspend fun sync(
        userId: Uuid,
        request: SyncRequest
    ): SyncResponse {
        val protos = request.changes.map { it.toProto() }

        val beforeCount = countUserNotes(userId)
        noteRepository.upsertNotes(userId, protos)
        val afterCount = countUserNotes(userId)

        val appliedCount = request.changes.size
        val conflictsResolved =
            (beforeCount + appliedCount - afterCount)
                .coerceAtLeast(0)

        val serverChanges = noteRepository.getDelta(
            userId = userId,
            lastSyncTimestamp = request.lastSyncTimestamp
        )

        val newTimestamp = Clock.System.now().toEpochMilliseconds()

        return SyncResponse(
            serverChanges = serverChanges.map { it.toResponse() },
            newSyncTimestamp = newTimestamp,
            appliedCount = appliedCount,
            conflictsResolved = conflictsResolved
        )
    }

    suspend fun getDelta(userId: Uuid, since: Long): DeltaResponse {
        val notes = noteRepository.getDelta(userId, since)
        return DeltaResponse(
            notes = notes.map { it.toResponse() },
            serverTimestamp = Clock.System.now()
                .toEpochMilliseconds(),
            sinceTimestamp = since
        )
    }

    private suspend fun countUserNotes(userId: Uuid): Int =
        noteRepository
            .findAllByUserId(
                userId,
                limit = 1,
                offset = 0,
                includeDeleted = true,
            )
            .totalCount

    private fun parseUuidOrThrow(value: String): Uuid =
        Uuid.parseOrNull(value)
            ?: throw NotFoundException.NoteNotFound(value)

    private fun Note.toResponse(): NoteResponse = NoteResponse(
        id = id.toString(),
        encryptedData = Base64.getEncoder()
            .encodeToString(encryptedData),
        cipherType = cipherType,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    private fun NoteChange.toProto(): NoteProto =
        NoteProto.newBuilder()
            .setId(id)
            .setEncryptedData(
                ByteString.copyFrom(
                    Base64.getDecoder().decode(encryptedData)
                )
            )
            .setCipherType(cipherType)
            .setUpdatedAt(updatedAt)
            .setIsDeleted(isDeleted)
            .build()

    companion object {
        private const val MAX_PAGE_SIZE = 100
        const val DEFAULT_PAGE_SIZE = 50
    }
}