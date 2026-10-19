package com.example.backupflow.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupArchiveTest {

    @Test
    fun testPathSanitizationStripsTraversals() {
        val rawPath = "../../etc/passwd"
        val sanitized = BackupArchive.sanitize(rawPath)
        assertEquals("etc/passwd", sanitized)
    }

    @Test
    fun testSafeRemotePathBuilding() {
        val safe = BackupArchive.safeRemotePath("/backup", "../photos/me.jpg")
        assertEquals("/backup/photos/me.jpg", safe)
    }

    @Test
    fun testReservedPathDetection() {
        assertTrue(BackupArchive.isReserved(".backupflow_archive/somefile.txt"))
        assertTrue(BackupArchive.isReserved(".backupflow_manifest.json"))
        assertFalse(BackupArchive.isReserved("DCIM/Camera/IMG_0001.jpg"))
    }
}
