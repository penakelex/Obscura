package org.penakelex.obscura.db.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Note(
    val id: Uuid,
    val userId: Uuid,
    val encryptedData: ByteArray,
    val cipherType: Int,
    val updatedAt: Long,
    val isDeleted: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Note
        if (id != other.id) return false
        if (userId != other.userId) return false
        if (!encryptedData.contentEquals(other.encryptedData))
            return false
        if (cipherType != other.cipherType) return false
        if (updatedAt != other.updatedAt) return false
        if (isDeleted != other.isDeleted) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + cipherType
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + isDeleted.hashCode()
        return result
    }
}