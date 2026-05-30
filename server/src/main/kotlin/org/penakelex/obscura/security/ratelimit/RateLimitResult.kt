package org.penakelex.obscura.security.ratelimit

import kotlin.time.Duration

data class RateLimitResult(
    val allowed: Boolean,
    val remaining: Int,
    val retryAfter: Duration?,
    val limit: Int
)