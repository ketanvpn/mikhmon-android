package com.mikhmon.android.core.logging

/**
 * Log levels for the application
 */
enum class LogLevel(val priority: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    CRITICAL(5);

    companion object {
        fun fromInt(value: Int): LogLevel {
            return entries.find { it.priority == value } ?: INFO
        }
    }
}
