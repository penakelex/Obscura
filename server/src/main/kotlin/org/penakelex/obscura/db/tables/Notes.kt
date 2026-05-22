package org.penakelex.obscura.db.tables

import org.jetbrains.exposed.v1.core.Table
import org.penakelex.obscura.config.ServerConfig
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object Notes : Table("notes") {
    val id = uuid("id").autoGenerate()

    val userId = reference("user_id", Users.id)
    val encryptedData = binary("encrypted_data")
    val cipherType = integer("cipher_type")
        .default(ServerConfig.security.defaultCipherType)
    val updatedAt = long("updated_at")
    val isDeleted = bool("is_deleted").default(false)

    init {
        index(false, userId, updatedAt)
    }

    override val primaryKey = PrimaryKey(id)
}