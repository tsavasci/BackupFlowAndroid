package com.example.backupflow.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.backupflow.domain.model.*
import java.util.UUID

@Entity(tableName = "backup_tasks")
data class BackupTaskEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val displayName: String = "Backup Task",
    val sourcePath: String, // "PHOTOS_LIBRARY" or SAF Tree URI
    val backupType: BackupType = BackupType.FTP,
    val serverHost: String = "",
    val serverPort: Int,
    val serverUsername: String = "",
    val credentialKeyRef: String = "keychain:${UUID.randomUUID()}",
    val destinationPath: String = "/",
    val mediaType: MediaType = MediaType.PHOTOS_ONLY,
    val isEnabled: Boolean = true,
    val isPaused: Boolean = false,
    val frequencyType: FrequencyType = FrequencyType.MANUAL,
    val scheduledHour: Int? = 0,
    val scheduledMinute: Int? = 0,
    val scheduledWeekday: Int? = 1,
    val scheduledMonthDay: Int? = 1,
    val intervalHours: Int? = 6,
    val deletionPolicy: BackupDeletionPolicy = BackupDeletionPolicy.APPEND_ONLY,
    val checksumMode: BackupChecksumMode = BackupChecksumMode.OFF,
    val requiresWiFi: Boolean = false,
    val requiresExternalPower: Boolean = false,
    val allowCellularForLocalServer: Boolean = false,
    val includeCloudAssets: Boolean = true,
    val allowedStartHour: Int? = null,
    val allowedStartMinute: Int? = null,
    val allowedEndHour: Int? = null,
    val allowedEndMinute: Int? = null,
    val wifiWaitDeadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastBackupDate: Long? = null,
    val nextRunTime: Long? = null,
    val backupStatus: BackupStatus = BackupStatus.IDLE,
    val progress: Double = 0.0,
    val currentFile: String? = null,
    val currentFileProgress: Double = 0.0,
    val currentFileBytesTransferred: Long = 0,
    val currentFileTotalBytes: Long = 0,
    val transferSpeedBytesPerSec: Double = 0.0,
    val lastError: String? = null,
    val lastFailureCategory: BackupFailureCategory? = null,
    val consecutiveFailureCount: Int = 0
)
