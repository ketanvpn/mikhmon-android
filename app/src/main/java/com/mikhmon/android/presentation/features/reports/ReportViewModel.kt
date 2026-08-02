package com.mikhmon.android.presentation.features.reports

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ReportUiState(
    val selectedPeriod: String = "today",
    val totalIncome: Double = 0.0,
    val vouchersSold: Int = 0,
    val reports: List<ReportItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ReportItem(
    val id: String,
    val date: String,
    val user: String,
    val price: Double,
    val profile: String,
    val validity: String
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    // TODO: Inject ReportRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()
    
    fun selectPeriod(period: String) {
        _uiState.value = _uiState.value.copy(selectedPeriod = period)
        loadReports()
    }
    
    fun loadReports() {
        // TODO: Implement report loading from MikroTik
        _uiState.value = _uiState.value.copy(
            totalIncome = 0.0,
            vouchersSold = 0,
            reports = emptyList(),
            isLoading = false
        )
    }
}
