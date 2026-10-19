package com.example.backupflow.data.network.ftp

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class FTPService @Inject constructor() {
    private val ftpClient = FTPClient()

    @Throws(IOException::class)
    suspend fun connect(host: String, port: Int, username: String, password: String?) = withContext(Dispatchers.IO) {
        try {
            ftpClient.connect(host, port)
            val replyCode = ftpClient.replyCode
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect()
                throw IOException("FTP server refused connection. Reply code: $replyCode")
            }

            val user = username.ifEmpty { "anonymous" }
            val pass = password ?: "anonymous@"
            
            if (!ftpClient.login(user, pass)) {
                throw IOException("FTP login failed")
            }

            ftpClient.enterLocalPassiveMode()
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.controlEncoding = "UTF-8"
            // Enable UTF-8 support if server supports it (RFC 2640)
            ftpClient.sendCommand("OPTS UTF8 ON")
            
        } catch (e: Exception) {
            disconnect()
            throw e
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        if (ftpClient.isConnected) {
            try {
                ftpClient.logout()
            } catch (e: Exception) {
                // Ignore logout exceptions
            } finally {
                ftpClient.disconnect()
            }
        }
    }

    @Throws(IOException::class)
    suspend fun ensureDirectoryExists(directoryPath: String) = withContext(Dispatchers.IO) {
        val parts = directoryPath.split("/").filter { it.isNotEmpty() }
        var currentPath = ""

        // Preserve absolute path root if started with /
        if (directoryPath.startsWith("/")) {
            currentPath = "/"
        }

        for (part in parts) {
            currentPath = if (currentPath == "/") "/$part" else "$currentPath/$part"
            
            val success = ftpClient.changeWorkingDirectory(currentPath)
            if (!success) {
                // Try making the directory
                if (ftpClient.makeDirectory(currentPath)) {
                    if (!ftpClient.changeWorkingDirectory(currentPath)) {
                        throw IOException("Created directory but couldn't change into it: $currentPath")
                    }
                } else {
                    throw IOException("Could not create directory: $currentPath")
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun uploadFile(remotePath: String, inputStream: InputStream) = withContext(Dispatchers.IO) {
        val lastSlash = remotePath.lastIndexOf('/')
        val dir = if (lastSlash > 0) remotePath.substring(0, lastSlash) else "/"
        val fileName = remotePath.substring(lastSlash + 1)
        val tempFileName = "$fileName.tmp"

        ensureDirectoryExists(dir)

        // Upload to .tmp
        val uploadSuccess = ftpClient.storeFile(tempFileName, inputStream)
        if (!uploadSuccess) {
            ftpClient.deleteFile(tempFileName) // Try cleanup
            throw IOException("FTP storeFile failed for $tempFileName")
        }

        // Rename from .tmp to final name
        ftpClient.deleteFile(fileName) // Delete existing if any (overwrite mode)
        val renameSuccess = ftpClient.rename(tempFileName, fileName)
        if (!renameSuccess) {
            throw IOException("FTP rename failed from $tempFileName to $fileName")
        }
    }

    @Throws(IOException::class)
    suspend fun deleteFile(remotePath: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext ftpClient.deleteFile(remotePath)
    }
}
