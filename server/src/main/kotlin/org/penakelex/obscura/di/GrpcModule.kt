package org.penakelex.obscura.di

import org.koin.dsl.module
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.grpc.GrpcAuthInterceptor
import org.penakelex.obscura.grpc.GrpcServerManager
import org.penakelex.obscura.grpc.NotesSyncService

fun grpcModule(serverConfig: ServerConfig) = module {
    single { GrpcAuthInterceptor() }

    single {
        NotesSyncService(
            noteRepository = get(),
            sessionRepository = get(),
            validationConfig = serverConfig.validation,
        )
    }

    single {
        GrpcServerManager(
            syncService = get(),
            authInterceptor = get(),
            networkConfig = serverConfig.network,
            grpcServerSettings = serverConfig.server.grpc,
        )
    }
}