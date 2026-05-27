package org.penakelex.obscura.rest.service

import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.runTest
import org.penakelex.obscura.cleanupDatabase
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.requests.account.DeleteAccountRequest
import org.penakelex.obscura.contract.rest.requests.auth.LoginRequest
import org.penakelex.obscura.contract.rest.requests.auth.RegisterRequest
import org.penakelex.obscura.contract.rest.responses.auth.LoginResponse
import org.penakelex.obscura.contract.rest.responses.auth.SessionsListResponse
import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse
import org.penakelex.obscura.setupTestApp
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthAccountSessionsTest {
    @AfterTest
    fun tearDown() {
        cleanupDatabase()
    }

    @Test
    fun `delete account with valid password returns 200 and removes user`() =
        runTest {
            testApplication {
                val client = setupTestApp()
                val email = "delete-me@example.com"
                val password = "SecurePass123"

                client.post("/api/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(RegisterRequest(email, password))
                }
                val loginResp = client.post("/api/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(LoginRequest(email, password))
                }
                val token = loginResp.body<LoginResponse>().token

                val deleteResp = client.delete("/api/auth/account") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(DeleteAccountRequest(password))
                }
                assertEquals(HttpStatusCode.OK, deleteResp.status)

                val loginAfterDelete =
                    client.post("/api/auth/login") {
                        contentType(ContentType.Application.Json)
                        setBody(LoginRequest(email, password))
                    }
                assertEquals(
                    HttpStatusCode.Unauthorized,
                    loginAfterDelete.status
                )
            }
        }

    @Test
    fun `delete account with wrong password returns 401`() = runTest {
        testApplication {
            val client = setupTestApp()
            val email = "no-delete@example.com"

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, "CorrectPass1"))
            }
            val loginResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, "CorrectPass1"))
            }
            val token = loginResponse.body<LoginResponse>().token

            val deleteResponse = client.delete("/api/auth/account") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(DeleteAccountRequest("WrongPassword1"))
            }
            assertEquals(
                HttpStatusCode.Unauthorized,
                deleteResponse.status
            )
            val error = deleteResponse.body<ErrorResponse>()
            assertEquals(
                ErrorCodes.Account.INVALID_CURRENT_PASSWORD,
                error.code
            )
        }
    }

    @Test
    fun `list sessions returns current session`() = runTest {
        testApplication {
            val client = setupTestApp()
            val email = "sessions@example.com"

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, "SecurePass123"))
            }
            val loginResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        email,
                        "SecurePass123",
                        "TestDevice"
                    )
                )
            }
            val token = loginResponse.body<LoginResponse>().token

            val sessionsResponse = client.get("/api/auth/sessions") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, sessionsResponse.status)
            val body = sessionsResponse.body<SessionsListResponse>()
            assertEquals(1, body.totalCount)
            assertTrue(body.sessions.first().isCurrent)
            assertEquals(
                "TestDevice",
                body.sessions.first().deviceInfo
            )
        }
    }

    @Test
    fun `revoke other session returns 200`() = runTest {
        testApplication {
            val client = setupTestApp()
            val email = "revoke@example.com"

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, "SecurePass123"))
            }

            val login1 = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        email,
                        "SecurePass123",
                        "Device1"
                    )
                )
            }
            val login2 = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    LoginRequest(
                        email,
                        "SecurePass123",
                        "Device2"
                    )
                )
            }
            val token1 = login1.body<LoginResponse>().token
            val token2 = login2.body<LoginResponse>().token

            val sessionsResponse = client.get("/api/auth/sessions") {
                header(HttpHeaders.Authorization, "Bearer $token1")
            }
            val sessions =
                sessionsResponse.body<SessionsListResponse>().sessions
            val otherSessionId = sessions.first { !it.isCurrent }.id

            val revokeResponse =
                client.delete("/api/auth/sessions/$otherSessionId") {
                    header(
                        HttpHeaders.Authorization,
                        "Bearer $token1"
                    )
                }
            assertEquals(HttpStatusCode.OK, revokeResponse.status)

            val meResponse = client.get("/api/auth/me") {
                header(HttpHeaders.Authorization, "Bearer $token2")
            }
            assertEquals(
                HttpStatusCode.Unauthorized,
                meResponse.status
            )
        }
    }

    @Test
    fun `revoke current session returns 400`() = runTest {
        testApplication {
            val client = setupTestApp()
            val email = "self-revoke@example.com"

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, "SecurePass123"))
            }
            val loginResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, "SecurePass123"))
            }
            val token = loginResponse.body<LoginResponse>().token

            val sessionsResponse = client.get("/api/auth/sessions") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val currentId =
                sessionsResponse.body<SessionsListResponse>()
                    .sessions.first { it.isCurrent }.id

            val revokeResponse =
                client.delete("/api/auth/sessions/$currentId") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            assertEquals(
                HttpStatusCode.BadRequest,
                revokeResponse.status
            )
            val error = revokeResponse.body<ErrorResponse>()
            assertEquals(
                ErrorCodes.Validation.CANNOT_REVOKE_CURRENT_SESSION,
                error.code
            )
        }
    }
}