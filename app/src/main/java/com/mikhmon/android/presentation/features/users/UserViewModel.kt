package com.mikhmon.android.presentation.features.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikhmon.android.core.logging.Logger
import com.mikhmon.android.data.model.HotspotUser
import com.mikhmon.android.data.model.UserProfile
import com.mikhmon.android.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserListUiState(
    val users: List<HotspotUser> = emptyList(),
    val filteredUsers: List<HotspotUser> = emptyList(),
    val profiles: List<UserProfile> = emptyList(),
    val selectedProfile: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(UserListUiState())
    val uiState: StateFlow<UserListUiState> = _uiState.asStateFlow()
    
    init {
        loadUsers()
        loadProfiles()
    }
    
    fun loadUsers() {
        viewModelScope.launch {
            Logger.debug(Logger.Category.USER, "Loading users")
            _uiState.update { it.copy(isLoading = true) }
            
            val result = userRepository.getUsersByProfile(_uiState.value.selectedProfile ?: "")
            
            if (result.isSuccess) {
                val users = result.getOrNull() ?: emptyList()
                _uiState.update { state ->
                    state.copy(
                        users = users,
                        filteredUsers = filterUsers(users, state.searchQuery),
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
    
    private fun loadProfiles() {
        viewModelScope.launch {
            val result = userRepository.getUserProfiles()
            if (result.isSuccess) {
                _uiState.update { it.copy(profiles = result.getOrNull() ?: emptyList()) }
            }
        }
    }
    
    fun search(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredUsers = filterUsers(state.users, query)
            )
        }
    }
    
    fun filterByProfile(profile: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedProfile = profile, isLoading = true) }
            
            val result = if (profile != null) {
                userRepository.getUsersByProfile(profile)
            } else {
                userRepository.getUsersByProfile("")
            }
            
            if (result.isSuccess) {
                val users = result.getOrNull() ?: emptyList()
                _uiState.update { state ->
                    state.copy(
                        users = users,
                        filteredUsers = filterUsers(users, state.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }
    
    fun toggleUserEnabled(userId: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val result = userRepository.setUserEnabled(userId, isEnabled)
            if (result.isSuccess) {
                loadUsers()
            }
        }
    }
    
    fun deleteUser(userId: String) {
        viewModelScope.launch {
            Logger.info(Logger.Category.USER, "Deleting user: $userId")
            val result = userRepository.deleteUser(userId)
            if (result.isSuccess) {
                loadUsers()
            }
        }
    }
    
    private fun filterUsers(users: List<HotspotUser>, query: String): List<HotspotUser> {
        if (query.isBlank()) return users
        return users.filter { user ->
            user.name.contains(query, ignoreCase = true) ||
            user.comment.contains(query, ignoreCase = true) ||
            user.profile.contains(query, ignoreCase = true)
        }
    }
}
