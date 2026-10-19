package com.example.backupflow.data.network

import com.example.backupflow.domain.model.BackupFailureCategory
import com.example.backupflow.security.ChangedKeyException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import javax.net.ssl.SSLPeerUnverifiedException

object NetworkErrorMappers {

    fun mapCommonException(e: Exception): BackupFailureCategory {
        return when (e) {
            is UnknownHostException -> BackupFailureCategory.NETWORK
            is ConnectException -> BackupFailureCategory.NETWORK
            is SocketTimeoutException -> BackupFailureCategory.NETWORK
            is ChangedKeyException -> BackupFailureCategory.CERTIFICATE
            is SSLHandshakeException -> BackupFailureCategory.CERTIFICATE
            is SSLPeerUnverifiedException -> BackupFailureCategory.CERTIFICATE
            is SecurityException -> BackupFailureCategory.PERMISSION
            is IOException -> {
                val message = e.message?.lowercase() ?: ""
                when {
                    "no space left" in message || "quota" in message -> BackupFailureCategory.QUOTA
                    "permission denied" in message || "access denied" in message -> BackupFailureCategory.AUTH
                    else -> BackupFailureCategory.NETWORK
                }
            }
            else -> BackupFailureCategory.UNKNOWN
        }
    }
}
