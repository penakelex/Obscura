package org.penakelex.obscura.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.settings.AppSettings
import org.penakelex.obscura.domain.model.settings.ThemeMode

private val Context.settingsDataStore by preferencesDataStore(
    name = "obscura_settings",
)

actual class SettingsStorage(private val context: Context) {
    private val themeKey = intPreferencesKey("theme_mode")
    private val cipherKey = intPreferencesKey("default_cipher_type")
    private val autoSyncKey = booleanPreferencesKey("is_auto_sync")
    private val lastSyncKey = longPreferencesKey("last_sync_timestamp")

    actual fun observe(): Flow<AppSettings> =
        context.settingsDataStore.data.map { prefs ->
            AppSettings(
                themeMode = ThemeMode.fromId(prefs[themeKey] ?: ThemeMode.DEFAULT.id),
                defaultCipherType = CipherType.fromIdOrFallback(
                    prefs[cipherKey] ?: CipherType.DEFAULT.id
                ),
                isAutoSyncEnabled = prefs[autoSyncKey] ?: true,
                lastSyncTimestamp = prefs[lastSyncKey] ?: 0L
            )
        }

    actual suspend fun get(): AppSettings =
        observe().first()

    actual suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[themeKey] = settings.themeMode.id
            prefs[cipherKey] = settings.defaultCipherType.id
            prefs[autoSyncKey] = settings.isAutoSyncEnabled
            prefs[lastSyncKey] = settings.lastSyncTimestamp
        }
    }
}