package org.penakelex.obscura.presentation.navigation

import androidx.navigation.NavOptionsBuilder


sealed interface NavigationEvent {
    data class Navigate(
        val route: NavRoute,
        val optionsBuilder: NavOptionsBuilder.() -> Unit = {},
    ) : NavigationEvent

    data object NavigateBack : NavigationEvent
    data object NavigateUp : NavigationEvent
}