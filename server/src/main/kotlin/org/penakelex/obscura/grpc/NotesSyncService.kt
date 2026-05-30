package org.penakelex.obscura.grpc

import com.google.protobuf.ByteString
import io.grpc.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.penakelex.obscura.db.model.Note
import org.penakelex.obscura.db.repository.NoteRepository
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.exception.auth.AuthException
import org.penakelex.obscura.proto.ClientSyncPayload
import org.penakelex.obscura.proto.NoteProto
import org.penakelex.obscura.proto.SecureNotesSyncGrpcKt
import org.penakelex.obscura.proto.ServerSyncPayload
import org.penakelex.obscura.proto.SyncStatus
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NotesSyncService(
    private val noteRepository: NoteRepository,
    private val sessionRepository: SessionRepository
) : SecureNotesSyncGrpcKt.SecureNotesSyncCoroutineImplBase() {

    private val logger =
        LoggerFactory.getLogger(NotesSyncService::class.java)

    companion object {
        private const val MAX_CHANGES_PER_PAYLOAD = 500
    }

    override fun syncNotes(
        requests: Flow<ClientSyncPayload>
    ): Flow<ServerSyncPayload> = flow {
        val rawToken = GrpcContext.RAW_TOKEN_KEY.get()
            ?: throw Status.UNAUTHENTICATED
                .withDescription("Missing authorization token")
                .asRuntimeException()

        val userId = try {
            sessionRepository.findActiveByRawToken(rawToken).userId
        } catch (e: AuthException) {
            throw Status.UNAUTHENTICATED
                .withDescription(e.message)
                .asRuntimeException()
        }

        logger.info("Sync stream opened for user {}", userId)
        var payloadCount = 0

        try {
            requests.collect { clientPayload ->
                payloadCount++
                processPayload(userId, payloadCount, clientPayload)
            }
        } finally {
            logger.info(
                "Sync stream closed for user {} after {} payloads",
                userId, payloadCount
            )
        }
    }

    private suspend fun FlowCollector<ServerSyncPayload>.processPayload(
        userId: Uuid,
        payloadCount: Int,
        clientPayload: ClientSyncPayload
    ) {
        try {
            val changes = clientPayload.clientChangesList

            if (changes.size > MAX_CHANGES_PER_PAYLOAD) {
                logger.warn(
                    "Payload #{} too large from user {}: {} changes (max {})",
                    payloadCount, userId, changes.size,
                    MAX_CHANGES_PER_PAYLOAD
                )
                emit(
                    ServerSyncPayload.newBuilder()
                        .setStatus(SyncStatus.PARTIAL)
                        .setNewSyncTimestamp(
                            clientPayload.lastSyncTimestamp
                        )
                        .build()
                )
                return
            }

            if (changes.isNotEmpty()) {
                noteRepository.upsertNotes(userId, changes)
                logger.debug(
                    "Applied {} changes for user {} in payload #{}",
                    changes.size, userId, payloadCount
                )
            }

            val serverDelta = noteRepository.getDelta(
                userId = userId,
                lastSyncTimestamp = clientPayload.lastSyncTimestamp
            )

            val newTimestamp =
                Clock.System.now().toEpochMilliseconds()

            emit(
                ServerSyncPayload.newBuilder()
                    .addAllServerChanges(serverDelta.map { it.toProto() })
                    .setNewSyncTimestamp(newTimestamp)
                    .setStatus(SyncStatus.SUCCESS)
                    .build()
            )

            logger.debug(
                "Sync payload #{} completed for user {}: " +
                        "sent {} server changes",
                payloadCount, userId, serverDelta.size
            )

        } catch (e: Exception) {
            logger.error(
                "Sync error for user {} in payload #{}: {}",
                userId, payloadCount, e.message, e
            )
            emit(
                ServerSyncPayload.newBuilder()
                    .setStatus(SyncStatus.PARTIAL)
                    .setNewSyncTimestamp(
                        clientPayload.lastSyncTimestamp
                    )
                    .build()
            )
        }
    }

    private fun Note.toProto(): NoteProto =
        NoteProto.newBuilder()
            .setId(id.toString())
            .setEncryptedData(ByteString.copyFrom(encryptedData))
            .setCipherType(cipherType)
            .setUpdatedAt(updatedAt)
            .setIsDeleted(isDeleted)
            .build()
}