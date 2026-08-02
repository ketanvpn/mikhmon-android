package com.mikhmon.android.core.api

import com.mikhmon.android.core.logging.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.math.min

/**
 * MikroTik RouterOS API Client
 * 
 * Implements the RouterOS API protocol for communication with MikroTik routers.
 * Supports both legacy (pre-v6.43) and modern authentication methods.
 * 
 * Protocol Details:
 * - Port: 8728 (plain), 8729 (SSL)
 * - Encoding: Length-prefixed words
 * - Commands: Similar to RouterOS CLI commands
 */
class MikrotikApi private constructor(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val timeoutMs: Int = DEFAULT_TIMEOUT,
    private val useSsl: Boolean = false
) {
    private var socket: Socket? = null
    private var outputStream: DataOutputStream? = null
    private var inputStream: DataInputStream? = null
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private var isLegacyAuth = false
    
    companion object {
        const val DEFAULT_PORT = 8728
        const val DEFAULT_SSL_PORT = 8729
        const val DEFAULT_TIMEOUT = 10000 // 10 seconds
        
        /**
         * Create a new MikrotikApi instance
         */
        fun create(
            host: String,
            port: Int = DEFAULT_PORT,
            username: String,
            password: String,
            timeoutMs: Int = DEFAULT_TIMEOUT,
            useSsl: Boolean = false
        ): MikrotikApi {
            return MikrotikApi(host, port, username, password, timeoutMs, useSsl)
        }
    }
    
    /**
     * Connect to the MikroTik router
     */
    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        val correlationId = Logger.generateCorrelationId()
        Logger.debug(Logger.Category.API, "Connecting to $host:$port", correlationId)
        
        try {
            _connectionState.value = ConnectionState.Connecting
            
            // Create socket and connect
            socket = Socket().apply {
                soTimeout = timeoutMs
                connect(InetSocketAddress(host, port), timeoutMs)
            }
            
            outputStream = DataOutputStream(socket?.getOutputStream())
            inputStream = DataInputStream(socket?.getInputStream())
            
            Logger.debug(Logger.Category.API, "Socket connected", correlationId)
            
            // Perform login
            val loginResult = login(correlationId)
            
            if (loginResult.isSuccess) {
                _connectionState.value = ConnectionState.Connected
                Logger.info(Logger.Category.API, "Connected to $host:$port", correlationId)
                Result.success(Unit)
            } else {
                disconnect()
                Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Connection failed")
            Logger.error(Logger.Category.API, "Connection failed: ${e.message}", e, correlationId)
            Result.failure(e)
        }
    }
    
    /**
     * Perform authentication
     */
    private suspend fun login(correlationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Try modern authentication first (RouterOS v6.43+)
            Logger.debug(Logger.Category.API, "Attempting modern authentication", correlationId)
            
            writeCommand("/login")
            val response = readResponse()
            
            // Check if we got a challenge (legacy auth)
            val retResponse = response.find { it.containsKey("ret") }
            
            if (retResponse != null) {
                // Legacy authentication (pre-v6.43)
                isLegacyAuth = true
                Logger.debug(Logger.Category.API, "Using legacy authentication", correlationId)
                
                val challenge = retResponse["ret"] ?: return@withContext Result.failure(Exception("No challenge received"))
                val hashedPassword = hashPassword(password, challenge)
                
                writeCommand("/login", mapOf(
                    "name" to username,
                    "response" to "00${hashedPassword}"
                ))
            } else {
                // Modern authentication (v6.43+)
                isLegacyAuth = false
                Logger.debug(Logger.Category.API, "Using modern authentication", correlationId)
                
                writeCommand("/login", mapOf(
                    "name" to username,
                    "password" to password
                ))
            }
            
            val loginResponse = readResponse()
            val error = loginResponse.find { it.containsKey("!trap") }
            
            if (error != null) {
                Logger.error(Logger.Category.AUTH, "Login failed: ${error["message"]}", null, correlationId)
                Result.failure(AuthenticationException(error["message"] ?: "Authentication failed"))
            } else {
                Logger.info(Logger.Category.AUTH, "Login successful", correlationId)
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Logger.error(Logger.Category.AUTH, "Login error: ${e.message}", e, correlationId)
            Result.failure(e)
        }
    }
    
    /**
     * Hash password for legacy authentication
     */
    private fun hashPassword(password: String, challenge: String): String {
        val md = MessageDigest.getInstance("MD5")
        val challengeBytes = hexStringToByteArray(challenge)
        
        md.update(0.toByte())
        md.update(password.toByteArray())
        val hash = md.digest(challengeBytes)
        
        return bytesToHexString(hash)
    }
    
    /**
     * Execute a command and get the response
     */
    suspend fun execute(command: String, params: Map<String, String> = emptyMap()): Result<List<Map<String, String>>> {
        val correlationId = Logger.generateCorrelationId()
        
        if (_connectionState.value !is ConnectionState.Connected) {
            Logger.error(Logger.Category.API, "Not connected to router", null, correlationId)
            return Result.failure(NotConnectedException())
        }
        
        return withContext(Dispatchers.IO) {
            try {
                Logger.debug(Logger.Category.API, "Executing: $command with params: $params", correlationId)
                
                writeCommand(command, params)
                val response = readResponse()
                
                val errors = response.filter { it.containsKey("!trap") }
                if (errors.isNotEmpty()) {
                    val errorMsg = errors.firstOrNull()?.get("message") ?: "Command failed"
                    Logger.error(Logger.Category.API, "Command failed: $errorMsg", null, correlationId)
                    Result.failure(MikrotikApiException(errorMsg))
                } else {
                    Logger.debug(Logger.Category.API, "Command executed successfully, ${response.size} results", correlationId)
                    Result.success(response.filter { !it.containsKey("!done") && !it.containsKey("!re") })
                }
            } catch (e: Exception) {
                Logger.error(Logger.Category.API, "Command execution failed: ${e.message}", e, correlationId)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Execute a query command
     */
    suspend fun query(command: String, queryParams: Map<String, String> = emptyMap(), properties: List<String> = emptyList()): Result<List<Map<String, String>>> {
        val params = mutableMapOf<String, String>()
        
        // Add property list if specified
        if (properties.isNotEmpty()) {
            params[".proplist"] = properties.joinToString(",")
        }
        
        // Add query parameters
        queryParams.forEach { (key, value) ->
            params["?$key"] = value
        }
        
        return execute(command, params)
    }
    
    /**
     * Write a command to the router
     */
    private fun writeCommand(command: String, params: Map<String, String> = emptyMap()) {
        val output = outputStream ?: throw IOException("Not connected")
        
        // Write command
        writeWord(output, command)
        
        // Write parameters
        params.forEach { (key, value) ->
            writeWord(output, "=$key=$value")
        }
        
        // Write empty word to end command
        writeWord(output, "")
        
        output.flush()
    }
    
    /**
     * Read response from the router
     */
    private suspend fun readResponse(): List<Map<String, String>> = withContext(Dispatchers.IO) {
        val response = mutableListOf<Map<String, String>>()
        val currentData = mutableMapOf<String, String>()
        
        while (true) {
            val word = readWord(inputStream ?: throw IOException("Not connected"))
            
            when (word) {
                "!re" -> {
                    // Data record - save current and start new
                    if (currentData.isNotEmpty()) {
                        response.add(currentData.toMap())
                        currentData.clear()
                    }
                }
                "!done" -> {
                    // Command completed
                    if (currentData.isNotEmpty()) {
                        response.add(currentData.toMap())
                    }
                    break
                }
                "!trap", "!fatal" -> {
                    // Error
                    currentData["!trap"] = "true"
                }
                "" -> {
                    // Empty word - end of record
                    if (currentData.isNotEmpty()) {
                        response.add(currentData.toMap())
                        currentData.clear()
                    }
                }
                else -> {
                    // Parse parameter
                    when {
                        word.startsWith("=") -> {
                            val eqIndex = word.indexOf('=', 1)
                            if (eqIndex > 0) {
                                val key = word.substring(1, eqIndex)
                                val value = word.substring(eqIndex + 1)
                                currentData[key] = value
                            }
                        }
                        word.startsWith(".tag=") -> {
                            currentData[".tag"] = word.substring(5)
                        }
                        else -> {
                            // Unknown format, store as is
                            currentData[word] = ""
                        }
                    }
                }
            }
        }
        
        response
    }
    
    /**
     * Write a word with length encoding
     */
    private fun writeWord(output: DataOutputStream, word: String) {
        val bytes = word.toByteArray(Charsets.UTF_8)
        val length = bytes.size
        
        // Encode length
        when {
            length < 0x80 -> output.writeByte(length)
            length < 0x4000 -> {
                output.writeByte((length shr 8) or 0x80)
                output.writeByte(length and 0xFF)
            }
            length < 0x200000 -> {
                output.writeByte((length shr 16) or 0xC0)
                output.writeByte((length shr 8) and 0xFF)
                output.writeByte(length and 0xFF)
            }
            length < 0x10000000 -> {
                output.writeByte((length shr 24) or 0xE0)
                output.writeByte((length shr 16) and 0xFF)
                output.writeByte((length shr 8) and 0xFF)
                output.writeByte(length and 0xFF)
            }
            else -> {
                output.writeByte(0xF0)
                output.writeByte((length shr 24) and 0xFF)
                output.writeByte((length shr 16) and 0xFF)
                output.writeByte((length shr 8) and 0xFF)
                output.writeByte(length and 0xFF)
            }
        }
        
        // Write word bytes
        output.write(bytes)
    }
    
    /**
     * Read a word with length decoding
     */
    private fun readWord(input: DataInputStream): String {
        // Read length
        var length = input.readByte().toInt() and 0xFF
        
        length = when {
            length < 0x80 -> length
            length < 0xC0 -> ((length and 0x3F) shl 8) or (input.readByte().toInt() and 0xFF)
            length < 0xE0 -> ((length and 0x1F) shl 16) or ((input.readByte().toInt() and 0xFF) shl 8) or (input.readByte().toInt() and 0xFF)
            length < 0xF0 -> ((length and 0x0F) shl 24) or ((input.readByte().toInt() and 0xFF) shl 16) or ((input.readByte().toInt() and 0xFF) shl 8) or (input.readByte().toInt() and 0xFF)
            else -> {
                input.readByte() // Skip first byte
                ((input.readByte().toInt() and 0xFF) shl 24) or 
                ((input.readByte().toInt() and 0xFF) shl 16) or 
                ((input.readByte().toInt() and 0xFF) shl 8) or 
                (input.readByte().toInt() and 0xFF)
            }
        }
        
        // Read word bytes
        val bytes = ByteArray(length)
        input.readFully(bytes)
        
        return String(bytes, Charsets.UTF_8)
    }
    
    /**
     * Disconnect from the router
     */
    fun disconnect() {
        val correlationId = Logger.generateCorrelationId()
        Logger.info(Logger.Category.API, "Disconnecting from $host:$port", correlationId)
        
        try {
            inputStream?.close()
            outputStream?.close()
            socket?.close()
        } catch (e: Exception) {
            Logger.error(Logger.Category.API, "Error during disconnect: ${e.message}", e, correlationId)
        } finally {
            inputStream = null
            outputStream = null
            socket = null
            _connectionState.value = ConnectionState.Disconnected
            Logger.info(Logger.Category.API, "Disconnected", correlationId)
        }
    }
    
    /**
     * Check if connected
     */
    fun isConnected(): Boolean = _connectionState.value is ConnectionState.Connected
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        disconnect()
        scope.cancel()
    }
    
    // Utility functions
    
    private fun hexStringToByteArray(hex: String): ByteArray {
        return ByteArray(hex.length / 2) {
            hex.substring(it * 2, it * 2 + 2).toInt(16).toByte()
        }
    }
    
    private fun bytesToHexString(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

/**
 * Connection state sealed class
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Exceptions
 */
class AuthenticationException(message: String) : Exception(message)
class NotConnectedException : Exception("Not connected to router")
class MikrotikApiException(message: String) : Exception(message)
class IOException(message: String) : Exception(message)
