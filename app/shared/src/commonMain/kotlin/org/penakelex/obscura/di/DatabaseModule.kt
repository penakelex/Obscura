package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.data.local.db.DatabaseFactory
import org.penakelex.obscura.data.local.db.ObscuraDatabase

val databaseModule: Module = module {
    single<ObscuraDatabase> { get<DatabaseFactory>().create() }
    single { get<ObscuraDatabase>().noteDao() }
}