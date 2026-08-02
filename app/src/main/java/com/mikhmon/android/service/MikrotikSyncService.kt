package com.mikhmon.android.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mikhmon.android.MikhmonApp
import com.mikhmon.android.R
import com.mikhmon.android.MainActivity
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.repository.RouterRepository
import com.mikhmon.android.data.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Background service for syncing with MikroTik router
 * 
 * Handles:
 * - Periodic data sync
 * - Active user monitoring
 * - Notification alerts
 */
@AndroidEntryPoint
class MikrotikSyncService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @Inject
    lateinit var routerRepository: RouterRepository
    
    @Inject
    lateinit var userRepository: UserRepository
    
    private var syncJob: Job? = null
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    private var lastActiveUserCount = 0
    
    inner class LocalBinder : Binder() {
        fun getService(): MikrotikSyncService = this@MikrotikSyncService
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onCreate() {
        super.onCreate()
        Logger.info(Logger.Category.SYSTEM, "Sync service created")
        startForeground(NOTIFICATION_ID, createNotification())
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SYNC -> startSync()
            ACTION_STOP_SYNC -> stopSync()
        }
        return START_STICKY
    }
    
    private fun startSync() {
        if (syncJob?.isActive == true) return
        
        Logger.info(Logger.Category.SYNC, "Starting sync job")
        _syncState.value = SyncState.Syncing
        
        syncJob = serviceScope.launch {
            while (isActive) {
                try {
                    performSync()
                    delay(SYNC_INTERVAL)
                } catch (e: CancellationException) {
                    break
                } catch (e: Exception) {
                    Logger.error(Logger.Category.SYNC, "Sync error: ${e.message}", e)
                    _syncState.value = SyncState.Error(e.message ?: "Unknown error")
                    delay(RETRY_DELAY)
                }
            }
        }
    }
    
    private suspend fun performSync() {
        Logger.debug(Logger.Category.SYNC, "Performing sync")
        
        // Get active users
        val result = userRepository.getActiveUsers()
        
        if (result.isSuccess) {
            val users = result.getOrNull() ?: emptyList()
            val currentCount = users.size
            
            // Check if user count changed
            if (currentCount != lastActiveUserCount) {
                notifyUserCountChange(lastActiveUserCount, currentCount)
                lastActiveUserCount = currentCount
            }
            
            _syncState.value = SyncState.Synced(
                lastSyncTime = System.currentTimeMillis(),
                activeUsers = currentCount
            )
        }
    }
    
    private fun stopSync() {
        Logger.info(Logger.Category.SYNC, "Stopping sync job")
        syncJob?.cancel()
        syncJob = null
        _syncState.value = SyncState.Idle
    }
    
    private fun notifyUserCountChange(oldCount: Int, newCount: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val notification = NotificationCompat.Builder(this, MikhmonApp.CHANNEL_MONITORING)
            .setContentTitle("Hotspot Activity")
            .setContentText("Active users: $newCount")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_USER_COUNT_ID, notification)
    }
    
    private fun createNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, MikhmonApp.CHANNEL_SYSTEM)
            .setContentTitle("Mikhmon")
            .setContentText("Monitoring hotspot activity")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopSync()
        serviceScope.cancel()
        Logger.info(Logger.Category.SYSTEM, "Sync service destroyed")
    }
    
    companion object {
        const val ACTION_START_SYNC = "com.mikhmon.android.action.START_SYNC"
        const val ACTION_STOP_SYNC = "com.mikhmon.android.action.STOP_SYNC"
        
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_USER_COUNT_ID = 1002
        private const val SYNC_INTERVAL = 30_000L // 30 seconds
        private const val RETRY_DELAY = 60_000L // 1 minute
    }
}

/**
 * Sync state sealed class
 */
sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    data class Synced(
        val lastSyncTime: Long,
        val activeUsers: Int
    ) : SyncState()
    data class Error(val message: String) : SyncState()
}
