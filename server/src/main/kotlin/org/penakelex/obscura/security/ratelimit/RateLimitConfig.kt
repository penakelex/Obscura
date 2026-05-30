package org.penakelex.obscura.security.ratelimit

import io.ktor.server.application.ApplicationCall
import kotlin.time.Duration

data class RateLimitConfig(
    val maxRequests: Int,
    val window: Duration,
    val keyExtractor: (ApplicationCall) -> String = ::defaultKeyExtractor
)