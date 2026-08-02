package com.mikhmon.android.core.util

import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility functions for time/date handling
 */
object TimeUtils {
    
    /**
     * Format uptime string from MikroTik format
     * Input: "1d2h3m4s" -> Output: "1d 2h 3m 4s"
     */
    fun formatUptime(uptime: String): String {
        return uptime
            .replace("d", "d ")
            .replace("h", "h ")
            .replace("m", "m ")
            .replace("s", "s")
            .trim()
    }
    
    /**
     * Parse MikroTik uptime to seconds
     * Input: "1d2h3m4s" -> Output: total seconds
     */
    fun parseUptimeToSeconds(uptime: String): Long {
        var totalSeconds = 0L
        val regex = Regex("(\\d+)([dhms])")
        
        regex.findAll(uptime).forEach { match ->
            val value = match.groupValues[1].toLongOrNull() ?: 0
            val unit = match.groupValues[2]
            
            totalSeconds += when (unit) {
                "d" -> value * 86400
                "h" -> value * 3600
                "m" -> value * 60
                "s" -> value
                else -> 0
            }
        }
        
        return totalSeconds
    }
    
    /**
     * Format seconds to readable duration
     * Input: 90061 -> Output: "1d 1h 1m 1s"
     */
    fun formatSeconds(seconds: Long): String {
        val days = seconds / 86400
        val hours = (seconds % 86400) / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (secs > 0 || isEmpty()) append("${secs}s")
        }.trim()
    }
    
    /**
     * Get current timestamp in milliseconds
     */
    fun now(): Long = System.currentTimeMillis()
    
    /**
     * Format timestamp to human readable date
     */
    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * Parse validity string to duration description
     * Input: "1d", "1w", "1M" -> Output: "1 Day", "1 Week", "1 Month"
     */
    fun formatValidity(validity: String): String {
        val regex = Regex("(\\d+)([hdwM])")
        val match = regex.find(validity) ?: return validity
        
        val value = match.groupValues[1].toIntOrNull() ?: 1
        val unit = match.groupValues[2]
        
        val unitName = when (unit) {
            "h" -> if (value == 1) "Hour" else "Hours"
            "d" -> if (value == 1) "Day" else "Days"
            "w" -> if (value == 1) "Week" else "Weeks"
            "M" -> if (value == 1) "Month" else "Months"
            else -> ""
        }
        
        return "$value $unitName"
    }
}
