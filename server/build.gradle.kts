plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
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
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.exposed.migration)
    implementation(libs.exposed.migration.jdbc)

    implementation(libs.postgresql)
    implementation(libs.config)

    implementation(libs.hikaricp)

    implementation(libs.grpc.kotlin.stub)
    implementation(libs.grpc.stub)
    implementation(libs.grpc.netty.shaded)
    implementation(libs.protobuf.kotlin)
}