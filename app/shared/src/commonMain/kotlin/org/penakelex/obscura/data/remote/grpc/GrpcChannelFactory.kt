package org.penakelex.obscura.data.remote.grpc

import io.grpc.ClientInterceptor
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
        interceptor: ClientInterceptor? = null,
    ): ManagedChannel {
        val builder = OkHttpChannelBuilder
            .forAddress(host, port)
            .maxInboundMessageSize(maxInboundMessageSize)

        if (useTls) {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        if (interceptor != null) {
            builder.intercept(interceptor)
        }

        return builder.build()
    }
}