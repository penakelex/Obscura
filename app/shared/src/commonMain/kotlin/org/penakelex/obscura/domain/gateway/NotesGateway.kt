package org.penakelex.obscura.domain.gateway

import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.model.sync.NotesSyncResult

interface NotesGateway {
    suspend fun sync(
        localChanges: List<SyncableNote>,
        lastSyncTimestamp: Long
    ): NotesSyncResult

    suspend fun listNotes(
        limit: Int? = null,
        offset: Int? = null,
        includeDeleted: Boolean = false
    ): List<SyncableNote>

    suspend fun getNoteById(noteId: String): SyncableNote
    suspend fun getDelta(since: Long): List<SyncableNote>
}