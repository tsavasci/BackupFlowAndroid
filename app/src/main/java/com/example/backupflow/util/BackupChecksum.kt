package com.example.backupflow.util

import android.content.Context
import android.net.Uri
import java.io.InputStream
import java.security.MessageDigest

object BackupChecksum {
    fun sha256Hex(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                computeSha256(stream)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun computeSha256(stream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(64 * 1024) // 64KB chunk
        var bytesRead: Int
        while (stream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        val hash = digest.digest()
        return hash.joinToString("") { "%02x".format(it) }
    }
}
