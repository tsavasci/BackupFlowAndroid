package com.example.backupflow.domain.engine

import android.content.Context
import android.net.Uri
import com.example.backupflow.data.local.BackupTaskEntity
import com.example.backupflow.data.network.ftp.FTPService
import com.example.backupflow.data.network.sftp.SFTPService
import com.example.backupflow.data.network.webdav.WebDAVService
import com.example.backupflow.data.provider.FileAccessManager
import com.example.backupflow.data.provider.LocalFileItem
import com.example.backupflow.data.provider.PhotoLibraryManager
import com.example.backupflow.domain.model.BackupDeletionPolicy
import com.example.backupflow.domain.model.BackupType
import com.example.backupflow.security.BackupCredentialStore
import com.example.backupflow.util.BackupArchive
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

data class SyncResult(
    val filesProcessed: Int,
    val newFiles: Int,
    val updatedFiles: Int,
    val skippedFiles: Int,
    val deletedFiles: Int,
    val bytesTransferred: Long,
    val totalBytes: Long
)

class SyncEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val photoLibraryManager: PhotoLibraryManager,
    private val fileAccessManager: FileAccessManager,
    private val credentialStore: BackupCredentialStore,
    private val ftpService: FTPService,
    private val sftpService: SFTPService,
    private val webDAVService: WebDAVService
) {

    suspend fun executeBackup(
        task: BackupTaskEntity,
        isDryRun: Boolean = false,
        completedCheckpoints: MutableSet<String> = mutableSetOf(),
        onProgress: (currentFile: String, bytesDone: Long, totalBytes: Long, overallProgress: Double) -> Unit
    ): SyncResult = withContext(Dispatchers.IO) {

        // 1. Fetch Local Files
        val localFiles: List<LocalFileItem> = if (task.sourcePath == "PHOTOS_LIBRARY") {
            photoLibraryManager.fetchMediaAssets(task.mediaType)
        } else {
            val treeUri = Uri.parse(task.sourcePath)
            fileAccessManager.listFilesFromTreeUri(treeUri)
        }

        // Failsafe 1: If source is completely empty or inaccessible, abort orphan deletion
        val isSourceInaccessibleOrEmpty = localFiles.isEmpty()

        val password = credentialStore.getPassword(task.credentialKeyRef)

        var newCount = 0
        var updatedCount = 0
        var skippedCount = 0
        var deletedCount = 0
        var totalBytesTransferred = 0L
        val totalBytesToTransfer = localFiles.sumOf { it.size }

        // Connect protocol
        when (task.backupType) {
            BackupType.FTP -> ftpService.connect(task.serverHost, task.serverPort, task.serverUsername, password)
            BackupType.SFTP -> sftpService.connect(task.serverHost, task.serverPort, task.serverUsername, password)
            BackupType.WEBDAV -> webDAVService.connect(task.serverHost, task.serverPort, task.serverUsername, password, isHttps = task.serverPort == 443)
        }

        try {
            val totalCount = localFiles.size
            for ((index, item) in localFiles.withIndex()) {
                val relPath = item.relativePath

                // Checkpoint check (Android 15 timeout resilience)
                if (completedCheckpoints.contains(relPath)) {
                    skippedCount++
                    continue
                }

                if (isDryRun) {
                    newCount++
                    continue
                }

                // Upload
                val inputStream: InputStream? = context.contentResolver.openInputStream(item.uri)
                if (inputStream != null) {
                    inputStream.use { stream ->
                        val remoteDestination = BackupArchive.safeRemotePath(task.destinationPath, relPath)
                        when (task.backupType) {
                            BackupType.FTP -> ftpService.uploadFile(remoteDestination, stream)
                            BackupType.SFTP -> sftpService.uploadFile(remoteDestination, stream)
                            BackupType.WEBDAV -> webDAVService.uploadFile(remoteDestination, stream)
                        }
                    }
                    totalBytesTransferred += item.size
                    newCount++
                    completedCheckpoints.add(relPath)

                    val overallProgress = if (totalCount > 0) (index + 1).toDouble() / totalCount else 1.0
                    onProgress(item.relativePath, totalBytesTransferred, totalBytesToTransfer, overallProgress)
                }
            }

            // Orphan Cleanup Failsafe Verification
            if (!isDryRun && task.deletionPolicy != BackupDeletionPolicy.APPEND_ONLY && !isSourceInaccessibleOrEmpty) {
                if (task.sourcePath == "PHOTOS_LIBRARY" && !photoLibraryManager.hasFullPhotoAccess()) {
                    // Safety Guard: Don't delete if we don't have FULL access
                } else {
                    // Orphan deletion logic would execute here safely
                }
            }

        } finally {
            when (task.backupType) {
                BackupType.FTP -> ftpService.disconnect()
                BackupType.SFTP -> sftpService.disconnect()
                BackupType.WEBDAV -> webDAVService.disconnect()
            }
        }

        return@withContext SyncResult(
            filesProcessed = newCount + updatedCount,
            newFiles = newCount,
            updatedFiles = updatedCount,
            skippedFiles = skippedCount,
            deletedFiles = deletedCount,
            bytesTransferred = totalBytesTransferred,
            totalBytes = totalBytesToTransfer
        )
    }
}
