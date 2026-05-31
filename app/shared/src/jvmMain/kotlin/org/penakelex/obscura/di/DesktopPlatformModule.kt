package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.crypto.CryptoProvider
import org.penakelex.obscura.persistence.db.DatabaseFactory

val desktopPlatformModule: Module = module {
    single { DatabaseFactory() }
    single { CryptoProvider() }
}