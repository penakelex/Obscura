package org.penakelex.obscura.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse
import org.penakelex.obscura.contract.rest.responses.common.FieldError
import org.penakelex.obscura.exception.ObscuraException
import org.penakelex.obscura.exception.account.AccountException
import org.penakelex.obscura.exception.auth.AuthException
import org.penakelex.obscura.exception.resource.NotFoundException
import org.penakelex.obscura.exception.validation.ValidationException
import kotlin.time.Clock

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.application.log.warn(
                "Validation failed [code={}, traceId={}]: {}",
                cause.errorCode, call.callId, cause.message
            )
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    error = cause.message ?: "Validation failed",
                    code = cause.errorCode,
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds(),
                    details = cause.fieldErrors.map {
                        FieldError(it.field, it.code, it.message)
                    }
                )
            )
        }

        exception<AuthException> { call, cause ->
            call.application.log.warn(
                "Auth error [code={}, traceId={}]: {}",
                cause.errorCode, call.callId, cause.message
            )
            call.respond(
                cause.httpStatus,
                ErrorResponse(
                    error = cause.message ?: "Authentication error",
                    code = cause.errorCode,
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds()
                )
            )
        }

        exception<NotFoundException> { call, cause ->
            call.application.log.warn(
                "Resource not found [code={}, traceId={}]: {}",
                cause.errorCode, call.callId, cause.message
            )
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = cause.message ?: "Resource not found",
                    code = cause.errorCode,
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds()
                )
            )
        }

        exception<AccountException> { call, cause ->
            call.application.log.warn(
                "Account error [code={}, traceId={}]: {}",
                cause.errorCode, call.callId, cause.message
            )
            call.respond(
                cause.httpStatus,
                ErrorResponse(
                    error = cause.message
                        ?: "Account operation failed",
                    code = cause.errorCode,
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds()
                )
            )
        }

        exception<ObscuraException> { call, cause ->
            call.application.log.warn(
                "Unhandled ObscuraException [code={}, traceId={}]: {}",
                cause.errorCode, call.callId, cause.message
            )
            call.respond(
                cause.httpStatus,
                ErrorResponse(
                    error = cause.message ?: "Unknown error",
                    code = cause.errorCode,
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds()
                )
            )
        }

        exception<Throwable> { call, cause ->
            call.application.log.error(
                "Unhandled exception [traceId={}]",
                call.callId,
                cause
            )
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    error = "Internal server error",
                    code = ErrorCodes.System.INTERNAL_ERROR,
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds()
                )
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = "Route not found: ${call.request.path()}",
                    traceId = call.callId,
                    timestamp = Clock.System.now()
                        .toEpochMilliseconds()
                )
            )
        }
    }
}