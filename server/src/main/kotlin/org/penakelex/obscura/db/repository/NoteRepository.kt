package org.penakelex.obscura.db.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.batchUpsert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.crypto.CipherType
import org.penakelex.obscura.db.model.Note
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.exception.validation.ValidationException
import org.penakelex.obscura.proto.NoteProto
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NoteRepository(
    private val validationConfig: ServerConfig.Validation,
) {
    private val logger =
        LoggerFactory.getLogger(NoteRepository::class.java)

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

    suspend fun upsertNotes(
        userId: Uuid,
        changes: List<NoteProto>,
    ) = withContext(Dispatchers.IO) {
        val maxSize = validationConfig.note.maxEncryptedSizeBytes

        val oversizedCount = changes.count {
            it.encryptedData.size() > maxSize
        }

        if (oversizedCount != 0) {
            logger.warn(
                "User {} sent {} oversized notes (max {} bytes)",
                userId, oversizedCount, maxSize
            )
            throw ValidationException.PayloadTooLarge(maxSize)
        }

        suspendTransaction {
            val existingIds = changes.map { Uuid.parse(it.id) }
            val existingNotes = Notes.selectAll()
                .where { Notes.id inList existingIds }
                .associate { it[Notes.id] to it[Notes.updatedAt] }

            val toUpsert = changes.filter { proto ->
                val existingUpdatedAt =
                    existingNotes[Uuid.parse(proto.id)]
                existingUpdatedAt == null
                        || proto.updatedAt > existingUpdatedAt
            }

            toUpsert.forEach {
                logger.info("To upsert notes: $it")
            }

            if (toUpsert.isEmpty()) return@suspendTransaction

            logger.info("Existing notes: $existingNotes")

            Notes.batchUpsert(
                data = toUpsert,
                keys = arrayOf(Notes.id),
                onUpdate = {
                    it[Notes.encryptedData] =
                        insertValue(Notes.encryptedData)
                    it[Notes.cipherType] =
                        insertValue(Notes.cipherType)
                    it[Notes.updatedAt] = insertValue(Notes.updatedAt)
                    it[Notes.isDeleted] = insertValue(Notes.isDeleted)
                },
            ) { proto ->
                this[Notes.id] = Uuid.parse(proto.id)
                this[Notes.userId] = userId
                this[Notes.encryptedData] =
                    proto.encryptedData.toByteArray()

                val cipherType =
                    CipherType.fromIdOrFallback(proto.cipherType)
                if (cipherType.id != proto.cipherType) {
                    logger.warn(
                        "Invalid cipher_type {} from user {}, using fallback {}",
                        proto.cipherType, userId, cipherType.id
                    )
                }
                this[Notes.cipherType] = cipherType.id
                this[Notes.updatedAt] = proto.updatedAt
                this[Notes.isDeleted] = proto.isDeleted
            }.forEach {
                val note = it.toNote()
                logger.info("Upserted note: $note")
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

    suspend fun countByUserId(
        userId: Uuid,
        includeDeleted: Boolean,
    ): Int = withContext(Dispatchers.IO) {
        suspendTransaction {
            Notes.selectAll()
                .where {
                    if (includeDeleted) {
                        Notes.userId eq userId
                    } else {
                        (Notes.userId eq userId) and
                                (Notes.isDeleted eq false)
                    }
                }
                .count()
                .toInt()
        }
    }

    suspend fun deleteAllByUserId(userId: Uuid): Int =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Notes.deleteWhere { Notes.userId eq userId }
            }
        }

    suspend fun purgeOldDeletedNotes(retentionDays: Int): Int =
        withContext(Dispatchers.IO) {
            val cutoff = Clock.System.now().toEpochMilliseconds() -
                    retentionDays.days.inWholeMilliseconds
            suspendTransaction {
                Notes.deleteWhere {
                    (Notes.isDeleted eq true) and
                            (Notes.updatedAt less cutoff)
                }
            }
        }
}