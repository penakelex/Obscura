package org.penakelex.obscura.domain.gateway

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.model.settings.AppSettings
import org.penakelex.obscura.domain.model.common.CipherType

interface SettingsGateway {
    fun observe(): Flow<AppSettings>
    suspend fun get(): AppSettings
    suspend fun setDefaultCipherType(cipherType: CipherType)
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setAutoSync(enabled: Boolean)
    suspend fun setLastSyncTimestamp(timestamp: Long)
}