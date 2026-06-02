package org.penakelex.obscura.presentation.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val ObscuraPrimary = Color(0xFF6C63FF)
private val ObscuraPrimaryDark = Color(0xFF4B44CC)
private val ObscuraSecondary = Color(0xFF03DAC6)
private val ObscuraError = Color(0xFFCF6679)

internal val LightColors = lightColorScheme(
    primary = ObscuraPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E6FF),
    onPrimaryContainer = Color(0xFF1F1B4D),

    secondary = ObscuraSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFFB8F2E8),
    onSecondaryContainer = Color(0xFF00332C),

    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,

    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1A1B21),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1A1B21),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    error = ObscuraError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
)

internal val DarkColors = darkColorScheme(
    primary = Color(0xFFCFCBFF),
    onPrimary = Color(0xFF2B2484),
    primaryContainer = Color(0xFF433BA1),
    onPrimaryContainer = Color(0xFFE8E6FF),

    secondary = Color(0xFF6CD9C8),
    onSecondary = Color(0xFF003832),
    secondaryContainer = Color(0xFF005049),
    onSecondaryContainer = Color(0xFFB8F2E8),

    tertiary = Color(0xFFEFB7C6),
    onTertiary = Color(0xFF492531),

    background = Color(0xFF0F0D14),
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF0F0D14),
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
)

object ObscuraColors {
    val codeBackground = Color(0xFF1E1E2E)
    val codeText = Color(0xFFA6E3A1)
    val linkColor = Color(0xFF89B4FA)

    val syncStatusSynced = Color(0xFF4CAF50)
    val syncStatusPending = Color(0xFFFFC107)
    val syncStatusConflict = Color(0xFFFF5722)
    val syncStatusError = Color(0xFFE53935)
}