package com.mikhmon.android.presentation.features.routers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.Router
import com.mikhmon.android.data.repository.RouterRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouterUiState(
    val routers: List<Router> = emptyList(),
    val activeRouterId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RouterViewModel @Inject constructor(
    private val routerRepository: RouterRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RouterUiState())
    val uiState: StateFlow<RouterUiState> = _uiState.asStateFlow()
    
    init {
        loadRouters()
    }
    
    private fun loadRouters() {
        viewModelScope.launch {
            routerRepository.getAllRouters().collect { routers ->
                _uiState.update { state ->
                    state.copy(
                        routers = routers,
                        activeRouterId = routerRepository.activeRouterId.value,
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun connectRouter(routerId: String) {
        viewModelScope.launch {
            Logger.info(Logger.Category.ROUTER, "Connecting to router: $routerId")
            _uiState.update { it.copy(isLoading = true) }
            
            val result = routerRepository.connect(routerId)
            
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        activeRouterId = routerId,
                        isLoading = false,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message
                    )
                }
            }
        }
    }
    
    fun disconnectRouter(routerId: String) {
        Logger.info(Logger.Category.ROUTER, "Disconnecting from router: $routerId")
        routerRepository.disconnect(routerId)
        _uiState.update { 
            it.copy(activeRouterId = null)
        }
    }
    
    fun deleteRouter(routerId: String) {
        viewModelScope.launch {
            Logger.info(Logger.Category.ROUTER, "Deleting router: $routerId")
            routerRepository.deleteRouter(routerId)
        }
    }
}
