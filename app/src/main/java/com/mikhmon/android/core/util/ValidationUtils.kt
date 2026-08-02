package com.mikhmon.android.core.util

/**
 * Validation utility functions
 */
object ValidationUtils {
    
    /**
     * Validate IP address format
     */
    fun isValidIpAddress(ip: String): Boolean {
        val regex = Regex(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}" +
            "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        )
        return regex.matches(ip)
    }
    
    /**
     * Validate hostname or IP
     */
    fun isValidHost(host: String): Boolean {
        if (host.isBlank()) return false
        
        // Check if it's a valid IP
        if (isValidIpAddress(host)) return true
        
        // Check if it's a valid hostname
        val hostnameRegex = Regex(
            "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+" +
            "[a-zA-Z]{2,}$"
        )
        return hostnameRegex.matches(host)
    }
    
    /**
     * Validate port number
     */
    fun isValidPort(port: Int): Boolean {
        return port in 1..65535
    }
    
    /**
     * Validate username (MikroTik requirements)
     */
    fun isValidUsername(username: String): Boolean {
        if (username.isBlank()) return false
        if (username.length > 64) return false
        
        // MikroTik allows alphanumeric, dash, underscore, dot
        val regex = Regex("^[a-zA-Z0-9._-]+$")
        return regex.matches(username)
    }
    
    /**
     * Validate password
     */
    fun isValidPassword(password: String): Boolean {
        return password.isNotBlank()
    }
    
    /**
     * Validate rate limit format
     * Format: upload/download (e.g., "1M/2M", "512k/1M")
     */
    fun isValidRateLimit(rateLimit: String): Boolean {
        if (rateLimit.isBlank()) return true // Allow empty
        
        val regex = Regex("^[0-9]+[KMGkmg]?/[0-9]+[KMGkmg]?$")
        return regex.matches(rateLimit)
    }
    
    /**
     * Validate time limit format
     * Format: number + unit (h, d, w, M)
     */
    fun isValidTimeLimit(timeLimit: String): Boolean {
        if (timeLimit.isBlank()) return true // Allow empty
        
        val regex = Regex("^[0-9]+[hdwM]$")
        return regex.matches(timeLimit)
    }
    
    /**
     * Validate MAC address format
     */
    fun isValidMacAddress(mac: String): Boolean {
        val regex = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")
        return regex.matches(mac)
    }
}
