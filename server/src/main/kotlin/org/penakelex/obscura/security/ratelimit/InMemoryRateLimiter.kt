package org.penakelex.obscura.security.ratelimit

import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Clock
import kotlin.time.Instant

class InMemoryRateLimiter(private val config: RateLimitConfig) {
    private val logger =
        LoggerFactory.getLogger(InMemoryRateLimiter::class.java)

    private val buckets =
        ConcurrentHashMap<String, MutableList<Instant>>()

    fun tryAcquire(key: String): RateLimitResult {
        val now = Clock.System.now()
        val windowStart = now - config.window

        val bucket = buckets.compute(key) { _, existing ->
            val list = existing ?: mutableListOf()
            list.removeAll { it < windowStart }
            list.add(now)
            list
        }!!

        val requestsInWindow = bucket.size
        val remaining = (config.maxRequests - requestsInWindow)
            .coerceAtLeast(0)

        val retryAfter = if (remaining == 0) {
            val oldest = bucket.first()
            (oldest + config.window) - now
        } else {
            null
        }

        if (remaining == 0) {
            logger.debug(
                "Rate limit exceeded for key='{}' ({} requests in {})",
                key, requestsInWindow, config.window
            )
        }

        return RateLimitResult(
            allowed = remaining > 0,
            remaining = remaining,
            retryAfter = retryAfter,
            limit = config.maxRequests
        )
    }
}