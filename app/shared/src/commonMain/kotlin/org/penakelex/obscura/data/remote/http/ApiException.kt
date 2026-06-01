package org.penakelex.obscura.data.remote.http

import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse

sealed class ApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class Network(cause: Throwable) : ApiException(
        "Network error: ${cause.message}", cause
    )

    class Server(
        val statusCode: Int,
        val errorResponse: ErrorResponse
    ) : ApiException(errorResponse.error)

    class Unauthorized(val errorResponse: ErrorResponse) :
        ApiException(errorResponse.error)

    class BadRequest(val errorResponse: ErrorResponse) : ApiException(
        errorResponse.error
    )

    class NotFound(val errorResponse: ErrorResponse) : ApiException(
        errorResponse.error
    )

    class Conflict(val errorResponse: ErrorResponse) : ApiException(
        errorResponse.error
    )

    class Unknown(cause: Throwable) : ApiException(
        "Unknown error: ${cause.message}", cause
    )
}