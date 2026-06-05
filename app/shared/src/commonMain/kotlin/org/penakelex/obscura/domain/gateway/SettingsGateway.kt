package org.penakelex.obscura.domain.gateway

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.model.settings.AppSettings
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.settings.ThemeMode

interface SettingsGateway {
    fun observe(): Flow<AppSettings>
    suspend fun get(): AppSettings
    suspend fun setDefaultCipherType(cipherType: CipherType)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAutoSync(enabled: Boolean)
    suspend fun setLastSyncTimestamp(timestamp: Long)
}