package org.penakelex.obscura.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NavController.HandleNavigationEvents(navigator: Navigator) {
    LaunchedEffect(navigator) {
        navigator.navigationEvents.collectLatest { event ->
            when (event) {
                is NavigationEvent.Navigate -> {
                    navigate(event.route, event.optionsBuilder)
                }
                NavigationEvent.NavigateBack -> popBackStack()
                NavigationEvent.NavigateUp -> navigateUp()
            }
        }
    }
}