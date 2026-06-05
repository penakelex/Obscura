package org.penakelex.obscura.di

import org.koin.core.module.Module

val sharedModules: List<Module> = listOf(
    databaseModule,
    cryptoModule,
    repositoryModule,
    networkModule,
    syncModule,
    useCaseModule,
    settingsModule,
    navigationModule,
    presentationModule,
)