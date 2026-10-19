package com.example.backupflow.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.example.backupflow.data.local.BackupHistoryEntity
import com.example.backupflow.data.local.HistoryDao
import com.example.backupflow.data.local.TaskDao
import com.example.backupflow.domain.engine.SyncEngine
import com.example.backupflow.domain.model.BackupStatus
import com.example.backupflow.notification.BackupNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.UUID

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskDao: TaskDao,
    private val historyDao: HistoryDao,
    private val syncEngine: SyncEngine,
    private val backupNotifier: BackupNotifier
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TASK_ID = "KEY_TASK_ID"
    }

    override suspend fun doWork(): Result {
        val taskIdString = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
        val taskId = try { UUID.fromString(taskIdString) } catch (e: Exception) { return Result.failure() }

        val task = taskDao.getTaskById(taskId) ?: return Result.failure()

        // Attempt foreground service start
        try {
            val notification = backupNotifier.buildProgressNotification("Preparing...", 0)
            val foregroundInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ForegroundInfo(BackupNotifier.NOTIFICATION_ID_PROGRESS, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    ForegroundInfo(BackupNotifier.NOTIFICATION_ID_PROGRESS, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                }
            } else {
                ForegroundInfo(BackupNotifier.NOTIFICATION_ID_PROGRESS, notification)
            }
            setForeground(foregroundInfo)
        } catch (e: Exception) {
            // Android 12+ ForegroundServiceStartNotAllowedException fallback
            // Proceed running in background without foreground status
        }

        taskDao.insertTask(task.copy(backupStatus = BackupStatus.UPLOADING))

        val startTime = System.currentTimeMillis()
        val completedCheckpoints = mutableSetOf<String>()

        return try {
            val syncResult = syncEngine.executeBackup(
                task = task,
                isDryRun = false,
                completedCheckpoints = completedCheckpoints
            ) { currentFile, _, _, progress ->
                val progressPercent = (progress * 100).toInt()
                try {
                    val updatedNotification = backupNotifier.buildProgressNotification(currentFile, progressPercent)
                    // Update progress
                } catch (ignored: Exception) {}
            }

            val durationSec = (System.currentTimeMillis() - startTime) / 1000.0

            // Record success history
            historyDao.insertHistory(
                BackupHistoryEntity(
                    taskId = task.id,
                    status = "completed",
                    filesProcessed = syncResult.filesProcessed,
                    totalFiles = syncResult.filesProcessed + syncResult.skippedFiles,
                    newFiles = syncResult.newFiles,
                    updatedFiles = syncResult.updatedFiles,
                    skippedFiles = syncResult.skippedFiles,
                    deletedFiles = syncResult.deletedFiles,
                    bytesTransferred = syncResult.bytesTransferred,
                    totalBytes = syncResult.totalBytes,
                    durationSeconds = durationSec,
                    taskDisplayName = task.displayName,
                    taskSourcePath = task.sourcePath,
                    taskServerHost = task.serverHost,
                    taskServerPort = task.serverPort,
                    taskUsername = task.serverUsername
                )
            )

            taskDao.insertTask(task.copy(backupStatus = BackupStatus.COMPLETED, lastBackupDate = System.currentTimeMillis()))
            backupNotifier.showResultNotification("Backup Complete", "${task.displayName} finished successfully", task.id.hashCode())

            Result.success()
        } catch (e: Exception) {
            val durationSec = (System.currentTimeMillis() - startTime) / 1000.0

            historyDao.insertHistory(
                BackupHistoryEntity(
                    taskId = task.id,
                    status = "failed",
                    durationSeconds = durationSec,
                    errorMessage = e.message,
                    taskDisplayName = task.displayName
                )
            )

            taskDao.insertTask(task.copy(backupStatus = BackupStatus.FAILED, lastError = e.message))
            backupNotifier.showResultNotification("Backup Failed", "${task.displayName}: ${e.message}", task.id.hashCode())

            Result.failure()
        }
    }
}
