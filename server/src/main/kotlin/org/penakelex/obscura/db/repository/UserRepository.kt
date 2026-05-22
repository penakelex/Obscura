package org.penakelex.obscura.db.repository

import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.penakelex.obscura.db.tables.Users
import org.penakelex.obscura.db.model.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object UserRepository {
    fun create(email: String, passwordHash: String): Uuid? =
        transaction {
            val existing = Users.selectAll()
                .where { Users.email eq email }
                .singleOrNull()

            if (existing != null) {
                return@transaction null
            }

            Users.insert {
                it[this.email] = email
                it[this.passwordHash] = passwordHash
            } get Users.id
        }

    fun findByEmail(email: String): User? = transaction {
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

    fun findById(id: Uuid): User? = transaction {
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