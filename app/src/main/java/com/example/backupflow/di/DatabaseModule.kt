package com.example.backupflow.di

import android.content.Context
import androidx.room.Room
import com.example.backupflow.data.local.BackupFlowDatabase
import com.example.backupflow.data.local.HistoryDao
import com.example.backupflow.data.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideBackupFlowDatabase(@ApplicationContext context: Context): BackupFlowDatabase {
        return Room.databaseBuilder(
            context,
            BackupFlowDatabase::class.java,
            "backupflow_db"
        ).build()
    }

    @Provides
    fun provideTaskDao(database: BackupFlowDatabase): TaskDao {
        return database.taskDao()
    }

    @Provides
    fun provideHistoryDao(database: BackupFlowDatabase): HistoryDao {
        return database.historyDao()
    }
}
