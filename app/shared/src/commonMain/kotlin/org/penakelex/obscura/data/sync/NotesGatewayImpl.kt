package org.penakelex.obscura.data.sync

import org.penakelex.obscura.contract.rest.requests.sync.NoteChange
import org.penakelex.obscura.contract.rest.requests.sync.SyncRequest
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse
import org.penakelex.obscura.data.remote.http.NotesApiClient
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
        lastSyncTimestamp: Long
    ): NotesSyncResult {
        val request = SyncRequest(
            lastSyncTimestamp = lastSyncTimestamp,
            changes = localChanges.map { it.toNoteChange() }
        )

        val response = notesApiClient.sync(request)

        return NotesSyncResult(
            serverChanges = response.serverChanges.map {
                it.toSyncableNote()
            },
            newSyncTimestamp = response.newSyncTimestamp
        )
    }

    override suspend fun listNotes(
        limit: Int?,
        offset: Int?,
        includeDeleted: Boolean
    ): List<SyncableNote> {
        val response = notesApiClient.listNotes(
            limit = limit,
            offset = offset,
            includeDeleted = includeDeleted
        )
        return response.notes.map { it.toSyncableNote() }
    }

    override suspend fun getNoteById(noteId: String): SyncableNote {
        val response = notesApiClient.getNoteById(noteId)
        return response.toSyncableNote()
    }

    override suspend fun getDelta(since: Long): List<SyncableNote> {
        val response = notesApiClient.getDelta(since)
        return response.notes.map { it.toSyncableNote() }
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