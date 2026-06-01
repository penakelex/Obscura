package org.penakelex.obscura.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.penakelex.obscura.domain.model.settings.AppSettings
import org.penakelex.obscura.domain.model.common.CipherType

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "obscura_settings"
)

actual class SettingsStorage(private val context: Context) {

    private val cipherKey = intPreferencesKey("default_cipher_type")
    private val darkModeKey = booleanPreferencesKey("is_dark_mode")
    private val autoSyncKey = booleanPreferencesKey("is_auto_sync")
    private val lastSyncKey = longPreferencesKey("last_sync_timestamp")

    actual fun observe(): Flow<AppSettings> =
        context.settingsDataStore.data.map { prefs ->
            AppSettings(
                defaultCipherType = CipherType.fromIdOrFallback(
                    prefs[cipherKey] ?: CipherType.DEFAULT.id
                ),
                isDarkMode = prefs[darkModeKey] ?: false,
                isAutoSyncEnabled = prefs[autoSyncKey] ?: true,
                lastSyncTimestamp = prefs[lastSyncKey] ?: 0L
            )
        }

    actual suspend fun get(): AppSettings =
        observe().first()

    actual suspend fun save(settings: AppSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[cipherKey] = settings.defaultCipherType.id
            prefs[darkModeKey] = settings.isDarkMode
            prefs[autoSyncKey] = settings.isAutoSyncEnabled
            prefs[lastSyncKey] = settings.lastSyncTimestamp
        }
    }
}