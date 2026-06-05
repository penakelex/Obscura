package org.penakelex.obscura.presentation.screens.notes.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.sync_status_conflict
import obscura.app.shared.generated.resources.sync_status_pending
import obscura.app.shared.generated.resources.sync_status_synced
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.common.SyncStatus
import org.penakelex.obscura.presentation.theme.LocalObscuraColors
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun SyncStatusIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
) {
    val colors = LocalObscuraColors.current
    val (color, labelRes) = when (status) {
        SyncStatus.SYNCED -> colors.syncStatusSynced to
                Res.string.sync_status_synced
        SyncStatus.PENDING -> colors.syncStatusPending to
                Res.string.sync_status_pending
        SyncStatus.CONFLICT -> colors.syncStatusConflict to
                Res.string.sync_status_conflict
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ObscuraDimens.Size.syncIndicatorSize)
                .clip(CircleShape)
                .background(color),
        )

        if (showLabel) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}