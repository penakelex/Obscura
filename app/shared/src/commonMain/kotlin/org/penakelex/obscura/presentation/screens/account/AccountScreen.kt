package org.penakelex.obscura.presentation.screens.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.account
import obscura.app.shared.generated.resources.cancel
import obscura.app.shared.generated.resources.logout
import obscura.app.shared.generated.resources.logout_all_confirm_message
import obscura.app.shared.generated.resources.logout_all_confirm_message_unsaved
import obscura.app.shared.generated.resources.logout_all_confirm_title
import obscura.app.shared.generated.resources.logout_confirm_message
import obscura.app.shared.generated.resources.logout_confirm_message_unsaved
import obscura.app.shared.generated.resources.logout_confirm_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.penakelex.obscura.presentation.components.common.FullScreenLoading
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.components.common.ObscuraTopBar
import org.penakelex.obscura.presentation.components.dialogs.ConfirmDialog
import org.penakelex.obscura.presentation.screens.account.components.ChangeEmailDialog
import org.penakelex.obscura.presentation.screens.account.components.ChangePasswordDialog
import org.penakelex.obscura.presentation.screens.account.components.DangerZoneSection
import org.penakelex.obscura.presentation.screens.account.components.DeleteAccountDialog
import org.penakelex.obscura.presentation.screens.account.components.ProfileSection
import org.penakelex.obscura.presentation.screens.account.components.SecuritySection
import org.penakelex.obscura.presentation.screens.account.components.SessionSection
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.event.UiEvent
import org.penakelex.obscura.presentation.util.event.toDisplayLabel

@Composable
fun AccountScreen(
    snackbarHostState: ObscuraSnackbarHostState,
    onBackClick: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val logoutTitle = stringResource(Res.string.logout_confirm_title)
    val logoutAllTitle =
        stringResource(Res.string.logout_all_confirm_title)
    val confirmText = stringResource(Res.string.logout)
    val cancelText = stringResource(Res.string.cancel)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    val actionLabel = event.action?.toDisplayLabel()
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = actionLabel,
                    )
                }
                is UiEvent.Navigate, UiEvent.NavigateBack -> {
                }
            }
        }
    }

    if (state.isPasswordDialogVisible) {
        ChangePasswordDialog(
            isOperationInProgress = state.isOperationInProgress,
            onConfirm = viewModel::changePassword,
            onDismiss = viewModel::onPasswordDialogDismiss,
        )
    }

    if (state.isEmailDialogVisible) {
        ChangeEmailDialog(
            isOperationInProgress = state.isOperationInProgress,
            onConfirm = viewModel::changeEmail,
            onDismiss = viewModel::onEmailDialogDismiss,
        )
    }

    if (state.isDeleteDialogVisible) {
        DeleteAccountDialog(
            isOperationInProgress = state.isOperationInProgress,
            onConfirm = viewModel::deleteAccount,
            onDismiss = viewModel::onDeleteDialogDismiss,
        )
    }

    if (state.isLogoutDialogVisible) {
        val message = if (state.pendingNotesCount > 0) {
            stringResource(
                Res.string.logout_confirm_message_unsaved,
                state.pendingNotesCount
            )
        } else {
            stringResource(Res.string.logout_confirm_message)
        }

        ConfirmDialog(
            title = logoutTitle,
            message = message,
            confirmText = confirmText,
            dismissText = cancelText,
            onConfirm = {
                viewModel.onLogoutDialogDismiss()
                viewModel.onLogoutConfirmed()
            },
            onDismiss = viewModel::onLogoutDialogDismiss,
            isDestructive = true,
        )
    }

    if (state.isLogoutAllDialogVisible) {
        val message = if (state.pendingNotesCount > 0) {
            stringResource(
                Res.string.logout_all_confirm_message_unsaved,
                state.pendingNotesCount
            )
        } else {
            stringResource(Res.string.logout_all_confirm_message)
        }

        ConfirmDialog(
            title = logoutAllTitle,
            message = message,
            confirmText = confirmText,
            dismissText = cancelText,
            onConfirm = {
                viewModel.onLogoutAllDialogDismiss()
                viewModel.onLogoutAllConfirmed()
            },
            onDismiss = viewModel::onLogoutAllDialogDismiss,
            isDestructive = true,
        )
    }

    when {
        state.isLoading -> FullScreenLoading()
        else -> AccountScreenContent(
            state = state,
            onBackClick = onBackClick,
            onChangePasswordClick = viewModel::onChangePasswordClick,
            onChangeEmailClick = viewModel::onChangeEmailClick,
            onDeleteAccountClick = viewModel::onDeleteAccountClick,
            onLogoutClick = viewModel::onLogoutClick,
            onLogoutAllClick = viewModel::onLogoutAllClick,
        )
    }
}

@Composable
private fun AccountScreenContent(
    state: AccountUiState,
    onBackClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onLogoutAllClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            ObscuraTopBar(
                title = stringResource(Res.string.account),
                onBackClick = onBackClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(ObscuraDimens.Padding.m),
            verticalArrangement = Arrangement.spacedBy(
                ObscuraDimens.Padding.l,
            ),
        ) {
            ProfileSection(email = state.email)

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            SecuritySection(
                onChangePasswordClick = onChangePasswordClick,
                onChangeEmailClick = onChangeEmailClick,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            SessionSection(
                onLogoutClick = onLogoutClick,
                onLogoutAllClick = onLogoutAllClick,
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            DangerZoneSection(
                onDeleteAccountClick = onDeleteAccountClick,
            )
        }
    }
}