package com.mikhmon.android.domain.usecase.voucher

import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.*
import com.mikhmon.android.data.repository.UserRepository
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for generating vouchers
 */
class GenerateVouchersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    private val random = SecureRandom()
    
    suspend operator fun invoke(
        profile: String,
        quantity: Int,
        prefix: String = "",
        characterMode: CharacterMode = CharacterMode.MIX,
        userLength: Int = 4,
        voucherMode: VoucherMode = VoucherMode.VC,
        timeLimit: String? = null,
        dataLimit: Long? = null,
        comment: String = ""
    ): Result<List<Voucher>> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.VOUCHER, "Generating $quantity vouchers for profile: $profile", correlationId)
        
        if (quantity > 500) {
            return Result.failure(IllegalArgumentException("Maximum 500 vouchers per batch"))
        }
        
        val batchId = UUID.randomUUID().toString()
        val vouchers = mutableListOf<Voucher>()
        val usedNames = mutableSetOf<String>()
        
        // Generate vouchers
        repeat(quantity) { index ->
            val username = generateUsername(prefix, userLength, characterMode)
            val password = if (voucherMode == VoucherMode.VC) username else generatePassword(userLength + 2, characterMode)
            
            // Ensure unique username
            var finalUsername = username
            var counter = 1
            while (usedNames.contains(finalUsername)) {
                finalUsername = "$username$counter"
                counter++
            }
            usedNames.add(finalUsername)
            
            // Add user to MikroTik
            val addResult = userRepository.addUser(
                name = finalUsername,
                password = password,
                profile = profile,
                timeLimit = timeLimit,
                dataLimit = dataLimit,
                comment = comment.ifEmpty { "Batch: $batchId" }
            )
            
            if (addResult.isSuccess) {
                vouchers.add(
                    Voucher(
                        id = UUID.randomUUID().toString(),
                        username = finalUsername,
                        password = password,
                        profile = profile,
                        timeLimit = timeLimit,
                        dataLimit = dataLimit?.toString(),
                        price = 0.0,
                        sellingPrice = 0.0,
                        validity = timeLimit ?: "",
                        comment = comment,
                        batchId = batchId,
                        status = VoucherStatus.UNUSED
                    )
                )
            } else {
                Logger.error(Logger.Category.VOUCHER, "Failed to create voucher: ${addResult.exceptionOrNull()?.message}", null, correlationId)
            }
        }
        
        Logger.info(Logger.Category.VOUCHER, "Generated ${vouchers.size}/$quantity vouchers", correlationId)
        return Result.success(vouchers)
    }
    
    private fun generateUsername(prefix: String, length: Int, mode: CharacterMode): String {
        val suffix = generateRandomString(length, mode)
        return if (prefix.isNotEmpty()) "$prefix$suffix" else suffix
    }
    
    private fun generatePassword(length: Int, mode: CharacterMode): String {
        return generateRandomString(length, mode)
    }
    
    private fun generateRandomString(length: Int, mode: CharacterMode): String {
        val chars = when (mode) {
            CharacterMode.LOWER -> "abcdefghijklmnopqrstuvwxyz"
            CharacterMode.UPPER -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            CharacterMode.UPPLOW -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            CharacterMode.MIX -> "abcdefghijklmnopqrstuvwxyz0123456789"
            CharacterMode.MIX1 -> "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            CharacterMode.MIX2 -> "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            CharacterMode.NUM -> "0123456789"
        }
        
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
    
    private fun Long.formatDataLimit(): String {
        return when {
            this >= 1024 * 1024 * 1024 -> "${this / (1024 * 1024 * 1024)}G"
            this >= 1024 * 1024 -> "${this / (1024 * 1024)}M"
            this >= 1024 -> "${this / 1024}K"
            else -> "${this}B"
        }
    }
}

/**
 * Use case for deleting vouchers
 */
class DeleteVouchersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userIds: List<String>): Result<Int> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.VOUCHER, "Deleting ${userIds.size} vouchers", correlationId)
        
        var deletedCount = 0
        userIds.forEach { userId ->
            val result = userRepository.deleteUser(userId)
            if (result.isSuccess) {
                deletedCount++
            }
        }
        
        Logger.info(Logger.Category.VOUCHER, "Deleted $deletedCount/${userIds.size} vouchers", correlationId)
        return Result.success(deletedCount)
    }
}
