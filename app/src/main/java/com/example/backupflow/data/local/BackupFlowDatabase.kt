package com.example.backupflow.data.local

import androidx.room.*
import com.example.backupflow.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface TaskDao {
    @Query("SELECT * FROM backup_tasks")
    fun getAllTasksFlow(): Flow<List<BackupTaskEntity>>

    @Query("SELECT * FROM backup_tasks")
    suspend fun getAllTasks(): List<BackupTaskEntity>

    @Query("SELECT * FROM backup_tasks WHERE id = :id")
    suspend fun getTaskById(id: UUID): BackupTaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: BackupTaskEntity)

    @Delete
    suspend fun deleteTask(task: BackupTaskEntity)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM backup_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<BackupHistoryEntity>>

    @Query("SELECT * FROM backup_history ORDER BY timestamp DESC")
    suspend fun getAllHistory(): List<BackupHistoryEntity>

    @Query("SELECT * FROM backup_history WHERE taskId = :taskId ORDER BY timestamp DESC")
    suspend fun getHistoryForTask(taskId: UUID): List<BackupHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: BackupHistoryEntity)
}

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuidString: String?): UUID? = uuidString?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromBackupType(value: BackupType): String = value.name

    @TypeConverter
    fun toBackupType(value: String): BackupType = enumValueOf(value)

    @TypeConverter
    fun fromBackupStatus(value: BackupStatus): String = value.name

    @TypeConverter
    fun toBackupStatus(value: String): BackupStatus = enumValueOf(value)

    @TypeConverter
    fun fromBackupDeletionPolicy(value: BackupDeletionPolicy): String = value.name

    @TypeConverter
    fun toBackupDeletionPolicy(value: String): BackupDeletionPolicy = enumValueOf(value)

    @TypeConverter
    fun fromBackupChecksumMode(value: BackupChecksumMode): String = value.name

    @TypeConverter
    fun toBackupChecksumMode(value: String): BackupChecksumMode = enumValueOf(value)

    @TypeConverter
    fun fromBackupFailureCategory(value: BackupFailureCategory?): String? = value?.name

    @TypeConverter
    fun toBackupFailureCategory(value: String?): BackupFailureCategory? = value?.let { enumValueOf<BackupFailureCategory>(it) }

    @TypeConverter
    fun fromMediaType(value: MediaType): String = value.name

    @TypeConverter
    fun toMediaType(value: String): MediaType = enumValueOf(value)

    @TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType = enumValueOf(value)
}

@Database(entities = [BackupTaskEntity::class, BackupHistoryEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class BackupFlowDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun historyDao(): HistoryDao
}
