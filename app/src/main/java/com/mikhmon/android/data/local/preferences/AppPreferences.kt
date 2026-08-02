package com.mikhmon.android.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mikhmon_preferences")

/**
 * Application preferences storage using DataStore
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_THEME = stringPreferencesKey("theme")
        private val KEY_DEFAULT_ROUTER_ID = stringPreferencesKey("default_router_id")
        private val KEY_LOG_LEVEL = stringPreferencesKey("log_level")
        private val KEY_REFRESH_INTERVAL = stringPreferencesKey("refresh_interval")
        private val KEY_CURRENCY = stringPreferencesKey("currency")
        private val KEY_TIMEZONE = stringPreferencesKey("timezone")
    }
    
    // Theme
    val themeFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_THEME] ?: "System Default" }
    
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME] = theme
        }
    }
    
    // Default Router
    val defaultRouterIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[KEY_DEFAULT_ROUTER_ID] }
    
    suspend fun setDefaultRouterId(routerId: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEFAULT_ROUTER_ID] = routerId
        }
    }
    
    // Log Level
    val logLevelFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_LOG_LEVEL] ?: "INFO" }
    
    suspend fun setLogLevel(level: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LOG_LEVEL] = level
        }
    }
    
    // Refresh Interval (in seconds)
    val refreshIntervalFlow: Flow<Int> = context.dataStore.data
        .map { preferences -> 
            preferences[KEY_REFRESH_INTERVAL]?.toIntOrNull() ?: 5 
        }
    
    suspend fun setRefreshInterval(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REFRESH_INTERVAL] = seconds.toString()
        }
    }
    
    // Currency
    val currencyFlow: Flow<String> = context.dataStore.data
        .map { preferences -> preferences[KEY_CURRENCY] ?: "IDR" }
    
    suspend fun setCurrency(currency: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CURRENCY] = currency
        }
    }
    
    // Clear all preferences
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
