package com.mikhmon.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Voucher data model
 * 
 * Represents a generated voucher/user
 */
@Serializable
data class Voucher(
    @SerialName("id")
    val id: String,
    
    @SerialName("username")
    val username: String,
    
    @SerialName("password")
    val password: String,
    
    @SerialName("profile")
    val profile: String,
    
    @SerialName("time_limit")
    val timeLimit: String? = null,
    
    @SerialName("data_limit")
    val dataLimit: String? = null,
    
    @SerialName("price")
    val price: Double,
    
    @SerialName("selling_price")
    val sellingPrice: Double,
    
    @SerialName("validity")
    val validity: String,
    
    @SerialName("comment")
    val comment: String = "",
    
    @SerialName("batch_id")
    val batchId: String, // ID of the batch generation
    
    @SerialName("status")
    val status: VoucherStatus = VoucherStatus.UNUSED,
    
    @SerialName("used_at")
    val usedAt: Long? = null,
    
    @SerialName("used_by_mac")
    val usedByMac: String? = null,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if voucher is printed
     */
    fun isPrinted(): Boolean = status == VoucherStatus.PRINTED || status == VoucherStatus.USED
    
    /**
     * Get formatted data limit
     */
    fun getFormattedDataLimit(): String? {
        return dataLimit?.let { limit ->
            val mb = limit.toLongOrNull() ?: 0
            when {
                mb >= 1024 -> "${mb / 1024} GB"
                else -> "$mb MB"
            }
        }
    }
}

/**
 * Voucher status enum
 */
@Serializable
enum class VoucherStatus {
    @SerialName("unused")
    UNUSED,
    
    @SerialName("printed")
    PRINTED,
    
    @SerialName("used")
    USED,
    
    @SerialName("expired")
    EXPIRED
}

/**
 * Voucher batch information
 */
@Serializable
data class VoucherBatch(
    @SerialName("id")
    val id: String,
    
    @SerialName("profile_name")
    val profileName: String,
    
    @SerialName("quantity")
    val quantity: Int,
    
    @SerialName("prefix")
    val prefix: String = "",
    
    @SerialName("character_mode")
    val characterMode: CharacterMode = CharacterMode.MIX,
    
    @SerialName("user_length")
    val userLength: Int = 4,
    
    @SerialName("voucher_mode")
    val voucherMode: VoucherMode = VoucherMode.VC,
    
    @SerialName("time_limit")
    val timeLimit: String? = null,
    
    @SerialName("data_limit")
    val dataLimit: String? = null,
    
    @SerialName("total_price")
    val totalPrice: Double,
    
    @SerialName("total_selling_price")
    val totalSellingPrice: Double,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Character mode for voucher generation
 */
@Serializable
enum class CharacterMode {
    @SerialName("lower")
    LOWER,      // lowercase letters only
    
    @SerialName("upper")
    UPPER,      // uppercase letters only
    
    @SerialName("upplow")
    UPPLOW,     // mixed case
    
    @SerialName("mix")
    MIX,        // lowercase + numbers
    
    @SerialName("mix1")
    MIX1,       // uppercase + numbers
    
    @SerialName("mix2")
    MIX2,       // mixed case + numbers
    
    @SerialName("num")
    NUM         // numbers only
}

/**
 * Voucher generation mode
 */
@Serializable
enum class VoucherMode {
    @SerialName("vc")
    VC,     // Username = Password
    
    @SerialName("up")
    UP      // Username != Password
}

/**
 * Voucher template for printing
 */
@Serializable
data class VoucherTemplate(
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("template_type")
    val templateType: TemplateType = TemplateType.DEFAULT,
    
    @SerialName("logo_path")
    val logoPath: String? = null,
    
    @SerialName("background_color")
    val backgroundColor: String = "#FFFFFF",
    
    @SerialName("text_color")
    val textColor: String = "#000000",
    
    @SerialName("show_qr")
    val showQr: Boolean = false,
    
    @SerialName("show_price")
    val showPrice: Boolean = true,
    
    @SerialName("show_validity")
    val showValidity: Boolean = true,
    
    @SerialName("custom_text")
    val customText: String? = null,
    
    @SerialName("font_size")
    val fontSize: Int = 12,
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Template type enum
 */
@Serializable
enum class TemplateType {
    @SerialName("default")
    DEFAULT,
    
    @SerialName("small")
    SMALL,
    
    @SerialName("qr")
    QR,
    
    @SerialName("custom")
    CUSTOM
}
