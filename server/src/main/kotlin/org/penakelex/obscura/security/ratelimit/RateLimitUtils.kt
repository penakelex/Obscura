package org.penakelex.obscura.security.ratelimit

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header

private const val HEADER_X_FORWARDED_FOR = "X-Forwarded-For"
private const val HEADER_USER_AGENT = "User-Agent"
private const val UNKNOWN_AGENT = "unknown"
private const val KEY_SEPARATOR = "|"

fun defaultKeyExtractor(call: ApplicationCall): String {
    val ip = call.request.header(HEADER_X_FORWARDED_FOR)
        ?.substringBefore(',')
        ?.trim()
        ?: call.request.local.remoteAddress
    val userAgent =
        call.request.header(HEADER_USER_AGENT) ?: UNKNOWN_AGENT
    return "$ip$KEY_SEPARATOR$userAgent"
}