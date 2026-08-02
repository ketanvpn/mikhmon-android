package com.mikhmon.android.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Hotspot User data model
 * 
 * Represents a hotspot user in MikroTik
 */
@Serializable
data class HotspotUser(
    @SerialName("id")
    val id: String, // .id from MikroTik
    
    @SerialName("name")
    val name: String, // username
    
    @SerialName("password")
    val password: String = "",
    
    @SerialName("profile")
    val profile: String,
    
    @SerialName("server")
    val server: String? = null,
    
    @SerialName("uptime_limit")
    val uptimeLimit: String? = null, // time limit
    
    @SerialName("bytes_in")
    val bytesIn: Long = 0, // data limit in
    
    @SerialName("bytes_out")
    val bytesOut: Long = 0, // data limit out
    
    @SerialName("bytes_total_limit")
    val bytesTotalLimit: Long? = null, // total data limit
    
    @SerialName("disabled")
    val isDisabled: Boolean = false,
    
    @SerialName("comment")
    val comment: String = "",
    
    @SerialName("email")
    val email: String? = null,
    
    @SerialName("mac_address")
    val macAddress: String? = null,
    
    @SerialName("mac_address_auto")
    val macAddressAuto: Boolean = false, // if MAC was auto-locked
    
    @SerialName("dynamic")
    val isDynamic: Boolean = false,
    
    @SerialName("expired_date")
    val expiredDate: String? = null, // parsed from comment
    
    @SerialName("created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @SerialName("updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Check if user is expired
     */
    fun isExpired(): Boolean {
        expiredDate?.let { date ->
            // Parse date format: mmm/dd/yyyy hh:mm:ss
            return try {
                // Simple check - compare with current time
                // TODO: Proper date parsing
                false
            } catch (e: Exception) {
                false
            }
        }
        return false
    }
    
    /**
     * Get formatted data usage
     */
    fun getFormattedDataUsage(): String {
        val totalBytes = bytesIn + bytesOut
        return formatBytes(totalBytes)
    }
    
    /**
     * Get formatted data limit
     */
    fun getFormattedDataLimit(): String? {
        return bytesTotalLimit?.let { formatBytes(it) }
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024))
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

/**
 * Hotspot Active User (currently connected)
 */
@Serializable
data class HotspotActiveUser(
    @SerialName("id")
    val id: String,
    
    @SerialName("server")
    val server: String,
    
    @SerialName("user")
    val user: String,
    
    @SerialName("address")
    val address: String, // IP address
    
    @SerialName("mac_address")
    val macAddress: String,
    
    @SerialName("login_by")
    val loginBy: String, // http-chap, http-pap, cookie, etc.
    
    @SerialName("uptime")
    val uptime: String,
    
    @SerialName("bytes_in")
    val bytesIn: Long,
    
    @SerialName("bytes_out")
    val bytesOut: Long,
    
    @SerialName("bytes_total")
    val bytesTotal: Long,
    
    @SerialName("packets_in")
    val packetsIn: Long,
    
    @SerialName("packets_out")
    val packetsOut: Long,
    
    @SerialName("session_time_left")
    val sessionTimeLeft: String? = null,
    
    @SerialName("idle_time")
    val idleTime: String? = null,
    
    @SerialName("radius")
    val isRadius: Boolean = false,
    
    @SerialName("comment")
    val comment: String? = null
) {
    fun getFormattedUptime(): String {
        return uptime
    }
    
    fun getFormattedBytesIn(): String = formatBytes(bytesIn)
    
    fun getFormattedBytesOut(): String = formatBytes(bytesOut)
    
    fun getFormattedBytesTotal(): String = formatBytes(bytesTotal)
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> "%.2f MB".format(bytes / (1024.0 * 1024))
            bytes >= 1024 -> "%.2f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}

/**
 * User status enum
 */
enum class UserStatus {
    ONLINE,      // Currently active
    OFFLINE,     // Not connected
    EXPIRED,     // Past validity period
    DISABLED,    // Manually disabled
    LIMITED      // Reached data/time limit
}
