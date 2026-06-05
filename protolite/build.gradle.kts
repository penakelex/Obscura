plugins {
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

dependencies {
    api(libs.protobuf.kotlin.lite)
    api(libs.grpc.stub)
    api(libs.grpc.protobuf.lite)
    api(libs.grpc.kotlin.stub)
    api(libs.kotlinx.coroutines.core)
}

protobuf {
    protoc {
        artifact = libs.protoc.compiler.get().toString()
    }
    plugins {
        create("grpc") {
            artifact = libs.protoc.gen.grpc.java.get().toString()
        }
        create("grpckt") {
            artifact = "${libs.protoc.gen.grpc.kotlin.get()}:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                // ✅ 'java' уже существует (создан автоматически kotlin("jvm"))
                // Используем getByName вместо create
                getByName("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
            task.plugins {
                create("grpc") {
                    option("lite")
                }
                create("grpckt")
            }
        }
    }
}

// Читаем .proto файлы из модуля :proto
sourceSets {
    main {
        proto {
            srcDir(project(":proto").file("src/main/proto"))
        }
    }
}