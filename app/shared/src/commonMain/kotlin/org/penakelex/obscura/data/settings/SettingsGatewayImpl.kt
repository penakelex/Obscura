package org.penakelex.obscura.data.settings

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.model.settings.AppSettings
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.settings.ThemeMode

class SettingsGatewayImpl(
    private val storage: SettingsStorage
) : SettingsGateway {
    override fun observe(): Flow<AppSettings> = storage.observe()
    override suspend fun get(): AppSettings = storage.get()
    override suspend fun setDefaultCipherType(cipherType: CipherType) {
        update { it.copy(defaultCipherType = cipherType) }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        update { it.copy(themeMode = mode) }
    }

    override suspend fun setAutoSync(enabled: Boolean) {
        update { it.copy(isAutoSyncEnabled = enabled) }
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        update { it.copy(lastSyncTimestamp = timestamp) }
    }

    private suspend fun update(transform: (AppSettings) -> AppSettings) {
        val current = storage.get()
        storage.save(transform(current))
    }
}