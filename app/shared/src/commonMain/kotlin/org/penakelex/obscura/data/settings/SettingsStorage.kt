package org.penakelex.obscura.data.settings

import kotlinx.coroutines.flow.Flow
import org.penakelex.obscura.domain.model.settings.AppSettings

expect class SettingsStorage {
    fun observe(): Flow<AppSettings>
    suspend fun get(): AppSettings
    suspend fun save(settings: AppSettings)
}