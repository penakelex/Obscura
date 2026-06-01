package org.penakelex.obscura.data.local.db

import androidx.room.TypeConverter
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.common.SyncStatus

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