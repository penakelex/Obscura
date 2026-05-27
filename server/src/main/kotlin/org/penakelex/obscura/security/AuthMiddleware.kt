package org.penakelex.obscura.security

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import org.koin.ktor.plugin.koin
import org.penakelex.obscura.db.model.Session
import org.penakelex.obscura.db.repository.SessionRepository
import org.penakelex.obscura.exception.auth.AuthException

private const val BEARER_PREFIX = "Bearer "

suspend fun ApplicationCall.authenticate(): Session {
    val header = request.header(HttpHeaders.Authorization)
        ?: throw AuthException.SessionNotFound()

    if (!header.startsWith(BEARER_PREFIX, ignoreCase = true)) {
        throw AuthException.SessionNotFound()
    }

    val rawToken = header.substring(BEARER_PREFIX.length)

    val sessionRepository =
        application.koin().get<SessionRepository>()
    return sessionRepository.findActiveByRawToken(rawToken)
}