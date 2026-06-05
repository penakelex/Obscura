package org.penakelex.obscura.data.sync

import org.penakelex.obscura.contract.rest.requests.sync.NoteChange
import org.penakelex.obscura.contract.rest.requests.sync.SyncRequest
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse
import org.penakelex.obscura.data.remote.http.ApiException
import org.penakelex.obscura.data.remote.http.NotesApiClient
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.gateway.NotesGateway
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus
import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.model.sync.NotesSyncResult
import kotlin.io.encoding.Base64

class NotesGatewayImpl(
    private val notesApiClient: NotesApiClient
) : NotesGateway {
    override suspend fun sync(
        localChanges: List<SyncableNote>,
        lastSyncTimestamp: Long,
    ): NotesSyncResult = wrapApiExceptions {
        val request = SyncRequest(
            lastSyncTimestamp = lastSyncTimestamp,
            changes = localChanges.map { it.toNoteChange() }
        )
        val response = notesApiClient.sync(request)

        println("Sync response: $response")

        NotesSyncResult(
            serverChanges = response.serverChanges.map {
                it.toSyncableNote()
            },
            newSyncTimestamp = response.newSyncTimestamp,
        )
    }

    override suspend fun listNotes(
        limit: Int?,
        offset: Int?,
        includeDeleted: Boolean,
    ): List<SyncableNote> = wrapApiExceptions {
        val response = notesApiClient.listNotes(
            limit = limit,
            offset = offset,
            includeDeleted = includeDeleted
        )
        response.notes.map { it.toSyncableNote() }
    }

    override suspend fun getNoteById(noteId: String): SyncableNote =
        wrapApiExceptions {
            val response = notesApiClient.getNoteById(noteId)
            response.toSyncableNote()
        }

    override suspend fun getDelta(since: Long): List<SyncableNote> =
        wrapApiExceptions {
            val response = notesApiClient.getDelta(since)
            response.notes.map { it.toSyncableNote() }
        }

    private inline fun <T> wrapApiExceptions(block: () -> T): T =
        try {
            block()
        } catch (e: ApiException) {
            throw mapApiException(e)
        }

    private fun mapApiException(e: ApiException): SyncException =
        when (e) {
            is ApiException.Unauthorized ->
                SyncException.Unauthenticated(e)

            is ApiException.Network ->
                SyncException.ServerUnavailable(e.cause ?: e)

            is ApiException.Server ->
                when (e.statusCode) {
                    in 500..599 -> SyncException.ServerUnavailable(e)
                    else -> SyncException.Unknown(e)
                }

            is ApiException.BadRequest ->
                SyncException.InvalidPayload(e)

            is ApiException.Conflict,
            is ApiException.NotFound,
            is ApiException.Unknown ->
                SyncException.Unknown(e)
        }

    private fun SyncableNote.toNoteChange(): NoteChange = NoteChange(
        id = id,
        encryptedData = Base64.encode(encryptedData),
        cipherType = cipherType.id,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )

    private fun NoteResponse.toSyncableNote(): SyncableNote =
        SyncableNote(
            id = id,
            encryptedData = Base64.decode(encryptedData),
            cipherType = CipherType.fromIdOrFallback(cipherType),
            updatedAt = updatedAt,
            isDeleted = isDeleted,
            version = 1,
            syncStatus = SyncStatus.SYNCED
        )
}