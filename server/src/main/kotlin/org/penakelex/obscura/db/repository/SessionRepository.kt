package org.penakelex.obscura.db.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.model.Session
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.exception.auth.AuthException
import org.penakelex.obscura.security.hashSessionToken
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object SessionRepository {
    data class SessionCreationResult(
        val rawToken: String,
        val expiresAt: Instant
    )

    suspend fun create(
        userId: Uuid,
        deviceInfo: String?
    ): SessionCreationResult = withContext(Dispatchers.IO) {
        val rawToken = generateSecureToken(
            ServerConfig.security.session.tokenLengthBytes
        )
        val tokenHash = hashSessionToken(rawToken)
        val now = Clock.System.now()
        val expiresAt =
            now + ServerConfig.security.session.expirationDays.days

        suspendTransaction {
            Sessions.insert {
                it[this.userId] = userId
                it[this.tokenHash] = tokenHash
                it[this.deviceInfo] = deviceInfo
                it[createdAt] = now
                it[this.expiresAt] = expiresAt
                it[isActive] = true
            }
        }

        SessionCreationResult(rawToken, expiresAt)
    }

    suspend fun findActiveByTokenHash(tokenHash: String): Session =
        withContext(Dispatchers.IO) {
            val session = suspendTransaction {
                Sessions.selectAll()
                    .where { Sessions.tokenHash eq tokenHash }
                    .map { row ->
                        Session(
                            id = row[Sessions.id],
                            userId = row[Sessions.userId],
                            expiresAt = row[Sessions.expiresAt],
                            isActive = row[Sessions.isActive]
                        )
                    }
                    .singleOrNull()
            }

            if (session == null || !session.isActive) {
                throw AuthException.SessionNotFound()
            }

            if (session.expiresAt < Clock.System.now()) {
                throw AuthException.SessionExpired()
            }

            session
        }

    suspend fun revoke(sessionId: Uuid) =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Sessions.update({ Sessions.id eq sessionId }) {
                    it[isActive] = false
                }
            }
        }

    suspend fun revokeAllByUserId(userId: Uuid): Int =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Sessions.update({
                    (Sessions.userId eq userId) and
                            (Sessions.isActive eq true)
                }) {
                    it[isActive] = false
                }
            }
        }

    private fun generateSecureToken(lengthBytes: Int): String {
        val bytes = ByteArray(lengthBytes)
        SecureRandom.getInstanceStrong().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}