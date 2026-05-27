package org.penakelex.obscura

import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callId
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse
import org.penakelex.obscura.contract.rest.responses.common.FieldError
import org.penakelex.obscura.db.DatabaseManager
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.db.repository.UserRepository
import org.penakelex.obscura.exception.ObscuraException
import org.penakelex.obscura.exception.account.AccountException
import org.penakelex.obscura.exception.auth.AuthException
import org.penakelex.obscura.exception.resource.NotFoundException
import org.penakelex.obscura.exception.validation.ValidationException
import org.penakelex.obscura.jobs.SessionCleanupJob
import org.penakelex.obscura.rest.routing.authRouting
import org.penakelex.obscura.rest.routing.healthRouting
import org.penakelex.obscura.rest.service.AuthService
import org.slf4j.event.Level
import java.util.UUID
import kotlin.time.Clock

fun main() {
    dotenv().entries().forEach { entry ->
        if (System.getProperty(entry.key) == null) {
            System.setProperty(entry.key, entry.value)
        }
    }

    DatabaseManager.init()

    val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    SessionCleanupJob.start(scope)

    val server = embeddedServer(
        Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::module
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        scope.cancel()
        DatabaseManager.close()
    })

    server.start(wait = true)
}

fun Application.module() {
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate { UUID.randomUUID().toString() }
        verify { it.isNotEmpty() }
    }

    install(CallLogging) {
        level = Level.INFO
        filter { call -> !call.request.path().startsWith("/health") }
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = false
            prettyPrint = false
        })
    }

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

    val authService = AuthService(
        userRepository = UserRepository,
        sessionRepository = SessionRepository
    )

    routing {
        healthRouting()
        authRouting(authService)
    }
}