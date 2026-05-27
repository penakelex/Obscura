plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "org.penakelex.obscura"
version = "1.0.0"
application {
    mainClass = "org.penakelex.obscura.ApplicationKt"
}

dependencies {
    api(projects.core)
    implementation(projects.proto)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)

    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.call.id)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.migration)
    implementation(libs.exposed.migration.jdbc)

    implementation(libs.postgresql)
    implementation(libs.config)
    implementation(libs.dotenv.kotlin)

    implementation(libs.hikaricp)

    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.protobuf.kotlin)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    implementation(libs.password4j)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.ktor.client.content.negotiation)

    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.junit.jupiter)

    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.kotlinx.coroutines.test)
}