package com.example.backupflow.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BackupArchive {
    const val ARCHIVE_DIR = ".backupflow_archive"
    const val MANIFEST_FILE = ".backupflow_manifest.json"

    fun sanitize(relativePath: String): String {
        return relativePath.split("/")
            .filter { it.isNotBlank() && it != "." && it != ".." }
            .joinToString("/")
    }

    fun safeRemotePath(basePath: String, relativePath: String): String {
        val sanitizedBase = if (basePath == "/") "" else basePath.trimEnd('/')
        val sanitizedRelative = sanitize(relativePath)
        return if (sanitizedBase.isEmpty()) {
            if (sanitizedRelative.isEmpty()) "/" else "/$sanitizedRelative"
        } else {
            if (sanitizedRelative.isEmpty()) sanitizedBase else "$sanitizedBase/$sanitizedRelative"
        }
    }

    fun isReserved(relativePath: String): Boolean {
        val sanitized = sanitize(relativePath)
        return sanitized.startsWith(ARCHIVE_DIR) || sanitized == MANIFEST_FILE
    }

    fun destinationPath(basePath: String, relativePath: String): String {
        val formatter = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val timestamp = formatter.format(Date())
        val uuidShort = UUID.randomUUID().toString().take(8)
        val archiveBase = safeRemotePath(basePath, "$ARCHIVE_DIR/$timestamp-$uuidShort")
        return safeRemotePath(archiveBase, relativePath)
    }
}
