package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.persistence.db.DatabaseFactory
import org.penakelex.obscura.persistence.db.ObscuraDatabase

val databaseModule: Module = module {
    single<ObscuraDatabase> { get<DatabaseFactory>().create() }
    single { get<ObscuraDatabase>().noteDao() }
}