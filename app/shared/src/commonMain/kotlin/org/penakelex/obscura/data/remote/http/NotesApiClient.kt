package org.penakelex.obscura.data.remote.http

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import kotlinx.serialization.json.Json
import org.penakelex.obscura.contract.rest.requests.sync.SyncRequest
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse
import org.penakelex.obscura.contract.rest.responses.notes.NotesListResponse
import org.penakelex.obscura.contract.rest.responses.sync.DeltaResponse
import org.penakelex.obscura.contract.rest.responses.sync.SyncResponse
import org.penakelex.obscura.data.remote.config.NetworkConfig
import org.penakelex.obscura.data.storage.TokenStorage

class NotesApiClient(
    client: HttpClient,
    private val tokenStorage: TokenStorage,
    baseUrl: String = NetworkConfig.Rest.BASE_URL,
    json: Json = Json { ignoreUnknownKeys = true }
) : BaseApiClient(client, baseUrl, json) {
    private val paths = NetworkConfig.Rest.Paths

    suspend fun listNotes(
        limit: Int? = null,
        offset: Int? = null,
        includeDeleted: Boolean = false
    ): NotesListResponse = execute {
        client.get("$baseUrl${paths.NOTES_LIST}") {
            bearerAuth(requireToken())
            limit?.let { parameter("limit", it) }
            offset?.let { parameter("offset", it) }
            parameter("includeDeleted", includeDeleted)
        }
    }

    suspend fun getNoteById(noteId: String): NoteResponse = execute {
        client.get("$baseUrl${paths.NOTES_LIST}/$noteId") {
            bearerAuth(requireToken())
        }
    }

    suspend fun sync(request: SyncRequest): SyncResponse = execute {
        client.post("$baseUrl${paths.NOTES_SYNC}") {
            bearerAuth(requireToken())
            jsonBody(request)
        }
    }

    suspend fun getDelta(since: Long): DeltaResponse = execute {
        client.get("$baseUrl${paths.NOTES_DELTA}") {
            bearerAuth(requireToken())
            parameter("since", since)
        }
    }

    private fun requireToken(): String =
        tokenStorage.sessionFlow.value?.token
            ?: throw IllegalStateException("Not authenticated")
}