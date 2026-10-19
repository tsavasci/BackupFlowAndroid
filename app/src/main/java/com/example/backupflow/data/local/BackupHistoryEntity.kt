package com.example.backupflow.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.backupflow.domain.model.BackupFailureCategory
import java.util.UUID

@Entity(
    tableName = "backup_history",
    foreignKeys = [
        ForeignKey(
            entity = BackupTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class BackupHistoryEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val taskId: UUID?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String,
    val filesProcessed: Int = 0,
    val totalFiles: Int = 0,
    val newFiles: Int = 0,
    val updatedFiles: Int = 0,
    val skippedFiles: Int = 0,
    val deletedFiles: Int = 0,
    val verifiedFiles: Int = 0,
    val bytesTransferred: Long = 0,
    val totalBytes: Long = 0,
    val durationSeconds: Double = 0.0,
    val errorMessage: String? = null,
    val failureCategory: BackupFailureCategory? = null,
    val previewSummarySnapshot: String? = null,
    val taskDisplayName: String? = null,
    val taskSourcePath: String? = null,
    val taskServerHost: String? = null,
    val taskServerPort: Int? = null,
    val taskUsername: String? = null,
    val taskIsAnonymous: Boolean? = null,
    val taskMediaTypeRaw: String? = null
)
