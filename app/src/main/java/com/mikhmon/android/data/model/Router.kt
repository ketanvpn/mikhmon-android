package com.mikhmon.android.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Router data model
 * 
 * Represents a MikroTik router configuration
 */
@Entity(tableName = "routers")
@Serializable
data class Router(
    @PrimaryKey
    @SerialName("id")
    val id: String,
    
    @SerialName("name")
    val name: String,
    
    @SerialName("host")
    val host: String,
    
    @SerialName("port")
    val port: Int = 8728,
    
    @SerialName("username")
    val username: String,
    
    @SerialName("password")
    val password: String,
    
    @SerialName("use_ssl")
    val useSsl: Boolean = false,
    
    @SerialName("is_default")
    val isDefault: Boolean = false,
    
    @SerialName("is_connected")
    val isConnected: Boolean = false,
    
    @SerialName("last_connected")
    val lastConnected: Long? = null,
    
    @SerialName("created_at")
    val createdAt: Long,
    
    @SerialName("updated_at")
    val updatedAt: Long
) {
    companion object {
        fun create(
            id: String,
            name: String,
            host: String,
            port: Int = 8728,
            username: String,
            password: String,
            useSsl: Boolean = false,
            isDefault: Boolean = false
        ): Router {
            val now = System.currentTimeMillis()
            return Router(
                id = id,
                name = name,
                host = host,
                port = port,
                username = username,
                password = password,
                useSsl = useSsl,
                isDefault = isDefault,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

/**
 * Router status information
 */
data class RouterStatus(
    val routerId: String,
    val isConnected: Boolean,
    val uptime: String? = null,
    val cpuLoad: String? = null,
    val freeMemory: String? = null,
    val totalMemory: String? = null,
    val freeHdd: String? = null,
    val totalHdd: String? = null,
    val routerOsVersion: String? = null,
    val boardName: String? = null,
    val model: String? = null,
    val lastChecked: Long = System.currentTimeMillis()
)

/**
 * Router system resource
 */
data class SystemResource(
    val uptime: String,
    val version: String,
    val buildTime: String,
    val freeMemory: Long,
    val totalMemory: Long,
    val cpuLoad: Int,
    val freeHddSpace: Long,
    val totalHddSpace: Long,
    val boardName: String?,
    val model: String?,
    val architectureName: String
)
