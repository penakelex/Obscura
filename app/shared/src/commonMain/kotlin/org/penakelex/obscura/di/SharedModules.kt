package org.penakelex.obscura.di

import org.koin.core.module.Module

val sharedModules: List<Module> = listOf(
    databaseModule,
    repositoryModule,
    networkModule,
    syncModule,
    useCaseModule,
    settingsModule,
    navigationModule,
)