package com.mikhmon.android.presentation.features.vouchers

import androidx.lifecycle.ViewModel
import com.mikhmon.android.data.model.Voucher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class VoucherUiState(
    val vouchers: List<Voucher> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class VoucherViewModel @Inject constructor(
    // TODO: Inject VoucherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(VoucherUiState())
    val uiState: StateFlow<VoucherUiState> = _uiState.asStateFlow()
    
    fun loadVouchers() {
        // TODO: Implement
        _uiState.value = _uiState.value.copy(
            vouchers = emptyList(),
            isLoading = false
        )
    }
}
