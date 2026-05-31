package org.penakelex.obscura

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import co.touchlab.kermit.Logger
import org.koin.core.context.GlobalContext.startKoin
import org.penakelex.obscura.di.desktopPlatformModule
import org.penakelex.obscura.di.sharedModules

fun main() = application {
    startKoin {
        modules(sharedModules + desktopPlatformModule)
    }

    Logger.withTag("ObscuraDesktop").i { "Application started" }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Obscura",
    ) {
    }
}