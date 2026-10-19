package com.example.backupflow.data.network.sftp

import com.example.backupflow.security.SFTPHostKeyStore
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.HostKeyRepository
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class SFTPService @Inject constructor(
    private val hostKeyStore: SFTPHostKeyStore
) {
    private var session: Session? = null
    private var channelSftp: ChannelSftp? = null

    @Throws(IOException::class)
    suspend fun connect(host: String, port: Int, username: String, password: String?) = withContext(Dispatchers.IO) {
        try {
            val jSch = JSch()
            val newSession = jSch.getSession(username, host, port)
            if (!password.isNullOrEmpty()) {
                newSession.setPassword(password)
            }

            // Disable standard strict host key checking to allow custom TOFU checking
            val config = java.util.Properties()
            config["StrictHostKeyChecking"] = "no"
            newSession.setConfig(config)

            newSession.connect(15000) // 15 sec timeout

            // Verify host key via TOFU
            val hostKey = newSession.hostKey
            if (hostKey != null) {
                val fingerprint = "SHA256:" + hostKey.getFingerPrint(jSch)
                hostKeyStore.validate(fingerprint, host, port)
            }

            val channel = newSession.openChannel("sftp")
            channel.connect(15000)

            session = newSession
            channelSftp = channel as ChannelSftp

        } catch (e: Exception) {
            disconnect()
            throw IOException(e.message ?: "SFTP Connection failed", e)
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            channelSftp?.disconnect()
            session?.disconnect()
        } catch (e: Exception) {
            // Ignore disconnect errors
        } finally {
            channelSftp = null
            session = null
        }
    }

    @Throws(IOException::class)
    suspend fun ensureDirectoryExists(directoryPath: String) = withContext(Dispatchers.IO) {
        val sftp = channelSftp ?: throw IOException("SFTP not connected")
        val parts = directoryPath.split("/").filter { it.isNotEmpty() }
        var currentPath = if (directoryPath.startsWith("/")) "" else "."

        for (part in parts) {
            currentPath = "$currentPath/$part"
            try {
                sftp.cd(currentPath)
            } catch (e: SftpException) {
                try {
                    sftp.mkdir(currentPath)
                    sftp.cd(currentPath)
                } catch (e2: SftpException) {
                    throw IOException("Could not create SFTP directory: $currentPath", e2)
                }
            }
        }
    }

    @Throws(IOException::class)
    suspend fun uploadFile(remotePath: String, inputStream: InputStream) = withContext(Dispatchers.IO) {
        val sftp = channelSftp ?: throw IOException("SFTP not connected")
        val lastSlash = remotePath.lastIndexOf('/')
        val dir = if (lastSlash > 0) remotePath.substring(0, lastSlash) else "/"
        val fileName = remotePath.substring(lastSlash + 1)
        val tempFileName = "$fileName.tmp"

        ensureDirectoryExists(dir)
        sftp.cd(dir)

        val tempRemotePath = "$dir/$tempFileName"
        val finalRemotePath = "$dir/$fileName"

        try {
            sftp.put(inputStream, tempRemotePath, ChannelSftp.OVERWRITE)
        } catch (e: SftpException) {
            throw IOException("SFTP Put failed: ${e.message}", e)
        }

        try {
            // Delete existing target file if present
            try { sftp.rm(finalRemotePath) } catch (ignored: Exception) {}
            sftp.rename(tempRemotePath, finalRemotePath)
        } catch (e: SftpException) {
            throw IOException("SFTP Rename failed: ${e.message}", e)
        }
    }

    @Throws(IOException::class)
    suspend fun deleteFile(remotePath: String) = withContext(Dispatchers.IO) {
        val sftp = channelSftp ?: throw IOException("SFTP not connected")
        try {
            sftp.rm(remotePath)
        } catch (e: SftpException) {
            throw IOException("SFTP Delete failed: ${e.message}", e)
        }
    }
}
