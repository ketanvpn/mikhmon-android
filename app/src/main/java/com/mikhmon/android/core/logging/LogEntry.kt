package com.mikhmon.android.core.logging

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Represents a single log entry
 */
data class LogEntry(
    val id: Long = 0,
    val timestamp: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val level: LogLevel,
    val category: String,
    val correlationId: String? = null,
    val message: String,
    val throwable: Throwable? = null,
    val throwableMessage: String? = throwable?.message
) {
    fun format(): String {
        val correlation = correlationId?.let { "[$it] " } ?: ""
        val formattedTimestamp = "${timestamp.year}-${timestamp.monthNumber.toString().padStart(2, '0')}-${timestamp.dayOfMonth.toString().padStart(2, '0')} " +
                "${timestamp.hour.toString().padStart(2, '0')}:${timestamp.minute.toString().padStart(2, '0')}:${timestamp.second.toString().padStart(2, '0')}.${(timestamp.nanosecond / 1_000_000).toString().padStart(3, '0')}"
        
        return "[$formattedTimestamp] [${level.name}] [$category] ${correlation}$message${throwableMessage?.let { " | Exception: $it" } ?: ""}"
    }
}
