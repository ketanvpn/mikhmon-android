package com.mikhmon.android.domain.usecase.user

import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.HotspotActiveUser
import com.mikhmon.android.data.model.HotspotUser
import com.mikhmon.android.data.model.UserProfile
import com.mikhmon.android.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all hotspot users
 */
class GetUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Flow<Result<List<HotspotUser>>> {
        Logger.debug(Logger.Category.USER, "Getting all users")
        return userRepository.getAllUsers()
    }
}

/**
 * Use case for getting users by profile
 */
class GetUsersByProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(profile: String): Result<List<HotspotUser>> {
        Logger.debug(Logger.Category.USER, "Getting users by profile: $profile")
        return userRepository.getUsersByProfile(profile)
    }
}

/**
 * Use case for adding a new hotspot user
 */
class AddUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
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
        
        return userRepository.addUser(
            name = name,
            password = password,
            profile = profile,
            server = server,
            timeLimit = timeLimit,
            dataLimit = dataLimit,
            comment = comment
        )
    }
}

/**
 * Use case for updating a hotspot user
 */
class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        password: String? = null,
        profile: String? = null,
        timeLimit: String? = null,
        dataLimit: Long? = null,
        comment: String? = null
    ): Result<Unit> {
        Logger.debug(Logger.Category.USER, "Updating user: $userId")
        return userRepository.updateUser(userId, password, profile, timeLimit, dataLimit, comment)
    }
}

/**
 * Use case for deleting a hotspot user
 */
class DeleteUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        Logger.info(Logger.Category.USER, "Deleting user: $userId")
        return userRepository.deleteUser(userId)
    }
}

/**
 * Use case for enabling/disabling a user
 */
class SetUserEnabledUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, enabled: Boolean): Result<Unit> {
        Logger.debug(Logger.Category.USER, "Setting user $userId enabled: $enabled")
        return userRepository.setUserEnabled(userId, enabled)
    }
}

/**
 * Use case for getting active hotspot users
 */
class GetActiveUsersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<HotspotActiveUser>> {
        Logger.debug(Logger.Category.USER, "Getting active users")
        return userRepository.getActiveUsers()
    }
}

/**
 * Use case for kicking an active user
 */
class KickUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        Logger.info(Logger.Category.USER, "Kicking user: $userId")
        return userRepository.kickUser(userId)
    }
}

/**
 * Use case for getting user profiles
 */
class GetUserProfilesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<List<UserProfile>> {
        Logger.debug(Logger.Category.PROFILE, "Getting user profiles")
        return userRepository.getUserProfiles()
    }
}
