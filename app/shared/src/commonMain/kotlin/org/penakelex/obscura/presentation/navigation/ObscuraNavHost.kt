package org.penakelex.obscura.presentation.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import org.koin.compose.koinInject
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.usecase.auth.ObserveSessionUseCase

@Composable
fun ObscuraNavHost(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    observeSession: ObserveSessionUseCase = koinInject(),
) {
    val sessionState by observeSession().collectAsState()

    val startDestination: NavRoute = when (sessionState) {
        is SessionState.Loading -> NavRoute.Splash
        is SessionState.Unauthenticated -> NavRoute.Auth.Login
        is SessionState.Authenticated -> NavRoute.Main.NotesList
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        splashScreen(navController)
        authGraph(navController, snackbarHostState)
        mainGraph(navController, snackbarHostState)
    }

    LaunchedEffect(sessionState) {
        if (sessionState is SessionState.Unauthenticated) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && !currentRoute.contains("Auth")) {
                navController.navigate(NavRoute.Auth.Login) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }
}

private fun NavGraphBuilder.splashScreen(
    navController: NavHostController,
) {
    composable<NavRoute.Splash> {
        // TODO: SplashScreen()
    }
}

private fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    navigation<NavRoute.Auth>(
        startDestination = NavRoute.Auth.Login,
    ) {
        composable<NavRoute.Auth.Login> {
            // TODO: LoginScreen(snackbarHostState = snackbarHostState)
        }

        composable<NavRoute.Auth.Register> {
            // TODO: RegisterScreen(snackbarHostState = snackbarHostState)
        }
    }
}

private fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    navigation<NavRoute.Main>(
        startDestination = NavRoute.Main.NotesList,
    ) {
        composable<NavRoute.Main.NotesList> {
            // TODO: NotesListScreen(snackbarHostState = snackbarHostState)
        }

        composable<NavRoute.Main.NoteEditor> { backStackEntry ->
            val route: NavRoute.Main.NoteEditor = backStackEntry.toRoute()
            // TODO: NoteEditorScreen(
            //   noteId = route.noteId,
            //   snackbarHostState = snackbarHostState
            // )
        }

        composable<NavRoute.Main.Settings> {
            // TODO: SettingsScreen(snackbarHostState = snackbarHostState)
        }

        composable<NavRoute.Main.Account> {
            // TODO: AccountScreen(snackbarHostState = snackbarHostState)
        }

        composable<NavRoute.Main.Sessions> {
            // TODO: SessionsScreen(snackbarHostState = snackbarHostState)
        }
    }
}