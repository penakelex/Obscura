package org.penakelex.obscura.presentation.util.event

import org.penakelex.obscura.presentation.navigation.NavRoute

sealed interface UiEvent {
    data class ShowSnackbar(
        val messageRes: String,
        val args: List<Any> = emptyList(),
        val actionLabel: String? = null,
    ) : UiEvent

    data class Navigate(
        val route: NavRoute,
        val popUpToLogin: Boolean = false,
    ) : UiEvent

    data object NavigateBack : UiEvent
}