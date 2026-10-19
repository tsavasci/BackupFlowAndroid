package com.example.backupflow.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.backupflow.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_PROGRESS = "backup_progress"
        const val CHANNEL_RESULTS = "backup_results"
        const val NOTIFICATION_ID_PROGRESS = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val progressChannel = NotificationChannel(
                CHANNEL_PROGRESS,
                "Backup Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live backup transfer progress"
            }

            val resultsChannel = NotificationChannel(
                CHANNEL_RESULTS,
                "Backup Results",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies when a backup task completes or fails"
            }

            notificationManager.createNotificationChannel(progressChannel)
            notificationManager.createNotificationChannel(resultsChannel)
        }
    }

    fun buildProgressNotification(fileName: String, progressPercent: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_PROGRESS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Backing up files...")
            .setContentText(fileName)
            .setProgress(100, progressPercent, progressPercent == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    fun showResultNotification(title: String, message: String, notificationId: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_RESULTS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
