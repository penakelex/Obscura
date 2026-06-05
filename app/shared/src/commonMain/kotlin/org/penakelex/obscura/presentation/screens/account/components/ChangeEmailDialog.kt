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
import androidx.compose.ui.text.input.KeyboardType
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.cancel
import obscura.app.shared.generated.resources.change_email_title
import obscura.app.shared.generated.resources.confirm
import obscura.app.shared.generated.resources.current_password
import obscura.app.shared.generated.resources.email
import obscura.app.shared.generated.resources.email_placeholder
import org.jetbrains.compose.resources.stringResource
import org.penakelex.obscura.presentation.components.common.ButtonVariant
import org.penakelex.obscura.presentation.components.common.ObscuraButton
import org.penakelex.obscura.presentation.components.common.ObscuraTextField
import org.penakelex.obscura.presentation.theme.ObscuraDimens
import org.penakelex.obscura.presentation.util.error.UiError
import org.penakelex.obscura.presentation.util.error.toDisplayString

@Composable
fun ChangeEmailDialog(
    isOperationInProgress: Boolean,
    onConfirm: (
        currentPassword: String,
        newEmail: String,
        onFieldErrors: (
            currentPasswordError: UiError?,
            emailError: UiError?
        ) -> Unit
    ) -> Unit,
    onDismiss: () -> Unit,
) {
    val currentPassword = remember { mutableStateOf("") }
    val newEmail = remember { mutableStateOf("") }
    val currentPasswordError = remember {
        mutableStateOf<UiError?>(null)
    }
    val emailError = remember { mutableStateOf<UiError?>(null) }

    LaunchedEffect(currentPassword.value) {
        currentPasswordError.value = null
    }
    LaunchedEffect(newEmail.value) {
        emailError.value = null
    }

    val isConfirmEnabled = currentPassword.value.isNotBlank() &&
            newEmail.value.isNotBlank() &&
            !isOperationInProgress &&
            currentPasswordError.value == null &&
            emailError.value == null

    AlertDialog(
        onDismissRequest = { if (!isOperationInProgress) onDismiss() },
        title = {
            Text(
                text = stringResource(Res.string.change_email_title),
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
                    value = newEmail.value,
                    onValueChange = { newEmail.value = it },
                    label = stringResource(Res.string.email),
                    placeholder = stringResource(
                        Res.string.email_placeholder,
                    ),
                    errorMessage = emailError.value?.toDisplayString(),
                    enabled = !isOperationInProgress,
                    keyboardType = KeyboardType.Email,
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
                        newEmail.value,
                    ) { currentErr, emailErr ->
                        currentPasswordError.value = currentErr
                        emailError.value = emailErr
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