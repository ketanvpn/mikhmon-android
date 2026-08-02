package com.mikhmon.android.presentation.features.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.repository.RouterRepository
import com.mikhmon.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val routerName: String = "",
    val routerIp: String = "",
    val isConnected: Boolean = false,
    val uptime: String = "-",
    val cpuLoad: String = "-",
    val freeMemory: String = "-",
    val totalMemory: String = "-",
    val freeHdd: String = "-",
    val totalHdd: String = "-",
    val routerOsVersion: String = "-",
    val boardName: String = "-",
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val routerRepository: RouterRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    fun loadDashboardData() {
        viewModelScope.launch {
            Logger.debug(Logger.Category.UI, "Loading dashboard data")
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Get router info
            val activeRouterId = routerRepository.activeRouterId.value
            if (activeRouterId == null) {
                _uiState.update { it.copy(isLoading = false, error = "No active router connection") }
                return@launch
            }
            
            val router = routerRepository.getRouterById(activeRouterId)
            
            // Get system resource
            val resourceResult = routerRepository.getSystemResource()
            
            // Get user counts
            val usersResult = userRepository.getActiveUsers()
            
            _uiState.update { state ->
                state.copy(
                    routerName = router?.name ?: "",
                    routerIp = router?.host ?: "",
                    isConnected = routerRepository.isConnected(activeRouterId),
                    isLoading = false,
                    error = null
                )
            }
            
            if (resourceResult.isSuccess) {
                val data = resourceResult.getOrNull() ?: emptyMap()
                _uiState.update { state ->
                    state.copy(
                        uptime = data["uptime"] ?: "-",
                        cpuLoad = data["cpu-load"] ?: "-",
                        freeMemory = formatBytes(data["free-memory"]?.toLongOrNull() ?: 0),
                        totalMemory = formatBytes(data["total-memory"]?.toLongOrNull() ?: 0),
                        freeHdd = formatBytes(data["free-hdd-space"]?.toLongOrNull() ?: 0),
                        totalHdd = formatBytes(data["total-hdd-space"]?.toLongOrNull() ?: 0),
                        routerOsVersion = data["version"] ?: "-",
                        boardName = data["board-name"] ?: "-"
                    )
                }
            }
            
            if (usersResult.isSuccess) {
                val activeUsers = usersResult.getOrNull()?.size ?: 0
                _uiState.update { state ->
                    state.copy(activeUsers = activeUsers)
                }
            }
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
    
    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
            bytes >= 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
            bytes >= 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
