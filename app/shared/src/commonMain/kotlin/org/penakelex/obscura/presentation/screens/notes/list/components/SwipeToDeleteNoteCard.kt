package org.penakelex.obscura.presentation.screens.notes.list.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.delete
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.domain.model.note.Note
import org.penakelex.obscura.presentation.theme.ObscuraDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteNoteCard(
    note: Note,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { totalDistance ->
            totalDistance * 0.35f
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = { SwipeDeleteBackground() },
        modifier = modifier.fillMaxWidth(),
        enableDismissFromStartToEnd = false,
        gesturesEnabled = !isSelectionMode,
        onDismiss = { onDelete() },
    ) {
        NoteCard(
            note = note,
            isSelected = isSelected,
            isSelectionMode = isSelectionMode,
            onClick = onClick,
            onLongClick = onLongClick,
            onToggleSelection = onToggleSelection,
        )
    }
}

@Composable
private fun SwipeDeleteBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
            )
            .padding(ObscuraDimens.Padding.s),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ObscuraDimens.Padding.m,
                vertical = ObscuraDimens.Padding.s,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                ObscuraDimens.Padding.xs,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(Res.string.delete),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}