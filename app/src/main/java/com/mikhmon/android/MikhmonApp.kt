package com.mikhmon.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.core.logging.LogLevel
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MikhmonApp : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Initialize logging
        initializeLogging()
        
        // Create notification channels
        createNotificationChannels()
        
        Logger.info("SYSTEM", "Application started - Mikhmon Android v${BuildConfig.VERSION_NAME}")
    }

    private fun initializeLogging() {
        // Plant Timber tree for logging
        if (BuildConfig.DEBUG_MODE) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "Mikhmon-${element.fileName}:${element.lineNumber}"
                }
            })
        }
        
        // Initialize custom Logger
        Logger.initialize(
            enableConsoleLog = BuildConfig.DEBUG_MODE,
            enableFileLog = true,
            minLogLevel = LogLevel.fromInt(BuildConfig.LOG_LEVEL)
        )
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_MONITORING,
                    "Monitoring Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Notifications for hotspot monitoring alerts"
                },
                NotificationChannel(
                    CHANNEL_SYSTEM,
                    "System Notifications",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "System and background service notifications"
                }
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(channels)
        }
    }

    companion object {
        const val CHANNEL_MONITORING = "monitoring_channel"
        const val CHANNEL_SYSTEM = "system_channel"
    }
}
