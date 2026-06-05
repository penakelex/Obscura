package org.penakelex.obscura.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.koin.compose.koinInject
import org.penakelex.obscura.domain.model.auth.SessionState
import org.penakelex.obscura.domain.usecase.auth.session.ObserveSessionUseCase
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.screens.account.AccountScreen
import org.penakelex.obscura.presentation.screens.auth.login.LoginScreen
import org.penakelex.obscura.presentation.screens.auth.register.RegisterScreen
import org.penakelex.obscura.presentation.screens.notes.editor.NoteEditorScreen
import org.penakelex.obscura.presentation.screens.notes.list.NotesListScreen
import org.penakelex.obscura.presentation.screens.sessions.SessionsScreen
import org.penakelex.obscura.presentation.screens.settings.SettingsScreen
import org.penakelex.obscura.presentation.screens.splash.SplashScreen

@Composable
fun ObscuraNavHost(
    snackbarHostState: ObscuraSnackbarHostState,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    navigator: Navigator = koinInject(),
    observeSession: ObserveSessionUseCase = koinInject(),
) {
    val sessionState by observeSession().collectAsState()

    navController.HandleNavigationEvents(navigator)

    val startDestination: NavRoute = when (sessionState) {
        is SessionState.Loading -> NavRoute.Splash
        is SessionState.Unauthenticated -> NavRoute.Main.NotesList
        is SessionState.Authenticated -> NavRoute.Main.NotesList
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<NavRoute.Splash> {
            SplashScreen()
        }
        composable<NavRoute.Auth.Login> {
            LoginScreen(snackbarHostState = snackbarHostState)
        }
        composable<NavRoute.Auth.Register> {
            RegisterScreen(snackbarHostState = snackbarHostState)
        }
        composable<NavRoute.Main.NotesList> {
            NotesListScreen(snackbarHostState = snackbarHostState)
        }
        composable<NavRoute.Main.NoteEditor> { backStackEntry ->
            val route: NavRoute.Main.NoteEditor = backStackEntry.toRoute()
            NoteEditorScreen(
                noteId = route.noteId,
                snackbarHostState = snackbarHostState,
            )
        }
        composable<NavRoute.Main.Settings> {
            SettingsScreen(
                snackbarHostState = snackbarHostState,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<NavRoute.Main.Account> {
            AccountScreen(
                snackbarHostState = snackbarHostState,
                onBackClick = { navController.popBackStack() },
            )
        }
        composable<NavRoute.Main.Sessions> {
            SessionsScreen(
                snackbarHostState = snackbarHostState,
                onBackClick = { navController.popBackStack() },
            )
        }
    }

    LaunchedEffect(sessionState) {
        val currentRoute = navController.currentDestination?.route
        when (sessionState) {
            is SessionState.Authenticated -> {
                val isInAuth = currentRoute?.let { route ->
                    route.contains("Login") || route.contains("Register")
                } == true
                if (isInAuth) {
                    navigator.navigate(NavRoute.Main.NotesList) {
                        popUpTo(NavRoute.Auth.Login) { inclusive = true }
                        popUpTo(NavRoute.Auth.Register) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            is SessionState.Unauthenticated -> {
                val isInProtectedScreen = currentRoute?.let { route ->
                    route.contains("Settings") ||
                            route.contains("Account") ||
                            route.contains("Sessions") ||
                            route.contains("NoteEditor")
                } == true
                if (isInProtectedScreen) {
                    navigator.navigate(NavRoute.Main.NotesList) {
                        popUpTo(0) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
            SessionState.Loading -> {  }
        }
    }
}