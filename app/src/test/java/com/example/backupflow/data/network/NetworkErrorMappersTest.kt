package com.example.backupflow.data.network

import com.example.backupflow.domain.model.BackupFailureCategory
import com.example.backupflow.security.ChangedKeyException
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

class NetworkErrorMappersTest {

    @Test
    fun testMapUnknownHostException() {
        val category = NetworkErrorMappers.mapCommonException(UnknownHostException("Host not found"))
        assertEquals(BackupFailureCategory.NETWORK, category)
    }

    @Test
    fun testMapConnectException() {
        val category = NetworkErrorMappers.mapCommonException(ConnectException("Connection refused"))
        assertEquals(BackupFailureCategory.NETWORK, category)
    }

    @Test
    fun testMapChangedKeyException() {
        val category = NetworkErrorMappers.mapCommonException(ChangedKeyException("Fingerprint mismatch"))
        assertEquals(BackupFailureCategory.CERTIFICATE, category)
    }

    @Test
    fun testMapQuotaIOException() {
        val category = NetworkErrorMappers.mapCommonException(IOException("No space left on device"))
        assertEquals(BackupFailureCategory.QUOTA, category)
    }

    @Test
    fun testMapPermissionDeniedIOException() {
        val category = NetworkErrorMappers.mapCommonException(IOException("Permission denied"))
        assertEquals(BackupFailureCategory.AUTH, category)
    }
}
