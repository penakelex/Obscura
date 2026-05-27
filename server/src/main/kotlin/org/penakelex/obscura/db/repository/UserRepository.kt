package org.penakelex.obscura.db.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update
import org.penakelex.obscura.db.tables.Users
import org.penakelex.obscura.db.model.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object UserRepository {
    suspend fun create(email: String, passwordHash: String): Uuid? =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                val existing = Users.selectAll()
                    .where { Users.email eq email }
                    .singleOrNull()

                if (existing != null) {
                    return@suspendTransaction null
                }

                Users.insert {
                    it[this.email] = email
                    it[this.passwordHash] = passwordHash
                } get Users.id
            }
        }

    suspend fun findByEmail(email: String): User? =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Users.selectAll()
                    .where { Users.email eq email }
                    .map { row ->
                        User(
                            id = row[Users.id],
                            email = row[Users.email],
                            passwordHash = row[Users.passwordHash]
                        )
                    }
                    .singleOrNull()
            }
        }

    suspend fun findById(id: Uuid): User? =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Users.selectAll()
                    .where { Users.id eq id }
                    .map { row ->
                        User(
                            id = row[Users.id],
                            email = row[Users.email],
                            passwordHash = row[Users.passwordHash]
                        )
                    }
                    .singleOrNull()
            }
        }

    suspend fun updatePassword(
        userId: Uuid,
        newPasswordHash: String
    ) = withContext(Dispatchers.IO) {
        suspendTransaction {
            Users.update({ Users.id eq userId }) {
                it[passwordHash] = newPasswordHash
            }
        }
    }

    suspend fun updateEmail(userId: Uuid, newEmail: String) =
        withContext(Dispatchers.IO) {
            suspendTransaction {
                Users.update({ Users.id eq userId }) {
                    it[email] = newEmail
                }
            }
        }
}