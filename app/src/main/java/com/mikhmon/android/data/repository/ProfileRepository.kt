package com.mikhmon.android.data.repository

import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.UserProfile
import com.mikhmon.android.data.model.ExpiredMode
import com.mikhmon.android.data.repository.RouterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for user profile management
 */
@Singleton
class ProfileRepository @Inject constructor(
    private val routerRepository: RouterRepository
) {
    /**
     * Get all user profiles
     */
    suspend fun getProfiles(): Result<List<UserProfile>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.PROFILE, "Fetching user profiles", correlationId)
        
        val api = routerRepository.getActiveApi()
            ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/user/profile/print")
        
        return if (result.isSuccess) {
            val profiles = result.getOrNull()?.map { parseProfile(it) } ?: emptyList()
            Logger.info(Logger.Category.PROFILE, "Fetched ${profiles.size} profiles", correlationId)
            Result.success(profiles)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to fetch profiles"))
        }
    }
    
    /**
     * Add a new user profile
     */
    suspend fun addProfile(
        name: String,
        rateLimit: String? = null,
        addressPool: String? = null,
        sharedUsers: Int = 1,
        validity: String? = null,
        price: Double = 0.0,
        sellingPrice: Double = 0.0,
        expiredMode: ExpiredMode = ExpiredMode.NONE,
        lockUser: Boolean = false,
        comment: String = ""
    ): Result<UserProfile> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.PROFILE, "Adding profile: $name", correlationId)
        
        val api = routerRepository.getActiveApi()
            ?: return Result.failure(Exception("No active router connection"))
        
        val params = mutableMapOf<String, String>(
            "name" to name,
            "shared-users" to sharedUsers.toString()
        )
        
        rateLimit?.let { params["rate-limit"] = it }
        addressPool?.let { params["address-pool"] = it }
        
        // Build on-login script for expiration handling
        if (expiredMode != ExpiredMode.NONE && validity != null) {
            val onLoginScript = buildOnLoginScript(expiredMode, validity, price, sellingPrice, comment)
            params["on-login"] = onLoginScript
        }
        
        if (comment.isNotEmpty()) {
            params["comment"] = comment
        }
        
        val result = api.execute("/ip/hotspot/user/profile/add", params)
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.PROFILE, "Profile added: $name", correlationId)
            
            // Fetch the created profile
            val profilesResult = getProfiles()
            val profile = profilesResult.getOrNull()?.find { it.name == name }
                ?: UserProfile(id = "", name = name)
            
            Result.success(profile)
        } else {
            Logger.error(
                Logger.Category.PROFILE,
                "Failed to add profile: ${result.exceptionOrNull()?.message}",
                null,
                correlationId
            )
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to add profile"))
        }
    }
    
    /**
     * Update user profile
     */
    suspend fun updateProfile(
        profileId: String,
        name: String? = null,
        rateLimit: String? = null,
        sharedUsers: Int? = null
    ): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.PROFILE, "Updating profile: $profileId", correlationId)
        
        val api = routerRepository.getActiveApi()
            ?: return Result.failure(Exception("No active router connection"))
        
        val params = mutableMapOf<String, String>(".id" to profileId)
        
        name?.let { params["name"] = it }
        rateLimit?.let { params["rate-limit"] = it }
        sharedUsers?.let { params["shared-users"] = it.toString() }
        
        val result = api.execute("/ip/hotspot/user/profile/set", params)
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.PROFILE, "Profile updated: $profileId", correlationId)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to update profile"))
        }
    }
    
    /**
     * Delete user profile
     */
    suspend fun deleteProfile(profileId: String): Result<Unit> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.PROFILE, "Deleting profile: $profileId", correlationId)
        
        val api = routerRepository.getActiveApi()
            ?: return Result.failure(Exception("No active router connection"))
        
        val result = api.execute("/ip/hotspot/user/profile/remove", mapOf(".id" to profileId))
        
        return if (result.isSuccess) {
            Logger.info(Logger.Category.PROFILE, "Profile deleted: $profileId", correlationId)
            Result.success(Unit)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to delete profile"))
        }
    }
    
    private fun buildOnLoginScript(
        expiredMode: ExpiredMode,
        validity: String,
        price: Double,
        sellingPrice: Double,
        comment: String
    ): String {
        // Build MikroTik script for expiration handling
        val mode = when (expiredMode) {
            ExpiredMode.REMOVE -> "remove"
            ExpiredMode.NOTICE -> "set limit-uptime=1s"
            ExpiredMode.REMOVE_RECORD -> "remove"
            ExpiredMode.NOTICE_RECORD -> "set limit-uptime=1s"
            else -> ""
        }
        
        return if (mode.isNotEmpty()) {
            ":local date [ /system clock get date ]; " +
            ":local comment \"$date - $validity\"; " +
            "/ip hotspot user set comment=\$comment [find where name=\$user]; " +
            ":delay ${validity}; " +
            "/ip hotspot user $mode [find where name=\$user]"
        } else {
            ""
        }
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
            onLogin = data["on-login"],
            onLogout = data["on-logout"],
            parentQueue = data["parent-queue"],
            comment = data["comment"] ?: ""
        )
    }
}
