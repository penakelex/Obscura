package org.penakelex.obscura.rest.service

import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.test.runTest
import org.penakelex.obscura.cleanupDatabase
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.requests.auth.LoginRequest
import org.penakelex.obscura.contract.rest.requests.auth.RegisterRequest
import org.penakelex.obscura.contract.rest.responses.auth.LoginResponse
import org.penakelex.obscura.contract.rest.responses.auth.ProfileResponse
import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse
import org.penakelex.obscura.contract.rest.responses.common.SuccessResponse
import org.penakelex.obscura.setupTestApp
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthServiceTest {
    @AfterTest
    fun tearDown() {
        cleanupDatabase()
    }

    @Test
    fun `register with valid data returns 201`() = runTest {
        testApplication {
            val client = setupTestApp()

            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    RegisterRequest(
                        email = "test@example.com",
                        password = "SecurePass123"
                    )
                )
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val body = response.body<SuccessResponse>()
            assertTrue(body.message.contains("registered"))
        }
    }

    @Test
    fun `register with duplicate email returns 409`() = runTest {
        testApplication {
            val client = setupTestApp()

            // Первая регистрация
            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("dup@example.com", "SecurePass123"))
            }

            // Вторая регистрация с тем же email
            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("dup@example.com", "SecurePass123"))
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals(ErrorCodes.Auth.EMAIL_ALREADY_REGISTERED, error.code)
        }
    }

    @Test
    fun `register with invalid email returns 400`() = runTest {
        testApplication {
            val client = setupTestApp()

            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("not-an-email", "SecurePass123"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals(ErrorCodes.Validation.MULTIPLE_FIELDS_INVALID, error.code)
            assertNotNull(error.details)
            assertTrue(error.details!!.any {
                it.field == "email" && it.code == ErrorCodes.Validation.INVALID_EMAIL_FORMAT
            })
        }
    }

    @Test
    fun `register with short password returns 400`() = runTest {
        testApplication {
            val client = setupTestApp()

            val response = client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("test@example.com", "short"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals(ErrorCodes.Validation.MULTIPLE_FIELDS_INVALID, error.code)
            assertNotNull(error.details)
            assertTrue(error.details!!.any {
                it.field == "password" && it.code == ErrorCodes.Validation.PASSWORD_TOO_SHORT
            })
        }
    }

    @Test
    fun `login with valid credentials returns token`() = runTest {
        testApplication {
            val client = setupTestApp()

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("login@example.com", "SecurePass123"))
            }

            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest("login@example.com", "SecurePass123"))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<LoginResponse>()
            assertTrue(body.token.isNotEmpty())
            assertTrue(body.userId.isNotEmpty())
            assertTrue(body.expiresAt.isNotEmpty())
        }
    }

    @Test
    fun `login with wrong password returns 401`() = runTest {
        testApplication {
            val client = setupTestApp()

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("wrong@example.com", "SecurePass123"))
            }

            val response = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest("wrong@example.com", "WrongPassword1"))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals(ErrorCodes.Auth.INVALID_CREDENTIALS, error.code)
        }
    }

    @Test
    fun `get profile with valid token returns user data`() = runTest {
        testApplication {
            val client = setupTestApp()

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("me@example.com", "SecurePass123"))
            }
            val loginResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest("me@example.com", "SecurePass123"))
            }
            val token = loginResponse.body<LoginResponse>().token

            val response = client.get("/api/auth/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val profile = response.body<ProfileResponse>()
            assertEquals("me@example.com", profile.email)
        }
    }

    @Test
    fun `get profile without token returns 401`() = runTest {
        testApplication {
            val client = setupTestApp()

            val response = client.get("/api/auth/me")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals(ErrorCodes.Auth.SESSION_NOT_FOUND, error.code)
        }
    }

    @Test
    fun `get profile with invalid token returns 401`() = runTest {
        testApplication {
            val client = setupTestApp()

            val response = client.get("/api/auth/me") {
                header(HttpHeaders.Authorization, "Bearer invalid_token_hex")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
            val error = response.body<ErrorResponse>()
            assertEquals(ErrorCodes.Auth.SESSION_NOT_FOUND, error.code)
        }
    }

    @Test
    fun `logout revokes session and subsequent me returns 401`() = runTest {
        testApplication {
            val client = setupTestApp()

            client.post("/api/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest("logout@example.com", "SecurePass123"))
            }
            val loginResponse = client.post("/api/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest("logout@example.com", "SecurePass123"))
            }
            val token = loginResponse.body<LoginResponse>().token

            val logoutResponse = client.post("/api/auth/logout") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, logoutResponse.status)

            val meResponse = client.get("/api/auth/me") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            assertEquals(HttpStatusCode.Unauthorized, meResponse.status)
            val error = meResponse.body<ErrorResponse>()
            assertEquals(ErrorCodes.Auth.SESSION_NOT_FOUND, error.code)
        }
    }
}