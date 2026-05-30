package org.penakelex.obscura.grpc

import io.grpc.Context
import io.grpc.Contexts
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import org.slf4j.LoggerFactory

class GrpcAuthInterceptor : ServerInterceptor {
    private val logger =
        LoggerFactory.getLogger(GrpcAuthInterceptor::class.java)

    override fun <ReqT, RespT> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val rawToken = extractBearerToken(headers)

        if (rawToken == null) {
            call.close(
                Status.UNAUTHENTICATED.withDescription(
                    "Missing or malformed Authorization header"
                ),
                Metadata()
            )
            return object : ServerCall.Listener<ReqT>() {}
        }

        val context = Context.current()
            .withValue(GrpcContext.RAW_TOKEN_KEY, rawToken)

        logger.debug(
            "gRPC call: token extracted, method={}",
            call.methodDescriptor.fullMethodName
        )

        return Contexts.interceptCall(context, call, headers, next)
    }

    private fun extractBearerToken(headers: Metadata): String? {
        val authHeader = headers.get(AUTHORIZATION_METADATA_KEY)
        if (authHeader == null || !authHeader.regionMatches(
                0, BEARER_PREFIX, 0, BEARER_PREFIX.length,
                ignoreCase = true
            )
        ) {
            return null
        }
        val token = authHeader.substring(BEARER_PREFIX.length).trim()
        return token.ifEmpty { null }
    }

    private companion object {
        const val BEARER_PREFIX = "Bearer "
        val AUTHORIZATION_METADATA_KEY: Metadata.Key<String> =
            Metadata.Key.of(
                "authorization",
                Metadata.ASCII_STRING_MARSHALLER
            )
    }
}