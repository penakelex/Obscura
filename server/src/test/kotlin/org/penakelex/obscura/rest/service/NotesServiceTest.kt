package org.penakelex.obscura.rest.service

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.runTest
import org.penakelex.obscura.cleanupDatabase
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.requests.auth.LoginRequest
import org.penakelex.obscura.contract.rest.requests.auth.RegisterRequest
import org.penakelex.obscura.contract.rest.requests.sync.NoteChange
import org.penakelex.obscura.contract.rest.requests.sync.SyncRequest
import org.penakelex.obscura.contract.rest.responses.auth.LoginResponse
import org.penakelex.obscura.contract.rest.responses.notes.NoteResponse
import org.penakelex.obscura.contract.rest.responses.notes.NotesListResponse
import org.penakelex.obscura.contract.rest.responses.sync.DeltaResponse
import org.penakelex.obscura.contract.rest.responses.sync.SyncResponse
import org.penakelex.obscura.setupTestApp
import java.util.Base64
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotesServiceTest {
    @AfterTest
    fun tearDown() {
        cleanupDatabase()
    }

    private suspend fun registerAndLogin(
        client: io.ktor.client.HttpClient,
        email: String = "notes@example.com"
    ): String {
        client.post("/api/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, "SecurePass123"))
        }
        val response = client.post("/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, "SecurePass123"))
        }
        return response.body<LoginResponse>().token
    }

    private fun encodeData(text: String): String =
        Base64.getEncoder().encodeToString(text.toByteArray())

    @Test
    fun `list notes returns paginated results`() = runTest {
        testApplication {
            val client = setupTestApp()
            val token = registerAndLogin(client)

            val changes = (1..3).map { i ->
                NoteChange(
                    id = "00000000-0000-0000-0000-00000000000$i",
                    encryptedData = encodeData("Note $i"),
                    cipherType = 1,
                    updatedAt = System.currentTimeMillis() + i,
                    isDeleted = false
                )
            }
            client.post("/api/notes/sync") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    SyncRequest(
                        lastSyncTimestamp = 0L,
                        changes = changes
                    )
                )
            }

            val listResponse =
                client.get("/api/notes?limit=2&offset=0") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, listResponse.status)
            val body = listResponse.body<NotesListResponse>()
            assertEquals(2, body.notes.size)
            assertEquals(3, body.totalCount)
            assertTrue(body.hasMore)
        }
    }

    @Test
    fun `list notes without auth returns 401`() = runTest {
        testApplication {
            val client = setupTestApp()
            val response = client.get("/api/notes")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    fun `get note by id returns correct note`() = runTest {
        testApplication {
            val client = setupTestApp()
            val token = registerAndLogin(client)
            val noteId = "11111111-1111-1111-1111-111111111111"

            client.post("/api/notes/sync") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    SyncRequest(
                        lastSyncTimestamp = 0L,
                        changes = listOf(
                            NoteChange(
                                id = noteId,
                                encryptedData = encodeData("Secret note"),
                                cipherType = 1,
                                updatedAt = System.currentTimeMillis(),
                                isDeleted = false
                            )
                        )
                    )
                )
            }

            val response = client.get("/api/notes/$noteId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val note = response.body<NoteResponse>()
            assertEquals(noteId, note.id)
            assertFalse(note.isDeleted)
        }
    }

    @Test
    fun `get non-existent note returns 404`() = runTest {
        testApplication {
            val client = setupTestApp()
            val token = registerAndLogin(client)

            val response =
                client.get("/api/notes/99999999-9999-9999-9999-999999999999") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.NotFound, response.status)
            val error =
                response.body<org.penakelex.obscura.contract.rest.responses.common.ErrorResponse>()
            assertEquals(
                ErrorCodes.Resources.NOTE_NOT_FOUND,
                error.code
            )
        }
    }

    @Test
    fun `sync applies changes and returns server delta`() = runTest {
        testApplication {
            val client = setupTestApp()
            val token = registerAndLogin(client)
            val noteId = "22222222-2222-2222-2222-222222222222"

            val syncResponse = client.post("/api/notes/sync") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    SyncRequest(
                        lastSyncTimestamp = 0L,
                        changes = listOf(
                            NoteChange(
                                id = noteId,
                                encryptedData = encodeData("Synced note"),
                                cipherType = 1,
                                updatedAt = System.currentTimeMillis(),
                                isDeleted = false
                            )
                        )
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, syncResponse.status)
            val body = syncResponse.body<SyncResponse>()
            assertEquals(1, body.appliedCount)
            assertTrue(body.newSyncTimestamp > 0L)
        }
    }

    @Test
    fun `sync with empty changes returns empty delta`() = runTest {
        testApplication {
            val client = setupTestApp()
            val token = registerAndLogin(client)

            val syncResponse = client.post("/api/notes/sync") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    SyncRequest(
                        lastSyncTimestamp = 0L,
                        changes = emptyList()
                    )
                )
            }
            assertEquals(HttpStatusCode.OK, syncResponse.status)
            val body = syncResponse.body<SyncResponse>()
            assertEquals(0, body.appliedCount)
            assertTrue(body.serverChanges.isEmpty())
        }
    }

    @Test
    fun `delta returns notes newer than timestamp`() = runTest {
        testApplication {
            val client = setupTestApp()
            val token = registerAndLogin(client)
            val now = System.currentTimeMillis()

            client.post("/api/notes/sync") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    SyncRequest(
                        lastSyncTimestamp = 0L,
                        changes = listOf(
                            NoteChange(
                                id = "33333333-3333-3333-3333-333333333333",
                                encryptedData = encodeData("Delta note"),
                                cipherType = 1,
                                updatedAt = now,
                                isDeleted = false
                            )
                        )
                    )
                )
            }

            val deltaResponse =
                client.get("/api/notes/delta?since=${now - 1000}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, deltaResponse.status)
            val body = deltaResponse.body<DeltaResponse>()
            assertEquals(1, body.notes.size)
            assertEquals(now - 1000, body.sinceTimestamp)

            val emptyDelta =
                client.get("/api/notes/delta?since=${now + 1000}") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(HttpStatusCode.OK, emptyDelta.status)
            assertTrue(emptyDelta.body<DeltaResponse>().notes.isEmpty())
        }
    }
}