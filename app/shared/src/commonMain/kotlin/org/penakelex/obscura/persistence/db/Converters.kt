package org.penakelex.obscura.persistence.db

import androidx.room.TypeConverter
import org.penakelex.obscura.crypto.CipherType
import org.penakelex.obscura.persistence.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): Int = status.code

    @TypeConverter
    fun toSyncStatus(code: Int): SyncStatus =
        SyncStatus.fromCode(code)

    @TypeConverter
    fun fromCipherType(cipher: CipherType): Int = cipher.id

    @TypeConverter
    fun toCipherType(id: Int): CipherType =
        CipherType.fromIdOrFallback(id)
}