package org.penakelex.obscura

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.penakelex.obscura.config.ServerConfig
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.db.tables.Users
import org.testcontainers.containers.PostgreSQLContainer

private val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
    withDatabaseName("obscura_test")
    withUsername("test")
    withPassword("test")
    start()
}

fun ApplicationTestBuilder.setupTestApp(): HttpClient {
    Database.connect(
        url = postgres.jdbcUrl,
        driver = "org.postgresql.Driver",
        user = postgres.username,
        password = postgres.password
    )

    transaction {
        SchemaUtils.create(Users, Sessions, Notes)
    }

    application {
        module(ServerConfig())
    }

    return createClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
    }
}

fun cleanupDatabase() {
    transaction {
        exec("TRUNCATE TABLE notes, sessions, users RESTART IDENTITY CASCADE")
    }
}