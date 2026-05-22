package org.penakelex.obscura.db.tables

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
object Sessions : Table("sessions") {
    val id = uuid("id").autoGenerate()

    val userId = reference("user_id", Users.id)
    val tokenHash = varchar("token_hash", 64).index()
    val deviceInfo = varchar("device_info", 128).nullable()
    val createdAt = timestamp("created_at")
    val expiresAt = timestamp("expires_at")
    val isActive = bool("is_active").default(true)

    init {
        index(false, userId, isActive)
        index(false, expiresAt, isActive)
    }

    override val primaryKey = PrimaryKey(id)
}