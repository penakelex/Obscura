package org.penakelex.obscura.db.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.model.Note
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.proto.NoteProto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NoteRepository(
    private val securityConfig: ServerConfig.Security
) {
    data class NotesPage(
        val notes: List<Note>,
        val totalCount: Int
    )

    private fun ResultRow.toNote(): Note = Note(
        id = this[Notes.id],
        userId = this[Notes.userId],
        encryptedData = this[Notes.encryptedData],
        cipherType = this[Notes.cipherType],
        updatedAt = this[Notes.updatedAt],
        isDeleted = this[Notes.isDeleted]
    )

    suspend fun upsertNotes(userId: Uuid, changes: List<NoteProto>) =
        withContext(Dispatchers.IO) {
            if (changes.isEmpty()) {
                return@withContext
            }

            suspendTransaction {
                Notes.batchUpsert(
                    data = changes,
                    keys = arrayOf(Notes.id),
                    onUpdate = {
                        it[Notes.encryptedData] = Notes.encryptedData
                        it[Notes.cipherType] = Notes.cipherType
                        it[Notes.updatedAt] = Notes.updatedAt
                        it[Notes.isDeleted] = Notes.isDeleted
                    },
                    where = {
                        (Notes.userId eq userId) and
                                (Notes.updatedAt less Notes.updatedAt)
                    }
                ) { proto ->
                    this[Notes.id] = Uuid.parse(proto.id)
                    this[Notes.userId] = userId
                    this[Notes.encryptedData] =
                        proto.encryptedData.toByteArray()
                    this[Notes.cipherType] =
                        proto.cipherType.takeIf { it > 0 }
                            ?: securityConfig.defaultCipherType
                    this[Notes.updatedAt] = proto.updatedAt
                    this[Notes.isDeleted] = proto.isDeleted
                }
            }
        }

    suspend fun getDelta(
        userId: Uuid,
        lastSyncTimestamp: Long
    ): List<Note> =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Notes.selectAll()
                    .where {
                        (Notes.userId eq userId) and
                                (Notes.updatedAt greater lastSyncTimestamp)
                    }
                    .map { it.toNote() }
            }
        }

    suspend fun findAllByUserId(
        userId: Uuid,
        limit: Int,
        offset: Int,
        includeDeleted: Boolean
    ): NotesPage = withContext(Dispatchers.IO) {
        suspendTransaction {
            val baseQuery = Notes.selectAll().where {
                if (includeDeleted) {
                    Notes.userId eq userId
                } else {
                    (Notes.userId eq userId) and
                            (Notes.isDeleted eq false)
                }
            }

            val totalCount = baseQuery.count().toInt()

            val notes = baseQuery
                .orderBy(Notes.updatedAt to SortOrder.DESC)
                .limit(limit)
                .offset(offset.toLong())
                .map { it.toNote() }

            NotesPage(notes, totalCount)
        }
    }

    suspend fun findByIdAndUser(
        noteId: Uuid,
        userId: Uuid
    ): Note? = withContext(Dispatchers.IO) {
        suspendTransaction {
            Notes.selectAll()
                .where {
                    (Notes.id eq noteId) and (Notes.userId eq userId)
                }
                .singleOrNull()
                ?.toNote()
        }
    }

    suspend fun deleteAllByUserId(userId: Uuid): Int =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Notes.deleteWhere { Notes.userId eq userId }
            }
        }
}