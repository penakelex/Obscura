plugins {
    kotlin("jvm")
    alias(libs.plugins.protobuf)
}

dependencies {
    api(libs.protobuf.java)
    api(libs.protobuf.kotlin)
    api(libs.grpc.stub)
    api(libs.grpc.protobuf)
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
                create("kotlin")
            }
            task.plugins {
                create("grpc")
                create("grpckt")
            }
        }
    }
}