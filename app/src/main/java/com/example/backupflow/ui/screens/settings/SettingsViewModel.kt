package com.example.backupflow.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backupflow.data.local.BackupFlowDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val minimumBatteryThreshold: Int = 30,
    val requireExternalPower: Boolean = false,
    val keepHistoryDays: Int = 30,
    val selectedLanguage: String = "en"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: BackupFlowDatabase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateBatteryThreshold(value: Int) {
        _uiState.value = _uiState.value.copy(minimumBatteryThreshold = value)
    }

    fun updateRequireExternalPower(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(requireExternalPower = enabled)
    }

    fun resetAllData() {
        viewModelScope.launch {
            database.clearAllTables()
        }
    }
}
