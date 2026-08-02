package com.mikhmon.android.presentation.features.login

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

data class LoginUiState(
    val savedRouters: List<Router> = emptyList(),
    val selectedRouter: Router? = null,
    val host: String = "",
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val routerRepository: RouterRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        loadSavedRouters()
    }
    
    private fun loadSavedRouters() {
        viewModelScope.launch {
            routerRepository.getAllRouters().collect { routers ->
                _uiState.update { state ->
                    val defaultRouter = routers.find { it.isDefault }
                    state.copy(
                        savedRouters = routers,
                        selectedRouter = defaultRouter,
                        host = defaultRouter?.host ?: "",
                        username = defaultRouter?.username ?: "",
                        password = defaultRouter?.password ?: ""
                    )
                }
            }
        }
    }
    
    fun selectRouter(routerId: String) {
        val router = _uiState.value.savedRouters.find { it.id == routerId }
        _uiState.update { state ->
            state.copy(
                selectedRouter = router,
                host = router?.host ?: "",
                username = router?.username ?: "",
                password = router?.password ?: ""
            )
        }
    }
    
    fun connect() {
        viewModelScope.launch {
            val state = _uiState.value
            
            if (state.host.isBlank()) {
                _uiState.update { it.copy(error = "Please enter router host/IP") }
                return@launch
            }
            
            if (state.username.isBlank()) {
                _uiState.update { it.copy(error = "Please enter username") }
                return@launch
            }
            
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // If we have a saved router, use its ID
            val routerId = state.selectedRouter?.id
            
            if (routerId != null) {
                Logger.info(Logger.Category.AUTH, "Connecting to saved router: ${state.selectedRouter?.name}")
                val result = routerRepository.connect(routerId)
                
                if (result.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = result.exceptionOrNull()?.message ?: "Connection failed"
                        )
                    }
                }
            } else {
                // No saved router - need to add it first
                val addResult = routerRepository.addRouter(
                    name = state.host,
                    host = state.host,
                    username = state.username,
                    password = state.password
                )
                
                if (addResult.isSuccess) {
                    val newRouter = addResult.getOrNull()
                    if (newRouter != null) {
                        val connectResult = routerRepository.connect(newRouter.id)
                        
                        if (connectResult.isSuccess) {
                            _uiState.update { it.copy(isLoading = false, isLoggedIn = true) }
                        } else {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    error = connectResult.exceptionOrNull()?.message ?: "Connection failed"
                                )
                            }
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = addResult.exceptionOrNull()?.message ?: "Failed to add router"
                        )
                    }
                }
            }
        }
    }
    
    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }
}
