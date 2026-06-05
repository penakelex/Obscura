package org.penakelex.obscura.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose
import org.penakelex.obscura.data.remote.grpc.AuthMetadataInterceptor
import org.penakelex.obscura.data.remote.grpc.GrpcChannelFactory
import org.penakelex.obscura.data.remote.grpc.SyncApiClient
import org.penakelex.obscura.data.remote.http.AuthApiClient
import org.penakelex.obscura.data.remote.http.NotesApiClient
import org.penakelex.obscura.data.remote.http.createHttpClient
import org.penakelex.obscura.data.storage.TokenStorage

val networkModule: Module = module {
    single { createHttpClient() }
    single { AuthApiClient(client = get()) }
    single {
        NotesApiClient(
            client = get(),
            tokenStorage = get<TokenStorage>(),
        )
    }

    single { AuthMetadataInterceptor(tokenStorage = get()) }
    single {
        GrpcChannelFactory.create(
            interceptor = get<AuthMetadataInterceptor>(),
        )
    }
    single {
        SyncApiClient(channel = get())
    } onClose { it?.shutdown() }
}