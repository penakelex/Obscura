package org.penakelex.obscura.db.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.db.model.Session
import java.security.MessageDigest
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

    fun create(
        userId: Uuid,
        deviceInfo: String?
    ): SessionCreationResult {
        val rawToken = generateSecureToken(
            ServerConfig.security.session.tokenLengthBytes
        )
        val tokenHash = hashToken(rawToken)
        val now = Clock.System.now()
        val expiresAt =
            now + ServerConfig.security.session.expirationDays.days

        transaction {
            Sessions.insert {
                it[this.userId] = userId
                it[this.tokenHash] = tokenHash
                it[this.deviceInfo] = deviceInfo
                it[createdAt] = now
                it[this.expiresAt] = expiresAt
                it[isActive] = true
            }
        }

        return SessionCreationResult(rawToken, expiresAt)
    }

    fun findActiveByTokenHash(tokenHash: String): Session? =
        transaction {
            val now = Clock.System.now()
            Sessions.selectAll()
                .where {
                    (Sessions.tokenHash eq tokenHash) and
                            (Sessions.isActive eq true) and
                            (Sessions.expiresAt greaterEq now)
                }
                .map { row ->
                    Session(
                        id = row[Sessions.id],
                        userId = row[Sessions.userId],
                        expiresAt = row[Sessions.expiresAt]
                    )
                }
                .singleOrNull()
        }

    fun findLastActiveAt(userId: Uuid): Instant? = transaction {
        Sessions.selectAll()
            .where { Sessions.userId eq userId }
            .orderBy(Sessions.createdAt, SortOrder.DESC)
            .limit(1)
            .map { row -> row[Sessions.createdAt] }
            .singleOrNull()
    }

    fun revoke(sessionId: Uuid) = transaction {
        Sessions.update({ Sessions.id eq sessionId }) {
            it[isActive] = false
        }
    }

    fun revokeAllByUserId(userId: Uuid): Int = transaction {
        Sessions.update({
            (Sessions.userId eq userId) and
                    (Sessions.isActive eq true)
        }) {
            it[isActive] = false
        }
    }

    fun countActiveByUserId(userId: Uuid): Int = transaction {
        Sessions.selectAll()
            .where {
                (Sessions.userId eq userId) and
                        (Sessions.isActive eq true) and
                        (Sessions.expiresAt greaterEq
                                Clock.System.now())
            }
            .count()
            .toInt()
    }

    private fun generateSecureToken(lengthBytes: Int): String {
        val bytes = ByteArray(lengthBytes)
        SecureRandom.getInstanceStrong().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashToken(token: String): String = MessageDigest
        .getInstance(ServerConfig.security.session.hashAlgorithm)
        .digest(token.toByteArray())
        .joinToString("") { "%02x".format(it) }
}