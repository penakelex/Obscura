package org.penakelex.obscura.data.remote.http

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.penakelex.obscura.data.remote.config.NetworkConfig

fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true

    engine {
        config {
            retryOnConnectionFailure(true)
            followRedirects(true)
        }
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            isLenient = false
        })
    }

    install(HttpTimeout) {
        requestTimeoutMillis = NetworkConfig.Rest.TIMEOUT_MILLIS
        connectTimeoutMillis = NetworkConfig.Rest.TIMEOUT_MILLIS
        socketTimeoutMillis = NetworkConfig.Rest.TIMEOUT_MILLIS
    }

    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
}