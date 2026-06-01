package org.penakelex.obscura.data.remote.grpc

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.ForwardingClientCall
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import org.penakelex.obscura.data.storage.TokenStorage

class AuthMetadataInterceptor(
    private val tokenStorage: TokenStorage
) : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val call = next.newCall(method, callOptions)

        return object : ForwardingClientCall
        .SimpleForwardingClientCall<ReqT, RespT>(call) {
            override fun start(
                responseListener: Listener<RespT>,
                headers: Metadata
            ) {
                tokenStorage.sessionFlow.value?.token?.let { token ->
                    headers.put(
                        AUTHORIZATION_KEY,
                        "$BEARER_PREFIX$token"
                    )
                }
                super.start(responseListener, headers)
            }
        }
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "

        val AUTHORIZATION_KEY: Metadata.Key<String> = Metadata.Key.of(
            "authorization",
            Metadata.ASCII_STRING_MARSHALLER
        )
    }
}