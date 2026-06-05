package org.penakelex.obscura.presentation.screens.notes.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.close
import obscura.app.shared.generated.resources.corrupted_notes_warning
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@Composable
fun CorruptedNotesBanner(
    count: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = ObscuraDimens.Padding.m,
                    end = ObscuraDimens.Padding.s,
                    top = ObscuraDimens.Padding.m,
                    bottom = ObscuraDimens.Padding.m,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ObscuraDimens.Padding.s),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(ObscuraDimens.Size.iconMedium),
                )
                Text(
                    text = stringResource(
                        Res.string.corrupted_notes_warning,
                        count,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(ObscuraDimens.Size.iconMedium)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.close),
                    modifier = Modifier.size(ObscuraDimens.Size.iconSmall)
                )
            }
        }
    }
}