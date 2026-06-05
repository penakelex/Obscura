package org.penakelex.obscura.db.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.upsert
import org.penakelex.obscura.db.tables.UserKeysets
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class UserKeysetRepository {
    data class KeysetData(
        val userId: Uuid,
        val salt: ByteArray,
        val encryptedKeyset: ByteArray,
        val updatedAt: Long
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as KeysetData

            if (updatedAt != other.updatedAt) return false
            if (userId != other.userId) return false
            if (!salt.contentEquals(other.salt)) return false
            if (!encryptedKeyset.contentEquals(other.encryptedKeyset)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = updatedAt.hashCode()
            result = 31 * result + userId.hashCode()
            result = 31 * result + salt.contentHashCode()
            result = 31 * result + encryptedKeyset.contentHashCode()
            return result
        }
    }

    suspend fun upsertKeyset(
        userId: Uuid,
        salt: ByteArray,
        encryptedKeyset: ByteArray,
        updatedAt: Long
    ) = withContext(Dispatchers.IO) {
        suspendTransaction {
            UserKeysets.upsert {
                it[this.userId] = userId
                it[this.salt] = salt
                it[this.encryptedKeyset] = encryptedKeyset
                it[this.updatedAt] = updatedAt
            }
        }
    }

    suspend fun findByUserId(
        userId: Uuid,
    ): KeysetData? = withContext(Dispatchers.IO) {
        suspendTransaction {
            UserKeysets.selectAll()
                .where { UserKeysets.userId eq userId }
                .singleOrNull()
                ?.let { row ->
                    KeysetData(
                        userId = row[UserKeysets.userId],
                        salt = row[UserKeysets.salt],
                        encryptedKeyset = row[UserKeysets.encryptedKeyset],
                        updatedAt = row[UserKeysets.updatedAt]
                    )
                }
        }
    }

    suspend fun deleteByUserId(userId: Uuid): Boolean =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                UserKeysets.deleteWhere {
                    UserKeysets.userId eq userId
                } > 0
            }
        }
}