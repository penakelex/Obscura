package org.penakelex.obscura.presentation.components.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.penakelex.obscura.presentation.util.message.UiMessage
import org.penakelex.obscura.presentation.util.message.toDisplayMessage

@Stable
class ObscuraSnackbarHostState {
    val hostState: SnackbarHostState = SnackbarHostState()

    var currentMessage: UiMessage? by mutableStateOf(null)
        private set

    suspend fun showSnackbar(
        message: UiMessage,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ): SnackbarResult {
        currentMessage = message
        return try {
            hostState.showSnackbar(
                message = message.toDisplayMessage(),
                actionLabel = actionLabel,
                withDismissAction = withDismissAction,
                duration = duration,
            )
        } finally {
            currentMessage = null
        }
    }
}