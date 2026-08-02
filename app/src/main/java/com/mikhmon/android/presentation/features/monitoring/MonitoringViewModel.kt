package com.mikhmon.android.presentation.features.monitoring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.HotspotActiveUser
import com.mikhmon.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MonitoringUiState(
    val activeUsers: List<HotspotActiveUser> = emptyList(),
    val totalBytesIn: Long = 0,
    val totalBytesOut: Long = 0,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val autoRefresh: Boolean = true,
    val refreshInterval: Long = 5000, // 5 seconds
    val error: String? = null
)

@HiltViewModel
class MonitoringViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()
    
    private var refreshJob: Job? = null
    
    init {
        startAutoRefresh()
    }
    
    fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                loadActiveUsers()
                delay(_uiState.value.refreshInterval)
            }
        }
    }
    
    fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }
    
    fun toggleAutoRefresh() {
        if (_uiState.value.autoRefresh) {
            stopAutoRefresh()
            _uiState.update { it.copy(autoRefresh = false) }
        } else {
            startAutoRefresh()
            _uiState.update { it.copy(autoRefresh = true) }
        }
    }
    
    fun loadActiveUsers() {
        viewModelScope.launch {
            Logger.debug(Logger.Category.USER, "Loading active users for monitoring")
            
            val result = userRepository.getActiveUsers()
            
            if (result.isSuccess) {
                val users = result.getOrNull() ?: emptyList()
                val totalIn = users.sumOf { it.bytesIn }
                val totalOut = users.sumOf { it.bytesOut }
                
                _uiState.update { state ->
                    state.copy(
                        activeUsers = users,
                        totalBytesIn = totalIn,
                        totalBytesOut = totalOut,
                        isLoading = false,
                        isRefreshing = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    fun kickUser(userId: String) {
        viewModelScope.launch {
            Logger.info(Logger.Category.USER, "Kicking user: $userId")
            val result = userRepository.kickUser(userId)
            if (result.isSuccess) {
                loadActiveUsers()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        stopAutoRefresh()
    }
}
