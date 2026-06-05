package org.penakelex.obscura.db.tables

import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object UserKeysets : Table("user_keysets") {
    val userId = reference("user_id", Users.id).uniqueIndex()
    val salt = binary("salt")
    val encryptedKeyset = binary("encrypted_keyset")
    val updatedAt = long("updated_at")

    override val primaryKey = PrimaryKey(userId)
}