package org.penakelex.obscura.presentation.navigation

import androidx.navigation.NavOptionsBuilder
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class Navigator {
    private val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents = _navigationEvents.receiveAsFlow()

    fun navigate(route: NavRoute, builder: NavOptionsBuilder.() -> Unit = {}) {
        _navigationEvents.trySend(NavigationEvent.Navigate(route, builder))
    }

    fun navigateBack() {
        _navigationEvents.trySend(NavigationEvent.NavigateBack)
    }

    fun navigateUp() {
        _navigationEvents.trySend(NavigationEvent.NavigateUp)
    }
}
