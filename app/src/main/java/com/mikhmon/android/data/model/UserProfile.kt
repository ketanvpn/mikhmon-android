package com.mikhmon.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * User Profile data model
 * 
 * Represents a hotspot user profile in MikroTik
 * Defines bandwidth limits, validity, pricing, and expiration behavior
 */
@Serializable
data class UserProfile(
    @SerialName("id")
    val id: String, // .id from MikroTik
    
    @SerialName("name")
    val name: String, // profile name
    
    @SerialName("rate_limit")
    val rateLimit: String? = null, // bandwidth limit (e.g., "512k/1M")
    
    @SerialName("address_pool")
    val addressPool: String? = null, // IP pool for users
    
    @SerialName("shared_users")
    val sharedUsers: Int = 1, // number of simultaneous logins
    
    @SerialName("session_timeout")
    val sessionTimeout: String? = null,
    
    @SerialName("idle_timeout")
    val idleTimeout: String? = null,
    
    @SerialName("keepalive_timeout")
    val keepaliveTimeout: String? = null,
    
    @SerialName("status_autorefresh")
    val statusAutorefresh: String? = null,
    
    @SerialName("transparent_proxy")
    val transparentProxy: Boolean = false,
    
    @SerialName("open_status_page")
    val openStatusPage: String? = null,
    
    @SerialName("advertise")
    val advertise: Boolean = false,
    
    @SerialName("advertise_url")
    val advertiseUrl: String? = null,
    
    @SerialName("advertise_interval")
    val advertiseInterval: String? = null,
    
    @SerialName("advertise_timeout")
    val advertiseTimeout: String? = null,
    
    @SerialName("mac_cookie_timeout")
    val macCookieTimeout: String? = null,
    
    @SerialName("on_login")
    val onLogin: String? = null, // on-login script
    
    @SerialName("on_logout")
    val onLogout: String? = null, // on-logout script
    
    @SerialName("parent_queue")
    val parentQueue: String? = null,
    
    // Custom fields for Mikhmon
    @SerialName("validity")
    val validity: String? = null, // validity period (1h, 1d, 1w, 1M)
    
    @SerialName("grace_period")
    val gracePeriod: String? = null,
    
    @SerialName("price")
    val price: Double = 0.0, // cost price
    
    @SerialName("selling_price")
    val sellingPrice: Double = 0.0, // selling price
    
    @SerialName("expired_mode")
    val expiredMode: ExpiredMode = ExpiredMode.NONE,
    
    @SerialName("lock_user")
    val lockUser: Boolean = false, // MAC address locking
    
    @SerialName("lock_server")
    val lockServer: Boolean = false, // Server locking
    
    @SerialName("comment")
    val comment: String = "",
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Parse rate limit to get upload and download speeds
     */
    fun getSpeedLimits(): Pair<String, String>? {
        rateLimit?.let { limit ->
            val parts = limit.split("/")
            if (parts.size >= 2) {
                return Pair(parts[0], parts[1])
            }
        }
        return null
    }
    
    /**
     * Get formatted speed limit
     */
    fun getFormattedSpeed(): String {
        rateLimit?.let { limit ->
            val parts = limit.split("/")
            if (parts.size >= 2) {
                val up = parts[0]
                val down = parts[1]
                return "↓${formatSpeed(down)} / ↑${formatSpeed(up)}"
            }
        }
        return "Unlimited"
    }
    
    private fun formatSpeed(speed: String): String {
        // Remove any additional parameters after the speed value
        val speedValue = speed.split(" ")[0]
        return speedValue.uppercase()
    }
    
    /**
     * Get formatted validity period
     */
    fun getFormattedValidity(): String {
        validity?.let { v ->
            return when {
                v.contains("h") -> "${v.replace("h", " Jam")}"
                v.contains("d") -> "${v.replace("d", " Hari")}"
                v.contains("w") -> "${v.replace("w", " Minggu")}"
                v.contains("M") -> "${v.replace("M", " Bulan")}"
                else -> v
            }
        }
        return "Unlimited"
    }
}

/**
 * Expired mode enum
 */
@Serializable
enum class ExpiredMode {
    @SerialName("none")
    NONE,       // No expiration handling
    
    @SerialName("remove")
    REMOVE,     // Delete user on expire
    
    @SerialName("notice")
    NOTICE,     // Disable user on expire (limit-uptime=1s)
    
    @SerialName("remove_record")
    REMOVE_RECORD,   // Delete + record to report
    
    @SerialName("notice_record")
    NOTICE_RECORD    // Disable + record to report
}

/**
 * Profile statistics
 */
data class ProfileStats(
    val profileName: String,
    val totalUsers: Int,
    val activeUsers: Int,
    val expiredUsers: Int,
    val totalIncome: Double
)
