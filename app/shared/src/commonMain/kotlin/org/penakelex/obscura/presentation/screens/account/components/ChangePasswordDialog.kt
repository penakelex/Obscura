package org.penakelex.obscura.presentation.screens.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cancel
import obscura.app.shared.generated.resources.change_password_title
import obscura.app.shared.generated.resources.confirm
import obscura.app.shared.generated.resources.current_password
import obscura.app.shared.generated.resources.new_password
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.components.common.ObscuraTextField
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.error.UiError
import org.penakelex.obscura.presentation.util.error.toDisplayString

@Composable
fun ChangePasswordDialog(
    isOperationInProgress: Boolean,
    onConfirm: (
        currentPassword: String,
        newPassword: String,
        onFieldErrors: (
            currentPasswordError: UiError?,
            newPasswordError: UiError?
        ) -> Unit
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val currentPasswordError = remember {
        mutableStateOf<UiError?>(null)
    }
    val newPasswordError = remember {
        mutableStateOf<UiError?>(null)
    }

    LaunchedEffect(currentPassword.value) {
        currentPasswordError.value = null
    }
    LaunchedEffect(newPassword.value) {
        newPasswordError.value = null
    }

    val isConfirmEnabled = currentPassword.value.isNotBlank() &&
            newPassword.value.isNotBlank() &&
            !isOperationInProgress &&
            currentPasswordError.value == null &&
            newPasswordError.value == null

    AlertDialog(
        onDismissRequest = { if (!isOperationInProgress) onDismiss() },
        title = {
            Text(
                text = stringResource(Res.string.change_password_title),
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(
                    ObscuraDimens.Padding.m,
                ),
            ) {
                ObscuraTextField(
                    value = currentPassword.value,
                    onValueChange = { currentPassword.value = it },
                    label = stringResource(Res.string.current_password),
                    errorMessage = currentPasswordError.value
                        ?.toDisplayString(),
                    enabled = !isOperationInProgress,
                    isPassword = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                ObscuraTextField(
                    value = newPassword.value,
                    onValueChange = { newPassword.value = it },
                    label = stringResource(Res.string.new_password),
                    errorMessage = newPasswordError.value
                        ?.toDisplayString(),
                    enabled = !isOperationInProgress,
                    isPassword = true,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            ObscuraButton(
                text = stringResource(Res.string.confirm),
                onClick = {
                    onConfirm(
                        currentPassword.value,
                        newPassword.value,
                    ) { currentErr, newErr ->
                        currentPasswordError.value = currentErr
                        newPasswordError.value = newErr
                    }
                },
                variant = ButtonVariant.PRIMARY,
                enabled = isConfirmEnabled,
                loading = isOperationInProgress,
            )
        },
        dismissButton = {
            ObscuraButton(
                text = stringResource(Res.string.cancel),
                onClick = onDismiss,
                variant = ButtonVariant.TEXT,
                enabled = !isOperationInProgress,
            )
        },
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}