package com.mikhmon.android.core.util

/**
 * Utility functions for byte formatting
 */
object ByteUtils {
    
    /**
     * Format bytes to human readable string
     * Input: 1536 -> Output: "1.50 KB"
     */
    fun formatBytes(bytes: Long, decimals: Int = 2): String {
        return when {
            bytes >= 1024L * 1024 * 1024 * 1024 -> {
                String.format("%.${decimals}f TB", bytes / (1024.0 * 1024 * 1024 * 1024))
            }
            bytes >= 1024L * 1024 * 1024 -> {
                String.format("%.${decimals}f GB", bytes / (1024.0 * 1024 * 1024))
            }
            bytes >= 1024L * 1024 -> {
                String.format("%.${decimals}f MB", bytes / (1024.0 * 1024))
            }
            bytes >= 1024L -> {
                String.format("%.${decimals}f KB", bytes / 1024.0)
            }
            else -> "$bytes B"
        }
    }
    
    /**
     * Format bytes to short string
     * Input: 1536 -> Output: "1.5K"
     */
    fun formatBytesShort(bytes: Long): String {
        return when {
            bytes >= 1024L * 1024 * 1024 * 1024 -> {
                String.format("%.1fT", bytes / (1024.0 * 1024 * 1024 * 1024))
            }
            bytes >= 1024L * 1024 * 1024 -> {
                String.format("%.1fG", bytes / (1024.0 * 1024 * 1024))
            }
            bytes >= 1024L * 1024 -> {
                String.format("%.1fM", bytes / (1024.0 * 1024))
            }
            bytes >= 1024L -> {
                String.format("%.1fK", bytes / 1024.0)
            }
            else -> "${bytes}B"
        }
    }
    
    /**
     * Parse data limit string to bytes
     * Input: "1G", "500M", "1024K" -> Output: bytes
     */
    fun parseDataLimit(limit: String): Long {
        val regex = Regex("(\\d+)([KMG]?)", RegexOption.IGNORE_CASE)
        val match = regex.find(limit.trim()) ?: return 0
        
        val value = match.groupValues[1].toLongOrNull() ?: 0
        val unit = match.groupValues[2].uppercase()
        
        return when (unit) {
            "K" -> value * 1024
            "M" -> value * 1024 * 1024
            "G" -> value * 1024 * 1024 * 1024
            else -> value
        }
    }
    
    /**
     * Parse MikroTik rate limit string
     * Input: "1M/2M" -> Output: Pair(upload, download) in bits
     */
    fun parseRateLimit(rateLimit: String): Pair<Long, Long>? {
        val parts = rateLimit.split("/")
        if (parts.size != 2) return null
        
        val upload = parseSpeedToBits(parts[0])
        val download = parseSpeedToBits(parts[1])
        
        return Pair(upload, download)
    }
    
    private fun parseSpeedToBits(speed: String): Long {
        val regex = Regex("(\\d+)([KMG]?)", RegexOption.IGNORE_CASE)
        val match = regex.find(speed.trim()) ?: return 0
        
        val value = match.groupValues[1].toLongOrNull() ?: 0
        val unit = match.groupValues[2].uppercase()
        
        return when (unit) {
            "K" -> value * 1000
            "M" -> value * 1000 * 1000
            "G" -> value * 1000 * 1000 * 1000
            else -> value
        }
    }
}
