package org.penakelex.obscura.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.plugins.callid.callId
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import io.ktor.util.AttributeKey
import org.koin.ktor.ext.inject
import org.penakelex.obscura.contract.ErrorCodes
import org.penakelex.obscura.contract.rest.responses.common.ErrorResponse
import org.penakelex.obscura.security.ratelimit.InMemoryRateLimiter
import org.penakelex.obscura.security.ratelimit.defaultKeyExtractor
import kotlin.time.Clock

private const val HEADER_RATE_LIMIT = "X-RateLimit-Limit"
private const val HEADER_RATE_REMAINING = "X-RateLimit-Remaining"
private const val HEADER_RETRY_AFTER = "Retry-After"

private val RateLimiterAttributeKey =
    AttributeKey<InMemoryRateLimiter>("RateLimiter")

fun Application.configureRateLimiting() {
    val rateLimiter by inject<InMemoryRateLimiter>()
    attributes.put(RateLimiterAttributeKey, rateLimiter)
}

private val RateLimitPlugin = createRouteScopedPlugin("RateLimit") {
    onCall { call ->
        val rateLimiter = call.application
            .attributes[RateLimiterAttributeKey]
        handleRateLimit(call, rateLimiter)
    }
}

fun Route.rateLimited(build: Route.() -> Unit) {
    route("") {
        install(RateLimitPlugin)
        build()
    }
}

private suspend fun handleRateLimit(
    call: ApplicationCall,
    rateLimiter: InMemoryRateLimiter
) {
    val key = defaultKeyExtractor(call)
    val result = rateLimiter.tryAcquire(key)

    call.response.header(HEADER_RATE_LIMIT, result.limit.toString())
    call.response.header(
        HEADER_RATE_REMAINING,
        result.remaining.toString()
    )

    if (!result.allowed) {
        result.retryAfter?.let { retryAfter ->
            call.response.header(
                HEADER_RETRY_AFTER,
                retryAfter.inWholeSeconds.coerceAtLeast(1).toString()
            )
        }
        call.respond(
            HttpStatusCode.TooManyRequests,
            ErrorResponse(
                error = "Too many requests. Please try again later.",
                code = ErrorCodes.System.RATE_LIMIT_EXCEEDED,
                traceId = call.callId,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                retryAfterSeconds = result.retryAfter?.inWholeSeconds
            )
        )
    }
}