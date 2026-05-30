package org.penakelex.obscura.grpc

import io.grpc.Context

object GrpcContext {
    val RAW_TOKEN_KEY: Context.Key<String> = Context.key("raw_token")
}