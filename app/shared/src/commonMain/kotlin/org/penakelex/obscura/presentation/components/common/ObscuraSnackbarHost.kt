package org.penakelex.obscura.presentation.components.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.penakelex.obscura.presentation.util.message.UiMessage

@Composable
fun ObscuraSnackbarHost(
    hostState: ObscuraSnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val currentMessage = hostState.currentMessage

    SnackbarHost(
        hostState = hostState.hostState,
        modifier = modifier,
    ) { data ->
        val (containerColor, contentColor) = when (currentMessage) {
            is UiMessage.Success ->
                MaterialTheme.colorScheme.primary to
                        MaterialTheme.colorScheme.onPrimary
            is UiMessage.Error ->
                MaterialTheme.colorScheme.error to
                        MaterialTheme.colorScheme.onError
            is UiMessage.Warning ->
                MaterialTheme.colorScheme.tertiary to
                        MaterialTheme.colorScheme.onTertiary
            null ->
                MaterialTheme.colorScheme.inverseSurface to
                        MaterialTheme.colorScheme.inverseOnSurface
        }

        Snackbar(
            snackbarData = data,
            containerColor = containerColor,
            contentColor = contentColor,
            actionColor = contentColor,
            shape = MaterialTheme.shapes.medium,
        )
    }
}