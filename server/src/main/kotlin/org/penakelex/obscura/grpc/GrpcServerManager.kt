package org.penakelex.obscura.grpc

import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import org.penakelex.obscura.config.ServerConfig
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class GrpcServerManager(
    private val syncService: NotesSyncService,
    private val authInterceptor: GrpcAuthInterceptor,
    private val networkConfig: ServerConfig.Network,
    private val grpcServerSettings: ServerConfig.ServerSettings.Grpc,
) {
    private val logger =
        LoggerFactory.getLogger(GrpcServerManager::class.java)
    private var server: Server? = null

    fun start() {
        server = NettyServerBuilder
            .forAddress(
                InetSocketAddress(
                    networkConfig.host,
                    networkConfig.grpcPort
                )
            )
            .addService(syncService.bindService())
            .intercept(authInterceptor)
            .build()
            .start()

        logger.info(
            "gRPC server started on {}:{}",
            networkConfig.host,
            networkConfig.grpcPort
        )
    }

    fun stop() {
        server?.let {
            it.shutdown()
            val timeout = grpcServerSettings.terminationTimeoutSeconds
            if (!it.awaitTermination(timeout.toLong(), TimeUnit.SECONDS)) {
                logger.warn(
                    "gRPC server did not terminate in {}s, forcing shutdown",
                    timeout
                )
                it.shutdownNow()
            }
            logger.info("gRPC server stopped gracefully")
        }
    }
}