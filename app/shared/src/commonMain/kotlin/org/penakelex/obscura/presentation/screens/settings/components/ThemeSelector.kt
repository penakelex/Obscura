package org.penakelex.obscura.presentation.screens.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.theme_dark
import obscura.app.shared.generated.resources.theme_light
import obscura.app.shared.generated.resources.theme_system
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.settings.ThemeMode
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun ThemeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.xs,
        ),
    ) {
        ThemeMode.entries.forEach { mode ->
            ThemeOption(
                mode = mode,
                isSelected = mode == selectedMode,
                onClick = { onModeSelected(mode) },
            )
        }
    }
}

@Composable
private fun ThemeOption(
    mode: ThemeMode,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = when (mode) {
        ThemeMode.LIGHT -> stringResource(Res.string.theme_light)
        ThemeMode.DARK -> stringResource(Res.string.theme_dark)
        ThemeMode.SYSTEM -> stringResource(Res.string.theme_system)
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.s,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.m,
        ),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}