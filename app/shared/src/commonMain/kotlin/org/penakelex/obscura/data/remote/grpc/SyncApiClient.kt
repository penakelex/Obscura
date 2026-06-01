package org.penakelex.obscura.data.remote.grpc

import co.touchlab.kermit.Logger
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import org.penakelex.obscura.data.remote.grpc.NoteProtoMapper.toDomainList
import org.penakelex.obscura.data.remote.grpc.NoteProtoMapper.toProtoList
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.model.sync.SyncRequest
import org.penakelex.obscura.domain.model.sync.SyncResult
import org.penakelex.obscura.domain.model.sync.SyncResultStatus
import org.penakelex.obscura.proto.ClientSyncPayload
import org.penakelex.obscura.proto.SecureNotesSyncGrpcKt
import org.penakelex.obscura.proto.ServerSyncPayload
import org.penakelex.obscura.proto.SyncStatus

class SyncApiClient(
    private val channel: ManagedChannel
) : AutoCloseable {
    private val logger = Logger.withTag(LOG_TAG)

    private val stub by lazy {
        SecureNotesSyncGrpcKt.SecureNotesSyncCoroutineStub(channel)
    }

    fun sync(requests: Flow<SyncRequest>): Flow<SyncResult> =
        stub.syncNotes(requests.map { it.toProto() })
            .map { it.toDomain() }
            .onStart {
                logger.d { "gRPC sync stream opened" }
            }
            .onCompletion { cause ->
                if (cause == null) {
                    logger.d { "gRPC sync stream closed normally" }
                } else {
                    logger.w(cause) {
                        "gRPC sync stream closed with error"
                    }
                }
            }
            .catch { e ->
                throw mapGrpcException(e)
            }

    fun shutdown() {
        if (!channel.isShutdown) {
            channel.shutdown()
            logger.i { "gRPC channel shut down" }
        }
    }

    override fun close() = shutdown()

    private fun SyncRequest.toProto(): ClientSyncPayload =
        ClientSyncPayload.newBuilder()
            .addAllClientChanges(localChanges.toProtoList())
            .setLastSyncTimestamp(lastSyncTimestamp)
            .build()

    private fun ServerSyncPayload.toDomain(): SyncResult = SyncResult(
        serverChanges = serverChangesList.toDomainList(),
        newSyncTimestamp = newSyncTimestamp,
        status = status.toDomain()
    )

    private fun SyncStatus.toDomain(): SyncResultStatus = when (this) {
        SyncStatus.SUCCESS -> SyncResultStatus.SUCCESS
        SyncStatus.PARTIAL -> SyncResultStatus.PARTIAL
        SyncStatus.CONFLICT_RESOLVED -> SyncResultStatus.CONFLICT_RESOLVED
        SyncStatus.AUTH_ERROR -> SyncResultStatus.AUTH_ERROR
        SyncStatus.UNRECOGNIZED -> SyncResultStatus.PARTIAL
    }

    private fun mapGrpcException(e: Throwable): SyncException =
        when (e) {
            is StatusRuntimeException -> when (e.status.code) {
                Status.Code.UNAUTHENTICATED ->
                    SyncException.Unauthenticated(e)
                Status.Code.UNAVAILABLE ->
                    SyncException.ServerUnavailable(e)
                Status.Code.DEADLINE_EXCEEDED ->
                    SyncException.Timeout(e)
                Status.Code.INVALID_ARGUMENT ->
                    SyncException.InvalidPayload(e)
                else -> SyncException.Unknown(e)
            }
            else -> SyncException.Unknown(e)
        }

    private companion object {
        const val LOG_TAG = "SyncApiClient"
    }
}