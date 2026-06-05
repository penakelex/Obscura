package org.penakelex.obscura

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.koin.compose.koinInject
import org.penakelex.obscura.data.crypto.GuestCryptoManager
import org.penakelex.obscura.data.storage.TokenStorage
import org.penakelex.obscura.domain.usecase.auth.AccountBootstrapUseCase
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHost
import org.penakelex.obscura.presentation.components.common.ObscuraSnackbarHostState
import org.penakelex.obscura.presentation.navigation.ObscuraNavHost
import org.penakelex.obscura.presentation.theme.ObscuraTheme

@Composable
fun App() {
    val snackbarHostState = remember { ObscuraSnackbarHostState() }
    val tokenStorage: TokenStorage = koinInject()
    val guestCryptoManager: GuestCryptoManager = koinInject()
    val accountBootstrap: AccountBootstrapUseCase = koinInject()

    LaunchedEffect(Unit) {
        val session = tokenStorage.sessionFlow.value
        if (session != null) {
            println("Account boot strap result: ${accountBootstrap()}")
        } else {
            guestCryptoManager.initializeGuestMode()
        }
    }

    ObscuraTheme {
        Scaffold(
            snackbarHost = { ObscuraSnackbarHost(snackbarHostState) }
        ) { padding ->
            ObscuraNavHost(
                snackbarHostState = snackbarHostState,
                modifier = Modifier.padding(padding),
            )
        }
    }
}