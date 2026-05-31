package org.penakelex.obscura.persistence.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.penakelex.obscura.crypto.CipherType
import org.penakelex.obscura.persistence.SyncStatus

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["syncStatus", "updatedAt"]),
        Index(value = ["isDeleted", "updatedAt"]),
        Index(value = ["isDeleted", "syncStatus"])
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NoteEntity) return false
        return id == other.id &&
                encryptedData.contentEquals(other.encryptedData) &&
                cipherType == other.cipherType &&
                updatedAt == other.updatedAt &&
                syncStatus == other.syncStatus &&
                version == other.version &&
                isDeleted == other.isDeleted
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + encryptedData.contentHashCode()
        result = 31 * result + cipherType.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + syncStatus.hashCode()
        result = 31 * result + version
        result = 31 * result + isDeleted.hashCode()
        return result
    }
}