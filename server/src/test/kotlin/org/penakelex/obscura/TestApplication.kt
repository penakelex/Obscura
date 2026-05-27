package org.penakelex.obscura

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.penakelex.obscura.db.tables.Notes
import org.penakelex.obscura.db.tables.Sessions
import org.penakelex.obscura.db.tables.Users

fun ApplicationTestBuilder.setupTestApp(): HttpClient {
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER",
        driver = "org.h2.Driver"
    )

    transaction {
        SchemaUtils.create(Users, Sessions, Notes)
    }

    application {
        module()
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
        exec("SET REFERENTIAL_INTEGRITY FALSE")
        exec("TRUNCATE TABLE notes")
        exec("TRUNCATE TABLE sessions")
        exec("TRUNCATE TABLE users")
        exec("SET REFERENTIAL_INTEGRITY TRUE")
    }
}