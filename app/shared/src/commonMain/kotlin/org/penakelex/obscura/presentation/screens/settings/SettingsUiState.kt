package org.penakelex.obscura.presentation.screens.settings

import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.settings.ThemeMode

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val defaultCipherType: CipherType = CipherType.DEFAULT,
    val isAutoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val isSyncing: Boolean = false,
    val isLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
)