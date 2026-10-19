package com.example.backupflow.ui.screens.taskform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.backupflow.data.local.BackupTaskEntity
import com.example.backupflow.data.local.TaskDao
import com.example.backupflow.domain.model.BackupType
import com.example.backupflow.domain.model.MediaType
import com.example.backupflow.security.BackupCredentialStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class TaskFormUiState(
    val displayName: String = "My NAS Backup",
    val sourcePath: String = "PHOTOS_LIBRARY",
    val backupType: BackupType = BackupType.FTP,
    val serverHost: String = "",
    val serverPort: Int = 21,
    val serverUsername: String = "",
    val passwordInput: String = "",
    val destinationPath: String = "/",
    val mediaType: MediaType = MediaType.PHOTOS_ONLY,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val showPlaintextFtpWarning: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val taskDao: TaskDao,
    private val credentialStore: BackupCredentialStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskFormUiState())
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    fun updateProtocol(type: BackupType) {
        _uiState.value = _uiState.value.copy(
            backupType = type,
            serverPort = type.defaultPort,
            showPlaintextFtpWarning = type == BackupType.FTP
        )
    }

    fun updateHost(host: String) { _uiState.value = _uiState.value.copy(serverHost = host) }
    fun updatePort(portStr: String) {
        val port = portStr.toIntOrNull() ?: _uiState.value.backupType.defaultPort
        _uiState.value = _uiState.value.copy(serverPort = port)
    }
    fun updateUsername(user: String) { _uiState.value = _uiState.value.copy(serverUsername = user) }
    fun updatePassword(pass: String) { _uiState.value = _uiState.value.copy(passwordInput = pass) }
    fun updateDestinationPath(path: String) { _uiState.value = _uiState.value.copy(destinationPath = path) }

    fun saveTask() {
        val state = _uiState.value
        if (state.serverHost.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Server host is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true)
            val taskId = UUID.randomUUID()
            val credRef = "keychain:$taskId"

            if (state.passwordInput.isNotEmpty()) {
                credentialStore.savePassword(credRef, state.passwordInput)
            }

            val entity = BackupTaskEntity(
                id = taskId,
                displayName = state.displayName,
                sourcePath = state.sourcePath,
                backupType = state.backupType,
                serverHost = state.serverHost,
                serverPort = state.serverPort,
                serverUsername = state.serverUsername,
                credentialKeyRef = credRef,
                destinationPath = state.destinationPath,
                mediaType = state.mediaType
            )

            taskDao.insertTask(entity)
            _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
        }
    }
}
