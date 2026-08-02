package com.mikhmon.android.data.repository

import com.mikhmon.android.core.api.ConnectionState
import com.mikhmon.android.core.api.MikrotikApi
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.HotspotActiveUser
import com.mikhmon.android.data.model.HotspotUser
import com.mikhmon.android.data.model.Router
import com.mikhmon.android.data.model.UserProfile
import com.mikhmon.android.data.local.database.RouterDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing MikroTik router connections and operations
 * 
 * Handles:
 * - Router CRUD operations
 * - API connection management
 * - Command execution
 * - Error handling with proper logging
 */
@Singleton
class RouterRepository @Inject constructor(
    private val routerDao: RouterDao
) {
    private val apiInstances = mutableMapOf<String, MikrotikApi>()
    
    private val _activeRouterId = MutableStateFlow<String?>(null)
    val activeRouterId: StateFlow<String?> = _activeRouterId.asStateFlow()
    
    private val _connectionStates = MutableStateFlow<Map<String, ConnectionState>>(emptyMap())
    val connectionStates: StateFlow<Map<String, ConnectionState>> = _connectionStates.asStateFlow()
    
    /**
     * Get all saved routers
     */
    fun getAllRouters(): Flow<List<Router>> = routerDao.getAllRouters()
    
    /**
     * Get router by ID
     */
    suspend fun getRouterById(id: String): Router? = routerDao.getRouterById(id)
    
    /**
     * Get default router
     */
    suspend fun getDefaultRouter(): Router? = routerDao.getDefaultRouter()
    
    /**
     * Add a new router
     */
    suspend fun addRouter(
        name: String,
        host: String,
        port: Int = MikrotikApi.DEFAULT_PORT,
        username: String,
        password: String,
        useSsl: Boolean = false,
        isDefault: Boolean = false
    ): Result<Router> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.ROUTER, "Adding router: $name ($host:$port)", correlationId)
        
        return try {
            val router = Router.create(
                id = UUID.randomUUID().toString(),
                name = name,
                host = host,
                port = port,
                username = username,
                password = password,
                useSsl = useSsl,
                isDefault = isDefault
            )
            
            routerDao.insertRouter(router)
            Logger.info(Logger.Category.ROUTER, "Router added successfully: ${router.id}", correlationId)
            
            Result.success(router)
        } catch (e: Exception) {
            Logger.error(Logger.Category.ROUTER, "Failed to add router: ${e.message}", e, correlationId)
            Result.failure(e)
        }
    }
    
    /**
     * Update router
     */
    suspend fun updateRouter(router: Router): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.ROUTER, "Updating router: ${router.id}", correlationId)
        
        return try {
            val updatedRouter = router.copy(updatedAt = System.currentTimeMillis())
            routerDao.updateRouter(updatedRouter)
            Logger.info(Logger.Category.ROUTER, "Router updated: ${router.id}", correlationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error(Logger.Category.ROUTER, "Failed to update router: ${e.message}", e, correlationId)
            Result.failure(e)
        }
    }
    
    /**
     * Delete router
     */
    suspend fun deleteRouter(routerId: String): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.ROUTER, "Deleting router: $routerId", correlationId)
        
        return try {
            // Disconnect if connected
            disconnect(routerId)
            
            // Delete from database
            routerDao.deleteRouterById(routerId)
            
            Logger.info(Logger.Category.ROUTER, "Router deleted: $routerId", correlationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.error(Logger.Category.ROUTER, "Failed to delete router: ${e.message}", e, correlationId)
            Result.failure(e)
        }
    }
    
    /**
     * Connect to a router
     */
    suspend fun connect(routerId: String): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.ROUTER, "Connecting to router: $routerId", correlationId)
        
        val router = getRouterById(routerId) ?: return Result.failure(Exception("Router not found"))
        
        return try {
            // Disconnect existing connection if any
            apiInstances[routerId]?.disconnect()
            
            // Create new API instance
            val api = MikrotikApi.create(
                host = router.host,
                port = router.port,
                username = router.username,
                password = router.password,
                useSsl = router.useSsl
            )
            
            // Connect
            val result = api.connect()
            
            if (result.isSuccess) {
                apiInstances[routerId] = api
                _activeRouterId.value = routerId
                updateConnectionState(routerId, ConnectionState.Connected)
                
                // Update router last connected
                updateRouter(router.copy(lastConnected = System.currentTimeMillis()))
                
                Logger.info(Logger.Category.ROUTER, "Connected to router: $routerId", correlationId)
                Result.success(Unit)
            } else {
                val error = result.exceptionOrNull() ?: Exception("Connection failed")
                updateConnectionState(routerId, ConnectionState.Error(error.message ?: "Connection failed"))
                Logger.error(Logger.Category.ROUTER, "Connection failed: ${error.message}", error, correlationId)
                Result.failure(error)
            }
        } catch (e: Exception) {
            updateConnectionState(routerId, ConnectionState.Error(e.message ?: "Connection failed"))
            Logger.error(Logger.Category.ROUTER, "Connection error: ${e.message}", e, correlationId)
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from a router
     */
    fun disconnect(routerId: String) {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.ROUTER, "Disconnecting from router: $routerId", correlationId)
        
        apiInstances[routerId]?.let { api ->
            api.disconnect()
            apiInstances.remove(routerId)
        }
        
        if (_activeRouterId.value == routerId) {
            _activeRouterId.value = null
        }
        
        updateConnectionState(routerId, ConnectionState.Disconnected)
    }
    
    /**
     * Get active API instance
     */
    fun getActiveApi(): MikrotikApi? {
        val routerId = _activeRouterId.value ?: return null
        return apiInstances[routerId]
    }
    
    /**
     * Get API instance for specific router
     */
    fun getApi(routerId: String): MikrotikApi? = apiInstances[routerId]
    
    /**
     * Check if router is connected
     */
    fun isConnected(routerId: String): Boolean {
        return apiInstances[routerId]?.isConnected() == true
    }
    
    /**
     * Execute command on active router
     */
    suspend fun executeCommand(command: String, params: Map<String, String> = emptyMap()): Result<List<Map<String, String>>> {
        val api = getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        return api.execute(command, params)
    }
    
    /**
     * Get system resource from router
     */
    suspend fun getSystemResource(): Result<Map<String, String>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.API, "Fetching system resource", correlationId)
        
        val result = executeCommand("/system/resource/print")
        
        return if (result.isSuccess) {
            val data = result.getOrNull()?.firstOrNull() ?: emptyMap()
            Logger.debug(Logger.Category.API, "System resource fetched: $data", correlationId)
            Result.success(data)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to get system resource"))
        }
    }
    
    private fun updateConnectionState(routerId: String, state: ConnectionState) {
        val current = _connectionStates.value.toMutableMap()
        current[routerId] = state
        _connectionStates.value = current
    }
    
    /**
     * Cleanup all connections
     */
    fun cleanup() {
        apiInstances.forEach { (_, api) ->
            api.cleanup()
        }
        apiInstances.clear()
        _activeRouterId.value = null
        _connectionStates.value = emptyMap()
    }
}
