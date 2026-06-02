package org.penakelex.obscura.domain.model.settings

import kotlinx.serialization.Serializable
import org.penakelex.obscura.domain.model.common.CipherType

@Serializable
data class AppSettings(
    val defaultCipherType: CipherType = CipherType.DEFAULT,
    val themeMode: ThemeMode = ThemeMode.DEFAULT,
    val isAutoSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L
)