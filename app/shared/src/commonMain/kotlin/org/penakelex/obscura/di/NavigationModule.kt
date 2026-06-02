package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.penakelex.obscura.presentation.navigation.Navigator

val navigationModule: Module = module {
    factory { Navigator() }
}