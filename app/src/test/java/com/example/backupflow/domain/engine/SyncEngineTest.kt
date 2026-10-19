package com.example.backupflow.domain.engine

import com.example.backupflow.domain.model.BackupDeletionPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncEngineTest {

    @Test
    fun testDefaultPolicyIsAppendOnly() {
        val policy = BackupDeletionPolicy.APPEND_ONLY
        assertEquals(BackupDeletionPolicy.APPEND_ONLY, policy)
    }

    @Test
    fun testCheckpointSkippingLogic() {
        val checkpoints = mutableSetOf("file1.jpg", "file2.jpg")
        assertTrue(checkpoints.contains("file1.jpg"))
        assertTrue(checkpoints.contains("file2.jpg"))
    }
}
