package com.mikhmon.android.data.repository

import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.HotspotActiveUser
import com.mikhmon.android.data.model.HotspotUser
import com.mikhmon.android.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing hotspot users
 * 
 * Handles all user-related operations:
 * - CRUD operations for hotspot users
 * - User status monitoring
 * - Active user management
 */
@Singleton
class UserRepository @Inject constructor(
    private val routerRepository: RouterRepository
) {
    /**
     * Get all hotspot users
     */
    fun getAllUsers(): Flow<Result<List<HotspotUser>>> = flow {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.USER, "Fetching all hotspot users", correlationId)
        
        val api = routerRepository.getActiveApi()
        if (api == null) {
            Logger.error(Logger.Category.USER, "No active router connection", null, correlationId)
            emit(Result.failure(Exception("No active router connection")))
            return@flow
        }
        
        val result = api.execute("/ip/hotspot/user/print")
        
        if (result.isSuccess) {
            val users = result.getOrNull()?.map { parseUser(it) } ?: emptyList()
            Logger.info(Logger.Category.USER, "Fetched ${users.size} users", correlationId)
            emit(Result.success(users))
        } else {
            Logger.error(Logger.Category.USER, "Failed to fetch users: ${result.exceptionOrNull()?.message}", null, correlationId)
            emit(Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch users")))
        }
    }
    
    /**
     * Get users by profile
     */
    suspend fun getUsersByProfile(profile: String): Result<List<HotspotUser>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.USER, "Fetching users by profile: $profile", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/user/print", mapOf("?profile" to profile))
        
        return if (result.isSuccess) {
            val users = result.getOrNull()?.map { parseUser(it) } ?: emptyList()
            Logger.info(Logger.Category.USER, "Fetched ${users.size} users for profile: $profile", correlationId)
            Result.success(users)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch users"))
        }
    }
    
    /**
     * Get user by name
     */
    suspend fun getUserByName(name: String): Result<HotspotUser> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.USER, "Fetching user: $name", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/user/print", mapOf("?name" to name))
        
        return if (result.isSuccess) {
            val userData = result.getOrNull()?.firstOrNull()
            if (userData != null) {
                Result.success(parseUser(userData))
            } else {
                Result.failure(Exception("User not found"))
            }
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch user"))
        }
    }
    
    /**
     * Add a new hotspot user
     */
    suspend fun addUser(
        name: String,
        password: String,
        profile: String,
        server: String? = null,
        timeLimit: String? = null,
        dataLimit: Long? = null,
        comment: String = ""
    ): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.USER, "Adding user: $name", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val params = mutableMapOf<String, String>(
            "name" to name,
            "password" to password,
            "profile" to profile
        )
        
        server?.let { params["server"] = it }
        timeLimit?.let { params["limit-uptime"] = it }
        dataLimit?.let { params["limit-bytes-total"] = it.toString() }
        if (comment.isNotEmpty()) params["comment"] = comment
        
        val result = api.execute("/ip/hotspot/user/add", params)
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.USER, "User added: $name", correlationId)
            Result.success(Unit)
        } else {
            Logger.error(Logger.Category.USER, "Failed to add user: ${result.exceptionOrNull()?.message}", null, correlationId)
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to add user"))
        }
    }
    
    /**
     * Update hotspot user
     */
    suspend fun updateUser(
        userId: String,
        password: String? = null,
        profile: String? = null,
        timeLimit: String? = null,
        dataLimit: Long? = null,
        comment: String? = null
    ): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.USER, "Updating user: $userId", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val params = mutableMapOf<String, String>(".id" to userId)
        
        password?.let { params["password"] = it }
        profile?.let { params["profile"] = it }
        timeLimit?.let { params["limit-uptime"] = it }
        dataLimit?.let { params["limit-bytes-total"] = it.toString() }
        comment?.let { params["comment"] = it }
        
        val result = api.execute("/ip/hotspot/user/set", params)
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.USER, "User updated: $userId", correlationId)
            Result.success(Unit)
        } else {
            Logger.error(Logger.Category.USER, "Failed to update user: ${result.exceptionOrNull()?.message}", null, correlationId)
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to update user"))
        }
    }
    
    /**
     * Delete hotspot user
     */
    suspend fun deleteUser(userId: String): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.USER, "Deleting user: $userId", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/user/remove", mapOf(".id" to userId))
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.USER, "User deleted: $userId", correlationId)
            Result.success(Unit)
        } else {
            Logger.error(Logger.Category.USER, "Failed to delete user: ${result.exceptionOrNull()?.message}", null, correlationId)
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to delete user"))
        }
    }
    
    /**
     * Enable/disable user
     */
    suspend fun setUserEnabled(userId: String, enabled: Boolean): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.USER, "${if (enabled) "Enabling" else "Disabling"} user: $userId", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val command = if (enabled) "/ip/hotspot/user/enable" else "/ip/hotspot/user/disable"
        val result = api.execute(command, mapOf(".id" to userId))
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.USER, "User ${if (enabled) "enabled" else "disabled"}: $userId", correlationId)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to change user status"))
        }
    }
    
    /**
     * Get all active hotspot users
     */
    suspend fun getActiveUsers(): Result<List<HotspotActiveUser>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.USER, "Fetching active users", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/active/print")
        
        return if (result.isSuccess) {
            val users = result.getOrNull()?.map { parseActiveUser(it) } ?: emptyList()
            Logger.info(Logger.Category.USER, "Fetched ${users.size} active users", correlationId)
            Result.success(users)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch active users"))
        }
    }
    
    /**
     * Kick active user
     */
    suspend fun kickUser(userId: String): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.USER, "Kicking user: $userId", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/active/remove", mapOf(".id" to userId))
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.USER, "User kicked: $userId", correlationId)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to kick user"))
        }
    }
    
    /**
     * Get user profiles
     */
    suspend fun getUserProfiles(): Result<List<UserProfile>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.PROFILE, "Fetching user profiles", correlationId)
        
        val api = routerRepository.getActiveApi() ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/user/profile/print")
        
        return if (result.isSuccess) {
            val profiles = result.getOrNull()?.map { parseProfile(it) } ?: emptyList()
            Logger.info(Logger.Category.PROFILE, "Fetched ${profiles.size} profiles", correlationId)
            Result.success(profiles)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch profiles"))
        }
    }
    
    // Parsing functions
    
    private fun parseUser(data: Map<String, String>): HotspotUser {
        return HotspotUser(
            id = data[".id"] ?: "",
            name = data["name"] ?: "",
            password = data["password"] ?: "",
            profile = data["profile"] ?: "default",
            server = data["server"],
            uptimeLimit = data["limit-uptime"],
            bytesIn = data["bytes-in"]?.toLongOrNull() ?: 0,
            bytesOut = data["bytes-out"]?.toLongOrNull() ?: 0,
            bytesTotalLimit = data["limit-bytes-total"]?.toLongOrNull(),
            isDisabled = data["disabled"] == "true",
            comment = data["comment"] ?: "",
            macAddress = data["mac-address"],
            macAddressAuto = data["mac-address-auto"] == "true",
            isDynamic = data["dynamic"] == "true"
        )
    }
    
    private fun parseActiveUser(data: Map<String, String>): HotspotActiveUser {
        return HotspotActiveUser(
            id = data[".id"] ?: "",
            server = data["server"] ?: "",
            user = data["user"] ?: "",
            address = data["address"] ?: "",
            macAddress = data["mac-address"] ?: "",
            loginBy = data["login-by"] ?: "",
            uptime = data["uptime"] ?: "",
            bytesIn = data["bytes-in"]?.toLongOrNull() ?: 0,
            bytesOut = data["bytes-out"]?.toLongOrNull() ?: 0,
            bytesTotal = data["bytes-total"]?.toLongOrNull() ?: 0,
            packetsIn = data["packets-in"]?.toLongOrNull() ?: 0,
            packetsOut = data["packets-out"]?.toLongOrNull() ?: 0,
            sessionTimeLeft = data["session-time-left"],
            idleTime = data["idle-time"],
            isRadius = data["radius"] == "true",
            comment = data["comment"]
        )
    }
    
    private fun parseProfile(data: Map<String, String>): UserProfile {
        return UserProfile(
            id = data[".id"] ?: "",
            name = data["name"] ?: "",
            rateLimit = data["rate-limit"],
            addressPool = data["address-pool"],
            sharedUsers = data["shared-users"]?.toIntOrNull() ?: 1,
            sessionTimeout = data["session-timeout"],
            idleTimeout = data["idle-timeout"],
            keepaliveTimeout = data["keepalive-timeout"],
            onLogin = data["on-login"],
            onLogout = data["on-logout"],
            parentQueue = data["parent-queue"],
            comment = data["comment"] ?: ""
        )
    }
}
