package org.penakelex.obscura.grpc

import io.grpc.Server
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import org.penakelex.obscura.config.ServerConfig
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class GrpcServerManager(
    private val networkConfig: ServerConfig.Network,
    private val syncService: NotesSyncService,
    private val authInterceptor: GrpcAuthInterceptor
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
            if (!it.awaitTermination(10, TimeUnit.SECONDS)) {
                logger.warn("gRPC server did not terminate in 10s, forcing shutdown")
                it.shutdownNow()
            }
            logger.info("gRPC server stopped gracefully")
        }
    }
}