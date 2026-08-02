package com.mikhmon.android.data.repository

import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.Voucher
import com.mikhmon.android.data.model.VoucherBatch
import com.mikhmon.android.data.model.CharacterMode
import com.mikhmon.android.data.model.VoucherMode
import com.mikhmon.android.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for voucher management
 */
@Singleton
class VoucherRepository @Inject constructor(
    private val userRepository: UserRepository
) {
    private val random = SecureRandom()
    
    /**
     * Generate batch vouchers
     */
    suspend fun generateVouchers(
        profile: String,
        quantity: Int,
        prefix: String = "",
        characterMode: CharacterMode = CharacterMode.MIX,
        userLength: Int = 4,
        voucherMode: VoucherMode = VoucherMode.VC,
        timeLimit: String? = null,
        dataLimit: Long? = null,
        comment: String = "",
        price: Double = 0.0,
        sellingPrice: Double = 0.0
    ): Result<VoucherBatch> {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.VOUCHER, "Generating $quantity vouchers for profile: $profile", correlationId)
        
        if (quantity > 500) {
            return Result.failure(IllegalArgumentException("Maximum 500 vouchers per batch"))
        }
        
        val batchId = UUID.randomUUID().toString()
        val usedNames = mutableSetOf<String>()
        var successCount = 0
        
        repeat(quantity) {
            val username = generateUsername(prefix, userLength, characterMode)
            val password = if (voucherMode == VoucherMode.VC) {
                username
            } else {
                generatePassword(userLength + 2, characterMode)
            }
            
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
                successCount++
            } else {
                Logger.error(
                    Logger.Category.VOUCHER,
                    "Failed to create voucher: ${addResult.exceptionOrNull()?.message}",
                    null,
                    correlationId
                )
            }
        }
        
        val batch = VoucherBatch(
            id = batchId,
            profileName = profile,
            quantity = successCount,
            prefix = prefix,
            characterMode = characterMode,
            userLength = userLength,
            voucherMode = voucherMode,
            timeLimit = timeLimit,
            dataLimit = dataLimit?.toString(),
            totalPrice = price * successCount,
            totalSellingPrice = sellingPrice * successCount
        )
        
        Logger.info(Logger.Category.VOUCHER, "Generated $successCount/$quantity vouchers", correlationId)
        return Result.success(batch)
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
}
