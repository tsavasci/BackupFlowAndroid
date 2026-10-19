package com.example.backupflow.security

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.cert.X509Certificate
import javax.inject.Inject
import javax.inject.Singleton

class ChangedKeyException(message: String) : Exception(message)

@Singleton
class SFTPHostKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences("sftp_host_keys", Context.MODE_PRIVATE)

    fun validate(fingerprint: String, host: String, port: Int): Boolean {
        val key = "$host:$port"
        val stored = preferences.getString(key, null)
        if (stored == null) {
            preferences.edit().putString(key, fingerprint).apply()
            return true
        }
        if (stored != fingerprint) {
            throw ChangedKeyException("SFTP Host Key has changed for $host:$port. Possible MITM attack.")
        }
        return false
    }

    fun storedFingerprint(host: String, port: Int): String? {
        return preferences.getString("$host:$port", null)
    }
}

@Singleton
class WebDAVCertificateStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val preferences = context.getSharedPreferences("webdav_certs", Context.MODE_PRIVATE)

    fun validate(certificate: X509Certificate, host: String, port: Int): Boolean {
        val key = "$host:$port"
        val fingerprint = computeSHA256(certificate.encoded)
        val stored = preferences.getString(key, null)
        if (stored == null) {
            preferences.edit().putString(key, fingerprint).apply()
            return true
        }
        if (stored != fingerprint) {
            throw ChangedKeyException("WebDAV Certificate has changed for $host:$port. Possible MITM attack.")
        }
        return false
    }

    private fun computeSHA256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
