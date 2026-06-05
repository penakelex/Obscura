package org.penakelex.obscura.presentation.util.event

import androidx.compose.runtime.Composable
import obscura.app.shared.generated.resources.Res
import obscura.app.shared.generated.resources.action_details
import obscura.app.shared.generated.resources.action_retry
import obscura.app.shared.generated.resources.action_undo
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun SnackbarAction.toDisplayString(): String = when (this) {
    SnackbarAction.Undo -> stringResource(Res.string.action_undo)
    SnackbarAction.Retry -> stringResource(Res.string.action_retry)
    SnackbarAction.OpenDetails -> stringResource(Res.string.action_details)
}

suspend fun SnackbarAction.toDisplayLabel(): String = when (this) {
    SnackbarAction.Undo -> getString(Res.string.action_undo)
    SnackbarAction.Retry -> getString(Res.string.action_retry)
    SnackbarAction.OpenDetails -> getString(Res.string.action_details)
}