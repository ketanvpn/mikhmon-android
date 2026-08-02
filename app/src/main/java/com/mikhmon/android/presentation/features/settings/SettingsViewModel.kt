package com.mikhmon.android.presentation.features.settings

import androidx.lifecycle.ViewModel
import com.mikhmon.android.BuildConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val theme: String = "System Default",
    val appVersion: String = BuildConfig.VERSION_NAME,
    val autoRefresh: Boolean = true,
    val autoRefreshInterval: Int = 5,
    val logLevel: String = "INFO"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    // TODO: Inject PreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun setTheme(theme: String) {
        _uiState.value = _uiState.value.copy(theme = theme)
        // TODO: Save to DataStore
    }
    
    fun clearLogs() {
        // TODO: Implement log clearing
    }
}
