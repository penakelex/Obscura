package org.penakelex.obscura.db.tables

import org.jetbrains.exposed.v1.core.Table
import org.penakelex.obscura.config.ServerConfig
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object Users : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar(
        "password_hash",
        ServerConfig.security.password.hashLength
    )

    override val primaryKey = PrimaryKey(id)
}