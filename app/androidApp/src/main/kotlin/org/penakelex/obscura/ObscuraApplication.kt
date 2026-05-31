package org.penakelex.obscura

import android.app.Application
import co.touchlab.kermit.Logger
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.penakelex.obscura.di.androidPlatformModule
import org.penakelex.obscura.di.sharedModules

class ObscuraApplication : Application() {
    private val logger = Logger.withTag("ObscuraApplication")

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@ObscuraApplication)
            modules(
                sharedModules +
                        androidPlatformModule(this@ObscuraApplication)
            )
        }

        logger.i { "Obscura application started" }
    }
}