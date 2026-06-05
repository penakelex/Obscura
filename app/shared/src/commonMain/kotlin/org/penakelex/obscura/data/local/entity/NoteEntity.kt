package org.penakelex.obscura.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["syncStatus", "updatedAt"]),
        Index(value = ["isDeleted", "updatedAt"]),
        Index(value = ["isDeleted", "syncStatus"]),
        Index(value = ["isLocalOnly"]),
    ]
)
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val encryptedData: ByteArray,
    val cipherType: CipherType = CipherType.DEFAULT,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val version: Int = 1,
    val isDeleted: Boolean = false,
    val isLocalOnly: Boolean = false,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NoteEntity

        if (updatedAt != other.updatedAt) return false
        if (version != other.version) return false
        if (isDeleted != other.isDeleted) return false
        if (isLocalOnly != other.isLocalOnly) return false
        if (id != other.id) return false
        if (!encryptedData.contentEquals(other.encryptedData)) return false
        if (cipherType != other.cipherType) return false
        if (syncStatus != other.syncStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = updatedAt.hashCode()
        result = 31 * result + version
        result = 31 * result + isDeleted.hashCode()
        result = 31 * result + isLocalOnly.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + cipherType.hashCode()
        result = 31 * result + syncStatus.hashCode()
        return result
    }
}