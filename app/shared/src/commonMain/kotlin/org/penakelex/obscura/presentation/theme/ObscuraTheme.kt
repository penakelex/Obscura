package org.penakelex.obscura.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.penakelex.obscura.domain.gateway.SettingsGateway
import org.penakelex.obscura.domain.model.settings.AppSettings
import org.penakelex.obscura.domain.model.settings.ThemeMode

val LocalObscuraColors = compositionLocalOf { ObscuraColors }

@Composable
fun ObscuraTheme(
    settingsGateway: SettingsGateway = koinInject(),
    content: @Composable () -> Unit,
) {
    val settings by settingsGateway
        .observe()
        .collectAsState(initial = AppSettings())

    val isDarkTheme = shouldUseDarkTheme(settings.themeMode)

    val colorScheme = if (isDarkTheme) DarkColors else LightColors
    val typography = obscuraTypography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = ObscuraShapes,
    ) {
        CompositionLocalProvider(
            LocalObscuraColors provides ObscuraColors,
            content = content,
        )
    }
}

@Composable
private fun shouldUseDarkTheme(mode: ThemeMode): Boolean = when (mode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
}