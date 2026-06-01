package org.penakelex.obscura.data.sync

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import org.penakelex.obscura.data.remote.grpc.SyncApiClient
import org.penakelex.obscura.domain.exception.SyncException
import org.penakelex.obscura.domain.gateway.SyncGateway
import org.penakelex.obscura.domain.model.note.SyncableNote
import org.penakelex.obscura.domain.model.sync.SyncRequest
import org.penakelex.obscura.domain.model.sync.SyncResult

class SyncGatewayImpl(
    private val syncApiClient: SyncApiClient,
) : SyncGateway {
    override suspend fun sync(
        localChanges: List<SyncableNote>,
        lastSyncTimestamp: Long
    ): SyncResult {
        val request = SyncRequest(
            localChanges = localChanges,
            lastSyncTimestamp = lastSyncTimestamp,
        )

        return try {
            syncApiClient
                .sync(flowOf(request))
                .single()
        } catch (_: NoSuchElementException) {
            throw SyncException.ServerUnavailable(
                IllegalStateException(
                    "Sync stream closed without response"
                )
            )
        }
    }
}