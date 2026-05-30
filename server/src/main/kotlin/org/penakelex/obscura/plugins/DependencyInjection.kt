package org.penakelex.obscura.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.di.appModule
import org.penakelex.obscura.di.grpcModule

fun Application.configureDependencyInjection(
    serverConfig: ServerConfig
) {
    install(Koin) {
        slf4jLogger()
        modules(
            appModule(serverConfig),
            grpcModule(serverConfig)
        )
    }
}