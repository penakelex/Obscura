package org.penakelex.obscura.grpc

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptors
import io.grpc.ForwardingClientCall
import io.grpc.ManagedChannel
import io.grpc.Metadata
import io.grpc.MethodDescriptor
import io.grpc.ServerCall
import io.grpc.ServerInterceptors
import io.grpc.ServerServiceDefinition
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.inprocess.InProcessChannelBuilder
import io.grpc.inprocess.InProcessServerBuilder
import io.grpc.stub.ClientCalls
import io.grpc.testing.GrpcCleanupRule
import org.junit.Rule
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GrpcAuthInterceptorTest {
    @get:Rule
    val grpcCleanup = GrpcCleanupRule()

    private val interceptor = GrpcAuthInterceptor()

    private val pingMethod: MethodDescriptor<String, String> =
        MethodDescriptor.newBuilder<String, String>()
            .setType(MethodDescriptor.MethodType.UNARY)
            .setFullMethodName(
                MethodDescriptor.generateFullMethodName(
                    "test.TestService", "Ping"
                )
            )
            .setRequestMarshaller(StringMarshaller)
            .setResponseMarshaller(StringMarshaller)
            .build()

    private val testService: ServerServiceDefinition =
        ServerServiceDefinition.builder("test.TestService")
            .addMethod(pingMethod) { call, _ ->
                call.request(1)
                object :
                    ServerCall.Listener<String>() {
                    override fun onHalfClose() {
                        val token =
                            GrpcContext.RAW_TOKEN_KEY.get()
                                ?: "<none>"
                        call.sendHeaders(Metadata())
                        call.sendMessage(token)
                        call.close(Status.OK, Metadata())
                    }
                }
            }
            .build()

    private fun startServer(): ManagedChannel {
        val serverName = InProcessServerBuilder.generateName()
        grpcCleanup.register(
            InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(
                    ServerInterceptors.intercept(
                        testService,
                        interceptor
                    )
                )
                .build()
                .start()
        )
        return grpcCleanup.register(
            InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build()
        )
    }

    private val callOptionsWithTimeout: CallOptions =
        CallOptions.DEFAULT.withDeadlineAfter(5, TimeUnit.SECONDS)

    @Test
    fun `missing authorization header returns UNAUTHENTICATED`() {
        val channel = startServer()
        val exception = assertFailsWith<StatusRuntimeException> {
            ClientCalls.blockingUnaryCall(
                channel, pingMethod, callOptionsWithTimeout, ""
            )
        }
        assertEquals(
            Status.Code.UNAUTHENTICATED,
            exception.status.code
        )
    }

    @Test
    fun `invalid bearer format returns UNAUTHENTICATED`() {
        val channel = startServer()
        val intercepted = ClientInterceptors.intercept(
            channel, bearerInterceptor("Basic some-token")
        )
        val exception = assertFailsWith<StatusRuntimeException> {
            ClientCalls.blockingUnaryCall(
                intercepted, pingMethod, callOptionsWithTimeout, ""
            )
        }
        assertEquals(
            Status.Code.UNAUTHENTICATED,
            exception.status.code
        )
    }

    @Test
    fun `empty token after bearer returns UNAUTHENTICATED`() {
        val channel = startServer()
        val intercepted = ClientInterceptors.intercept(
            channel, bearerInterceptor("Bearer   ")
        )
        val exception = assertFailsWith<StatusRuntimeException> {
            ClientCalls.blockingUnaryCall(
                intercepted, pingMethod, callOptionsWithTimeout, ""
            )
        }
        assertEquals(
            Status.Code.UNAUTHENTICATED,
            exception.status.code
        )
    }

    @Test
    fun `valid token is passed through context`() {
        val channel = startServer()
        val intercepted = ClientInterceptors.intercept(
            channel, bearerInterceptor("Bearer valid_token_hex")
        )
        val response = ClientCalls.blockingUnaryCall(
            intercepted, pingMethod, callOptionsWithTimeout, ""
        )
        assertEquals("valid_token_hex", response)
    }

    @Test
    fun `bearer prefix is case insensitive`() {
        val channel = startServer()
        val intercepted = ClientInterceptors.intercept(
            channel, bearerInterceptor("bearer lower-case-token")
        )
        val response = ClientCalls.blockingUnaryCall(
            intercepted, pingMethod, callOptionsWithTimeout, ""
        )
        assertEquals("lower-case-token", response)
    }

    private object StringMarshaller :
        MethodDescriptor.Marshaller<String> {
        override fun stream(value: String) =
            ByteArrayInputStream(value.toByteArray())

        override fun parse(stream: java.io.InputStream): String =
            stream.bufferedReader().readText()
    }

    private fun bearerInterceptor(headerValue: String): io.grpc.ClientInterceptor =
        object : io.grpc.ClientInterceptor {
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
                        headers.put(
                            Metadata.Key.of(
                                "authorization",
                                Metadata.ASCII_STRING_MARSHALLER
                            ),
                            headerValue
                        )
                        super.start(responseListener, headers)
                    }
                }
            }
        }
}