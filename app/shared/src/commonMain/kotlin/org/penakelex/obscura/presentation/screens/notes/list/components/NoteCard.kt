package org.penakelex.obscura.presentation.screens.notes.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cipher_aes_gcm_short
import obscura.app.shared.generated.resources.cipher_xchacha20_short
import obscura.app.shared.generated.resources.note_local_only
import obscura.app.shared.generated.resources.note_local_only_description
import obscura.app.shared.generated.resources.untitled_note
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.content.NoteContentParser
import org.penakelex.obscura.presentation.util.date.DateFormatter
import org.penakelex.obscura.presentation.util.date.toDisplayString

@Composable
fun NoteCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = remember(note.content) {
        NoteContentParser.extractTitle(note.content)
    }
    val preview = remember(note.content, title) {
        NoteContentParser.extractPreview(
            content = note.content,
            titleToExclude = title,
        )
    }
    val dateLabel = remember(note.updatedAt) {
        DateFormatter.formatRelative(note.updatedAt)
    }

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "NoteCardContainer",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ObscuraDimens.Padding.m),
            verticalAlignment = Alignment.Top,
        ) {
            AnimatedVisibility(visible = isSelectionMode) {
                Box(
                    modifier = Modifier
                        .padding(end = ObscuraDimens.Padding.s)
                        .size(24.dp)
                        .clickable(onClick = onToggleSelection),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isSelected) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Outlined.Circle
                        },
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title?.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.untitled_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (preview.isNotBlank()) {
                    Text(
                        text = preview,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(
                        ObscuraDimens.Padding.s
                    ),
                ) {
                    if (note.isLocalOnly) {
                        LocalOnlyIndicator()
                    } else {
                        SyncStatusIndicator(
                            status = note.syncStatus,
                            showLabel = true,
                        )
                    }

                    Spacer(Modifier.width(4.dp))

                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = note.cipherType.displayName(),
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = note.cipherType.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.weight(1f))

                    Text(
                        text = dateLabel.toDisplayString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LocalOnlyIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.CloudOff,
            contentDescription = stringResource(
                Res.string.note_local_only_description
            ),
            modifier = Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(Res.string.note_local_only),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CipherType.displayName(): String =
    when (this) {
        CipherType.AES_GCM ->
            stringResource(Res.string.cipher_aes_gcm_short)

        CipherType.XCHACHA20_POLY1305 ->
            stringResource(Res.string.cipher_xchacha20_short)
    }