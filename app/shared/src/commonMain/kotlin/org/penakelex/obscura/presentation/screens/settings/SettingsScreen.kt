package org.penakelex.obscura.presentation.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.account
import obscura.app.shared.generated.resources.appearance
import obscura.app.shared.generated.resources.cancel
import obscura.app.shared.generated.resources.logout
import obscura.app.shared.generated.resources.logout_all_confirm_message
import obscura.app.shared.generated.resources.logout_all_confirm_title
import obscura.app.shared.generated.resources.logout_confirm_message
import obscura.app.shared.generated.resources.logout_confirm_title
import obscura.app.shared.generated.resources.security
import obscura.app.shared.generated.resources.settings
import obscura.app.shared.generated.resources.sync
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.penakelex.obscura.domain.model.common.CipherType
import org.penakelex.obscura.domain.model.settings.ThemeMode
import org.penakelex.obscura.presentation.components.common.FullScreenLoading
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.common.ObscuraTopBar
import org.penakelex.obscura.presentation.components.dialogs.ConfirmDialog
import org.penakelex.obscura.presentation.screens.settings.components.AccountLinks
import org.penakelex.obscura.presentation.screens.settings.components.CipherSelector
import org.penakelex.obscura.presentation.screens.settings.components.SettingsSection
import org.penakelex.obscura.presentation.screens.settings.components.SyncSettings
import org.penakelex.obscura.presentation.screens.settings.components.ThemeSelector
import org.penakelex.obscura.presentation.util.event.SnackbarAction
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.event.toDisplayLabel

@Composable
fun SettingsScreen(
    snackbarHostState: ObscuraSnackbarHostState,
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val logoutTitle = stringResource(Res.string.logout_confirm_title)
    val logoutMessage =
        stringResource(Res.string.logout_confirm_message)
    val logoutAllTitle = stringResource(
        Res.string.logout_all_confirm_title,
    )
    val logoutAllMessage = stringResource(
        Res.string.logout_all_confirm_message,
    )
    val confirmText = stringResource(Res.string.logout)
    val cancelText = stringResource(Res.string.cancel)
    val showLogoutDialog = remember { mutableStateOf(false) }
    val showLogoutAllDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val actionLabel = event.action?.toDisplayLabel()
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = actionLabel,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        when (event.action) {
                            SnackbarAction.Retry ->
                                viewModel.onSyncNowClick()
                            SnackbarAction.Undo,
                            SnackbarAction.OpenDetails,
                            null -> {
                            }
                        }
                    }
                }
                is UiEvent.Navigate, UiEvent.NavigateBack -> {
                }
            }
        }
    }

    if (showLogoutDialog.value) {
        ConfirmDialog(
            title = logoutTitle,
            message = logoutMessage,
            confirmText = confirmText,
            dismissText = cancelText,
            onConfirm = {
                showLogoutDialog.value = false
                viewModel.onLogoutConfirmed()
            },
            onDismiss = { showLogoutDialog.value = false },
            isDestructive = true,
        )
    }

    if (showLogoutAllDialog.value) {
        ConfirmDialog(
            title = logoutAllTitle,
            message = logoutAllMessage,
            confirmText = confirmText,
            dismissText = cancelText,
            onConfirm = {
                showLogoutAllDialog.value = false
                viewModel.onLogoutAllConfirmed()
            },
            onDismiss = { showLogoutAllDialog.value = false },
            isDestructive = true,
        )
    }

    when {
        state.isLoading -> FullScreenLoading()
        else -> SettingsScreenContent(
            state = state,
            onBackClick = onBackClick,
            onThemeSelected = viewModel::onThemeSelected,
            onCipherSelected = viewModel::onCipherSelected,
            onAutoSyncToggled = viewModel::onAutoSyncToggled,
            onSyncNowClick = viewModel::onSyncNowClick,
            onAccountClick = viewModel::onAccountClick,
            onSessionsClick = viewModel::onSessionsClick,
            onLogoutClick = { showLogoutDialog.value = true },
            onLogoutAllClick = { showLogoutAllDialog.value = true },
            onLoginClick = viewModel::onLoginClick,
            onRegisterClick = viewModel::onRegisterClick,
        )
    }
}

@Composable
private fun SettingsScreenContent(
    state: SettingsUiState,
    onBackClick: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit,
    onCipherSelected: (CipherType) -> Unit,
    onAutoSyncToggled: (Boolean) -> Unit,
    onSyncNowClick: () -> Unit,
    onAccountClick: () -> Unit,
    onSessionsClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLogoutAllClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            ObscuraTopBar(
                title = stringResource(Res.string.settings),
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            item(key = "appearance") {
                SettingsSection(
                    title = stringResource(Res.string.appearance),
                ) {
                    ThemeSelector(
                        selectedMode = state.themeMode,
                        onModeSelected = onThemeSelected,
                    )
                }
            }
            item(key = "security") {
                SettingsSection(
                    title = stringResource(Res.string.security),
                ) {
                    CipherSelector(
                        selectedCipher = state.defaultCipherType,
                        onCipherSelected = onCipherSelected,
                    )
                }
            }
            item(key = "sync") {
                SettingsSection(
                    title = stringResource(Res.string.sync),
                ) {
                    SyncSettings(
                        isAutoSyncEnabled = state.isAutoSyncEnabled,
                        lastSyncTimestamp = state.lastSyncTimestamp,
                        isSyncing = state.isSyncing,
                        isAuthenticated = state.isAuthenticated,
                        onAutoSyncToggled = onAutoSyncToggled,
                        onSyncNowClick = onSyncNowClick,
                    )
                }
            }
            item(key = "account") {
                SettingsSection(
                    title = stringResource(Res.string.account),
                ) {
                    AccountLinks(
                        isAuthenticated = state.isAuthenticated,
                        onAccountClick = onAccountClick,
                        onSessionsClick = onSessionsClick,
                        onLogoutClick = onLogoutClick,
                        onLogoutAllClick = onLogoutAllClick,
                        onLoginClick = onLoginClick,
                        onRegisterClick = onRegisterClick,
                    )
                }
            }
        }
    }
}