package org.penakelex.obscura.db.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greater
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.db.model.Note
import org.penakelex.obscura.proto.NoteProto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object NoteRepository {
    fun upsertNotes(userId: Uuid, changes: List<NoteProto>) =
        transaction {
            if (changes.isEmpty()) {
                return@transaction
            }

            val incomingIds = changes.map { Uuid.parse(it.id) }

            val existingNotes = Notes.selectAll()
                .where {
                    (Notes.id inList incomingIds) and
                            (Notes.userId eq userId)
                }
                .associate { row ->
                    row[Notes.id] to row[Notes.updatedAt]
                }

            val toInsert = mutableListOf<NoteProto>()
            val toUpdate = mutableListOf<NoteProto>()

            changes.forEach { proto ->
                val noteId = Uuid.parse(proto.id)
                val existingUpdatedAt = existingNotes[noteId]

                if (existingUpdatedAt == null) {
                    toInsert.add(proto)
                } else if (proto.updatedAt > existingUpdatedAt) {
                    toUpdate.add(proto)
                }
            }

            if (toInsert.isNotEmpty()) {
                Notes.batchInsert(toInsert) { proto ->
                    this[Notes.id] = Uuid.parse(proto.id)
                    this[Notes.userId] = userId
                    this[Notes.encryptedData] =
                        proto.encryptedData.toByteArray()
                    this[Notes.cipherType] =
                        proto.cipherType.takeIf { it > 0 }
                            ?: ServerConfig.security.defaultCipherType
                    this[Notes.updatedAt] = proto.updatedAt
                    this[Notes.isDeleted] = proto.isDeleted
                }
            }

            toUpdate.forEach { proto ->
                val noteId = Uuid.parse(proto.id)
                Notes.update({ Notes.id eq noteId }) {
                    it[encryptedData] =
                        proto.encryptedData.toByteArray()
                    it[cipherType] =
                        proto.cipherType.takeIf { c -> c > 0 }
                            ?: ServerConfig.security.defaultCipherType
                    it[updatedAt] = proto.updatedAt
                    it[isDeleted] = proto.isDeleted
                }
            }
        }

    fun getDelta(userId: Uuid, lastSyncTimestamp: Long): List<Note> =
        transaction {
            Notes.selectAll()
                .where {
                    (Notes.userId eq userId) and
                            (Notes.updatedAt greater lastSyncTimestamp)
                }
                .map { row ->
                    Note(
                        id = row[Notes.id],
                        userId = row[Notes.userId],
                        encryptedData = row[Notes.encryptedData],
                        cipherType = row[Notes.cipherType],
                        updatedAt = row[Notes.updatedAt],
                        isDeleted = row[Notes.isDeleted]
                    )
                }
        }
}