package org.penakelex.obscura.data.remote.http

import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.call.body
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse

abstract class BaseApiClient(
    protected val client: HttpClient,
    protected val baseUrl: String,
    protected val json: Json = Json { ignoreUnknownKeys = true }
) {
    protected fun HttpRequestBuilder.jsonBody(value: Any) {
        contentType(ContentType.Application.Json)
        setBody(value)
    }

    protected fun HttpRequestBuilder.bearerAuth(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    protected suspend inline fun <reified T> execute(
        crossinline block: suspend () -> HttpResponse
    ): T {
        val response = try {
            block()
        } catch (e: Exception) {
            throw ApiException.Network(e)
        }

        return when {
            response.status.isSuccess() -> response.body()
            response.status == HttpStatusCode.Unauthorized -> {
                val error = parseError(response)
                throw ApiException.Unauthorized(error)
            }
            response.status == HttpStatusCode.Conflict -> {
                val error = parseError(response)
                throw ApiException.Conflict(error)
            }
            response.status == HttpStatusCode.NotFound -> {
                val error = parseError(response)
                throw ApiException.NotFound(error)
            }
            response.status == HttpStatusCode.BadRequest -> {
                val error = parseError(response)
                throw ApiException.BadRequest(error)
            }
            else -> {
                val error = parseError(response)
                throw ApiException.Server(response.status.value, error)
            }
        }
    }

    protected suspend fun parseError(response: HttpResponse): ErrorResponse = try {
        json.decodeFromString(
            ErrorResponse.serializer(),
            response.bodyAsText()
        )
    } catch (_: Exception) {
        ErrorResponse(error = response.bodyAsText())
    }
}