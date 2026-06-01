package org.penakelex.obscura.data.remote.grpc

import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import org.penakelex.obscura.data.remote.config.NetworkConfig


object GrpcChannelFactory {
    fun create(
        host: String = NetworkConfig.Grpc.HOST,
        port: Int = NetworkConfig.Grpc.PORT,
        useTls: Boolean = NetworkConfig.Grpc.USE_TLS,
        maxInboundMessageSize: Int =
            NetworkConfig.Grpc.MAX_INBOUND_MESSAGE_SIZE,
    ): ManagedChannel {
        val builder = OkHttpChannelBuilder
            .forAddress(host, port)
            .maxInboundMessageSize(maxInboundMessageSize)

        if (useTls) {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        return builder.build()
    }
}