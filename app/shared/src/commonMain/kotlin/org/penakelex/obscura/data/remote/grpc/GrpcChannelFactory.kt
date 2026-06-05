package org.penakelex.obscura.data.remote.grpc

import io.grpc.ManagedChannel
import io.grpc.okhttp.OkHttpChannelBuilder
import org.penakelex.obscura.data.remote.config.NetworkConfig

class GrpcChannelFactory(
    private val authInterceptor: AuthMetadataInterceptor,
) {
    fun create(): ManagedChannel {
        val builder = OkHttpChannelBuilder
            .forAddress(
                NetworkConfig.Grpc.HOST,
                NetworkConfig.Grpc.PORT,
            )
            .maxInboundMessageSize(
                NetworkConfig.Grpc.MAX_INBOUND_MESSAGE_SIZE,
            )
            .intercept(authInterceptor)

        if (NetworkConfig.Grpc.USE_TLS) {
            builder.useTransportSecurity()
        } else {
            builder.usePlaintext()
        }

        return builder.build()
    }
}