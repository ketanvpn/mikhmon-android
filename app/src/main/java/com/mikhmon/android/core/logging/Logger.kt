package com.mikhmon.android.core.logging

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Centralized logging system for Mikhmon Android
 * 
 * Features:
 * - Console logging with Timber
 * - File logging for debugging
 * - Log categories for filtering
 * - Correlation IDs for request tracking
 * - Structured log format
 */
object Logger {
    
    private var isInitialized = false
    private var enableConsoleLog = true
    private var enableFileLog = false
    private var minLogLevel = LogLevel.INFO
    private val logBuffer = ConcurrentLinkedQueue<LogEntry>()
    private const val MAX_BUFFER_SIZE = 1000
    
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    
    // Log Categories
    object Category {
        const val API = "API"
        const val AUTH = "AUTH"
        const val USER = "USER"
        const val VOUCHER = "VOUCHER"
        const val PROFILE = "PROFILE"
        const val ROUTER = "ROUTER"
        const val SYNC = "SYNC"
        const val UI = "UI"
        const val SYSTEM = "SYSTEM"
        const val DATABASE = "DATABASE"
        const val NETWORK = "NETWORK"
    }
    
    /**
     * Initialize the logger
     */
    fun initialize(
        enableConsoleLog: Boolean = true,
        enableFileLog: Boolean = false,
        minLogLevel: LogLevel = LogLevel.INFO
    ) {
        this.enableConsoleLog = enableConsoleLog
        this.enableFileLog = enableFileLog
        this.minLogLevel = minLogLevel
        this.isInitialized = true
        
        log(LogLevel.INFO, Category.SYSTEM, null, "Logger initialized - minLevel: $minLogLevel")
    }
    
    /**
     * Generate a new correlation ID for request tracking
     */
    fun generateCorrelationId(): String {
        return UUID.randomUUID().toString().take(8)
    }
    
    // Convenience methods for each log level
    
    fun verbose(category: String, message: String, correlationId: String? = null) {
        log(LogLevel.VERBOSE, category, correlationId, message)
    }
    
    fun debug(category: String, message: String, correlationId: String? = null) {
        log(LogLevel.DEBUG, category, correlationId, message)
    }
    
    fun info(category: String, message: String, correlationId: String? = null) {
        log(LogLevel.INFO, category, correlationId, message)
    }
    
    fun warning(category: String, message: String, correlationId: String? = null) {
        log(LogLevel.WARNING, category, correlationId, message)
    }
    
    fun error(category: String, message: String, throwable: Throwable? = null, correlationId: String? = null) {
        log(LogLevel.ERROR, category, correlationId, message, throwable)
    }
    
    fun critical(category: String, message: String, throwable: Throwable? = null, correlationId: String? = null) {
        log(LogLevel.CRITICAL, category, correlationId, message, throwable)
    }
    
    /**
     * Core logging method
     */
    private fun log(
        level: LogLevel,
        category: String,
        correlationId: String?,
        message: String,
        throwable: Throwable? = null
    ) {
        if (!isInitialized || level.priority < minLogLevel.priority) {
            return
        }
        
        val entry = LogEntry(
            level = level,
            category = category,
            correlationId = correlationId,
            message = message,
            throwable = throwable
        )
        
        // Add to buffer
        addToBuffer(entry)
        
        // Console logging via Timber
        if (enableConsoleLog) {
            logToConsole(entry)
        }
        
        // File logging (async)
        if (enableFileLog) {
            logToFile(entry)
        }
    }
    
    private fun addToBuffer(entry: LogEntry) {
        if (logBuffer.size >= MAX_BUFFER_SIZE) {
            logBuffer.poll()
        }
        logBuffer.offer(entry)
    }
    
    private fun logToConsole(entry: LogEntry) {
        val formattedMessage = entry.format()
        
        when (entry.level) {
            LogLevel.VERBOSE -> Timber.v(formattedMessage)
            LogLevel.DEBUG -> Timber.d(formattedMessage)
            LogLevel.INFO -> Timber.i(formattedMessage)
            LogLevel.WARNING -> Timber.w(formattedMessage)
            LogLevel.ERROR -> Timber.e(entry.throwable, formattedMessage)
            LogLevel.CRITICAL -> Timber.wtf(entry.throwable, formattedMessage)
        }
    }
    
    private fun logToFile(entry: LogEntry) {
        // TODO: Implement file logging with rotation
        // Will write to app-specific log file
    }
    
    /**
     * Get all logs from buffer
     */
    fun getLogs(): List<LogEntry> = logBuffer.toList()
    
    /**
     * Get logs filtered by category
     */
    fun getLogsByCategory(category: String): List<LogEntry> =
        logBuffer.filter { it.category == category }
    
    /**
     * Get logs filtered by correlation ID
     */
    fun getLogsByCorrelationId(correlationId: String): List<LogEntry> =
        logBuffer.filter { it.correlationId == correlationId }
    
    /**
     * Clear log buffer
     */
    fun clearLogs() {
        logBuffer.clear()
    }
    
    /**
     * Export logs as string
     */
    fun exportLogs(): String {
        return logBuffer.joinToString("\n") { it.format() }
    }
}
