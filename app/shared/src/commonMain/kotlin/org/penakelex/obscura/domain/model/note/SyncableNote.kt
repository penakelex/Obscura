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
    val syncStatus: SyncStatus,
    val isLocalOnly: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SyncableNote

        if (updatedAt != other.updatedAt) return false
        if (isDeleted != other.isDeleted) return false
        if (version != other.version) return false
        if (isLocalOnly != other.isLocalOnly) return false
        if (id != other.id) return false
        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (cipherType != other.cipherType) return false
        if (syncStatus != other.syncStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = updatedAt.hashCode()
        result = 31 * result + isDeleted.hashCode()
        result = 31 * result + version
        result = 31 * result + isLocalOnly.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + cipherType.hashCode()
        result = 31 * result + syncStatus.hashCode()
        return result
    }
}