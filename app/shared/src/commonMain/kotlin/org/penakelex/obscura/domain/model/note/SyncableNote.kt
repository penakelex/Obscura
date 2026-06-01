package org.penakelex.obscura.domain.model.note

import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus

data class SyncableNote(
    val id: String,
    val encryptedData: ByteArray,
    val cipherType: CipherType,
    val updatedAt: Long,
    val isDeleted: Boolean,
    val version: Int,
    val syncStatus: SyncStatus
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SyncableNote) return false
        return id == other.id &&
                encryptedData.contentEquals(other.encryptedData) &&
                cipherType == other.cipherType &&
                updatedAt == other.updatedAt &&
                isDeleted == other.isDeleted &&
                version == other.version &&
                syncStatus == other.syncStatus
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + cipherType.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + isDeleted.hashCode()
        result = 31 * result + version
        result = 31 * result + syncStatus.hashCode()
        return result
    }
}