package org.penakelex.obscura.data.remote.http

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import kotlinx.serialization.json.Json
import org.penakelex.obscura.contract.rest.requests.account.ChangeEmailRequest
import org.penakelex.obscura.contract.rest.requests.account.ChangePasswordRequest
import org.penakelex.obscura.contract.rest.requests.account.DeleteAccountRequest
import org.penakelex.obscura.contract.rest.requests.auth.AuthRequest
import org.penakelex.obscura.contract.rest.requests.auth.ChallengeRequest
import org.penakelex.obscura.contract.rest.responses.auth.ChallengeResponse
import org.penakelex.obscura.contract.rest.responses.auth.SessionResponse
import org.penakelex.obscura.contract.rest.responses.auth.LogoutAllResponse
import org.penakelex.obscura.contract.rest.responses.auth.ProfileResponse
import org.penakelex.obscura.contract.rest.responses.auth.SessionsListResponse
import org.penakelex.obscura.contract.rest.responses.common.SuccessResponse
import org.penakelex.obscura.data.remote.config.NetworkConfig
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AuthApiClient(
    client: HttpClient,
    baseUrl: String = NetworkConfig.Rest.BASE_URL,
    json: Json = Json { ignoreUnknownKeys = true }
) : BaseApiClient(client, baseUrl, json) {
    private val paths = NetworkConfig.Rest.Paths

    suspend fun challenge(request: ChallengeRequest): ChallengeResponse =
        execute {
            client.post("$baseUrl${paths.AUTH_CHALLENGE}") {
                jsonBody(request)
            }
        }

    suspend fun register(request: AuthRequest): SessionResponse =
        execute {
            client.post("$baseUrl${paths.AUTH_REGISTER}") {
                jsonBody(request)
            }
        }

    suspend fun login(request: AuthRequest): SessionResponse =
        execute {
            client.post("$baseUrl${paths.AUTH_LOGIN}") {
                jsonBody(request)
            }
        }

    suspend fun logout(token: String): SuccessResponse =
        execute {
            client.post("$baseUrl${paths.AUTH_LOGOUT}") {
                bearerAuth(token)
            }
        }

    suspend fun logoutAll(token: String): LogoutAllResponse =
        execute {
            client.post("$baseUrl${paths.AUTH_LOGOUT_ALL}") {
                bearerAuth(token)
            }
        }

    suspend fun getProfile(token: String): ProfileResponse =
        execute {
            client.get("$baseUrl${paths.AUTH_ME}") {
                bearerAuth(token)
            }
        }

    suspend fun changePassword(
        token: String,
        request: ChangePasswordRequest
    ): SuccessResponse = execute {
        client.put("$baseUrl${paths.AUTH_PASSWORD}") {
            bearerAuth(token)
            jsonBody(request)
        }
    }

    suspend fun changeEmail(
        token: String,
        request: ChangeEmailRequest
    ): SuccessResponse = execute {
        client.put("$baseUrl${paths.AUTH_EMAIL}") {
            bearerAuth(token)
            jsonBody(request)
        }
    }

    suspend fun deleteAccount(
        token: String,
        request: DeleteAccountRequest
    ): SuccessResponse = execute {
        client.delete("$baseUrl${paths.AUTH_ACCOUNT}") {
            bearerAuth(token)
            jsonBody(request)
        }
    }

    suspend fun listSessions(token: String): SessionsListResponse =
        execute {
            client.get("$baseUrl${paths.AUTH_SESSIONS}") {
                bearerAuth(token)
            }
        }

    @OptIn(ExperimentalUuidApi::class)
    suspend fun revokeSession(
        token: String,
        sessionId: Uuid
    ): SuccessResponse = execute {
        client.delete(
            "$baseUrl${paths.AUTH_SESSIONS}/$sessionId"
        ) {
            bearerAuth(token)
        }
    }
}