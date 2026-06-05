package org.penakelex.obscura.presentation.screens.notes.list.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.deselect_all
import obscura.app.shared.generated.resources.delete_selected
import obscura.app.shared.generated.resources.menu_account
import obscura.app.shared.generated.resources.menu_sessions
import obscura.app.shared.generated.resources.menu_settings
import obscura.app.shared.generated.resources.notes
import obscura.app.shared.generated.resources.search_notes
import obscura.app.shared.generated.resources.select_all
import obscura.app.shared.generated.resources.selected_count
import obscura.app.shared.generated.resources.sync_now
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.screens.notes.list.NotesListUiState

@Composable
fun NotesListTopBar(
    state: NotesListUiState,
    onSearchToggle: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSessionsClick: () -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isSelectionMode -> SelectionModeTopBar(
            state = state,
            onSelectAll = onSelectAll,
            onDeselectAll = onDeselectAll,
            onDeleteSelected = onDeleteSelected,
            onCancelSelection = onCancelSelection,
            modifier = modifier,
        )

        state.isSearchActive -> SearchTopBar(
            query = state.searchQuery,
            onQueryChange = onSearchQueryChange,
            onClose = onSearchToggle,
            modifier = modifier,
        )

        else -> DefaultTopBar(
            isSyncing = state.isSyncing,
            onSearchToggle = onSearchToggle,
            onSyncClick = onSyncClick,
            onSettingsClick = onSettingsClick,
            onAccountClick = onAccountClick,
            onSessionsClick = onSessionsClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun DefaultTopBar(
    isSyncing: Boolean,
    onSearchToggle: () -> Unit,
    onSyncClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSessionsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = { Text(stringResource(Res.string.notes)) },
        modifier = modifier,
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(Res.string.search_notes),
                )
            }
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(8.dp))
            } else {
                IconButton(onClick = onSyncClick) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = stringResource(Res.string.sync_now),
                    )
                }
            }
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                )
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(Res.string.menu_settings)) },
                    onClick = {
                        menuExpanded = false
                        onSettingsClick()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(Res.string.menu_account)) },
                    onClick = {
                        menuExpanded = false
                        onAccountClick()
                    },
                )
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Devices,
                            contentDescription = null,
                        )
                    },
                    text = { Text(stringResource(Res.string.menu_sessions)) },
                    onClick = {
                        menuExpanded = false
                        onSessionsClick()
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(stringResource(Res.string.search_notes))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search,
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Close search",
                )
            }
        },
        actions = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun SelectionModeTopBar(
    state: NotesListUiState,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDeleteSelected: () -> Unit,
    onCancelSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {
            Text(
                stringResource(
                    Res.string.selected_count,
                    state.selectedCount,
                )
            )
        },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onCancelSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                )
            }
        },
        actions = {
            AnimatedVisibility(
                visible = !state.isAllSelected,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ObscuraButton(
                    text = stringResource(Res.string.select_all),
                    onClick = onSelectAll,
                    variant = ButtonVariant.TEXT,
                )
            }
            AnimatedVisibility(
                visible = state.isAllSelected,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ObscuraButton(
                    text = stringResource(Res.string.deselect_all),
                    onClick = onDeselectAll,
                    variant = ButtonVariant.TEXT,
                )
            }
            ObscuraButton(
                text = stringResource(
                    Res.string.delete_selected,
                    state.selectedCount,
                ),
                onClick = onDeleteSelected,
                variant = ButtonVariant.DESTRUCTIVE,
                enabled = state.isAnySelected,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor =
                MaterialTheme.colorScheme.primaryContainer,
            titleContentColor =
                MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor =
                MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor =
                MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    )
}