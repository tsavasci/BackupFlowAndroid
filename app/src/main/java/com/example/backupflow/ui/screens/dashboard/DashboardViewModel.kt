package com.example.backupflow.ui.screens.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.backupflow.data.local.BackupHistoryEntity
import com.example.backupflow.data.local.BackupTaskEntity
import com.example.backupflow.data.local.HistoryDao
import com.example.backupflow.data.local.TaskDao
import com.example.backupflow.worker.BackupWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val tasks: List<BackupTaskEntity> = emptyList(),
    val recentHistory: List<BackupHistoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val taskDao: TaskDao,
    private val historyDao: HistoryDao
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        taskDao.getAllTasksFlow(),
        historyDao.getAllHistoryFlow()
    ) { tasks, history ->
        DashboardUiState(
            tasks = tasks,
            recentHistory = history.take(5),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    fun triggerTask(taskId: UUID) {
        val workRequest = OneTimeWorkRequestBuilder<BackupWorker>()
            .setInputData(workDataOf(BackupWorker.KEY_TASK_ID to taskId.toString()))
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun deleteTask(task: BackupTaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}
