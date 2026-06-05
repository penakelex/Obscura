package org.penakelex.obscura.presentation.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.auto_sync
import obscura.app.shared.generated.resources.auto_sync_description
import obscura.app.shared.generated.resources.last_sync
import obscura.app.shared.generated.resources.sync_guest_message
import obscura.app.shared.generated.resources.sync_now
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.date.DateFormatter
import org.penakelex.obscura.presentation.util.date.toDisplayString

@Composable
fun SyncSettings(
    isAutoSyncEnabled: Boolean,
    lastSyncTimestamp: Long,
    isSyncing: Boolean,
    isAuthenticated: Boolean,
    onAutoSyncToggled: (Boolean) -> Unit,
    onSyncNowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.m,
        ),
    ) {
        if (!isAuthenticated) {
            GuestSyncMessage()
        } else {
            AutoSyncToggle(
                enabled = isAutoSyncEnabled,
                onToggled = onAutoSyncToggled,
            )
            LastSyncRow(
                lastSyncTimestamp = lastSyncTimestamp,
                isSyncing = isSyncing,
                onSyncNowClick = onSyncNowClick,
            )
        }
    }
}

@Composable
private fun GuestSyncMessage(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.s,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(
            ObscuraDimens.Padding.s,
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(ObscuraDimens.Size.iconMedium),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.sync_guest_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AutoSyncToggle(
    enabled: Boolean,
    onToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.s,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.auto_sync),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(
                    Res.string.auto_sync_description,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(ObscuraDimens.Padding.m))
        Switch(
            checked = enabled,
            onCheckedChange = onToggled,
        )
    }
}

@Composable
private fun LastSyncRow(
    lastSyncTimestamp: Long,
    isSyncing: Boolean,
    onSyncNowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dateLabel = remember(lastSyncTimestamp) {
        if (lastSyncTimestamp == 0L) {
            null
        } else {
            DateFormatter.formatRelative(lastSyncTimestamp)
        }
    }
    val lastSyncDisplay = dateLabel?.toDisplayString() ?: "—"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.s,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    Res.string.last_sync,
                    lastSyncDisplay,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(ObscuraDimens.Padding.m))
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            ObscuraButton(
                text = stringResource(Res.string.sync_now),
                onClick = onSyncNowClick,
                variant = ButtonVariant.SECONDARY,
            )
        }
    }
}