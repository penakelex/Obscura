package org.penakelex.obscura.presentation.util.event

import org.penakelex.obscura.presentation.navigation.NavRoute
import org.penakelex.obscura.presentation.util.message.UiMessage

sealed interface UiEvent {
    data class ShowSnackbar(
        val message: UiMessage,
        val action: SnackbarAction? = null,
    ) : UiEvent

    data class Navigate(
        val route: NavRoute,
        val popUpToLogin: Boolean = false,
    ) : UiEvent

    data object NavigateBack : UiEvent
}