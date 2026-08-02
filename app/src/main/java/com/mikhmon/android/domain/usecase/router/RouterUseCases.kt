package com.mikhmon.android.domain.usecase.router

import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.Router
import com.mikhmon.android.data.repository.RouterRepository
import javax.inject.Inject

/**
 * Use case for connecting to a MikroTik router
 */
class ConnectRouterUseCase @Inject constructor(
    private val routerRepository: RouterRepository
) {
    suspend operator fun invoke(routerId: String): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.ROUTER, "Connecting to router: $routerId", correlationId)
        
        return routerRepository.connect(routerId)
    }
}

/**
 * Use case for disconnecting from a router
 */
class DisconnectRouterUseCase @Inject constructor(
    private val routerRepository: RouterRepository
) {
    operator fun invoke(routerId: String) {
        Logger.info(Logger.Category.ROUTER, "Disconnecting from router: $routerId")
        routerRepository.disconnect(routerId)
    }
}

/**
 * Use case for getting router status
 */
class GetRouterStatusUseCase @Inject constructor(
    private val routerRepository: RouterRepository
) {
    suspend operator fun invoke(): Result<Map<String, String>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.ROUTER, "Getting router status", correlationId)
        
        return routerRepository.getSystemResource()
    }
}

/**
 * Use case for adding a new router
 */
class AddRouterUseCase @Inject constructor(
    private val routerRepository: RouterRepository
) {
    suspend operator fun invoke(
        name: String,
        host: String,
        port: Int = 8728,
        username: String,
        password: String,
        useSsl: Boolean = false,
        isDefault: Boolean = false
    ): Result<Router> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.ROUTER, "Adding router: $name", correlationId)
        
        return routerRepository.addRouter(
            name = name,
            host = host,
            port = port,
            username = username,
            password = password,
            useSsl = useSsl,
            isDefault = isDefault
        )
    }
}
