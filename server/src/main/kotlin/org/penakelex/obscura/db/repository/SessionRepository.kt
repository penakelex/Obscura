package org.penakelex.obscura.db.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.model.Session
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.exception.auth.AuthException
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class SessionRepository(
    private val config: ServerConfig.Security.Session
) {
    data class SessionCreationResult(
        val rawToken: String,
        val expiresAt: Instant
    )

    suspend fun create(
        userId: Uuid,
        deviceInfo: String?
    ): SessionCreationResult = withContext(Dispatchers.IO) {
        val rawToken = generateSecureToken(config.tokenLengthBytes)
        val tokenHash = hashToken(rawToken)
        val now = Clock.System.now()
        val expiresAt = now + config.expirationDays.days

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

    suspend fun findActiveByRawToken(rawToken: String): Session =
        withContext(Dispatchers.IO) {
            val tokenHash = hashToken(rawToken)

            val session = suspendTransaction {
                Sessions.selectAll()
                    .where {
                        (Sessions.tokenHash eq tokenHash) and
                                (Sessions.isActive eq true) and
                                (Sessions.expiresAt greaterEq Clock.System.now())
                    }
                    .singleOrNull()?.toSession()
            }

            if (session == null) {
                throw AuthException.SessionNotFound()
            }
            session
        }

    suspend fun findAllActiveByUserId(userId: Uuid): List<Session> =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Sessions.selectAll()
                    .where {
                        (Sessions.userId eq userId) and
                                (Sessions.isActive eq true)
                    }
                    .map { it.toSession() }
            }
        }

    suspend fun findActiveByIdAndUser(
        sessionId: Uuid,
        userId: Uuid
    ): Session? = withContext(Dispatchers.IO) {
        suspendTransaction {
            Sessions.selectAll()
                .where {
                    (Sessions.id eq sessionId) and
                            (Sessions.userId eq userId) and
                            (Sessions.isActive eq true)
                }
                .singleOrNull()
                ?.toSession()
        }
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

    suspend fun deleteAllByUserId(userId: Uuid): Int =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Sessions.deleteWhere { Sessions.userId eq userId }
            }
        }

    suspend fun deleteExpiredAndInactive(): Int =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                val now = Clock.System.now()
                Sessions.deleteWhere {
                    (Sessions.expiresAt less now) or
                            (Sessions.isActive eq false)
                }
            }
        }

    private fun ResultRow.toSession(): Session = Session(
        id = this[Sessions.id],
        userId = this[Sessions.userId],
        deviceInfo = this[Sessions.deviceInfo],
        createdAt = this[Sessions.createdAt],
        expiresAt = this[Sessions.expiresAt],
        isActive = this[Sessions.isActive]
    )

    private fun generateSecureToken(lengthBytes: Int): String {
        val bytes = ByteArray(lengthBytes)
        SecureRandom.getInstanceStrong().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashToken(rawToken: String): String =
        MessageDigest.getInstance(config.hashAlgorithm)
            .digest(rawToken.toByteArray())
            .joinToString("") { "%02x".format(it) }
}