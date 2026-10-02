package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FileNamesTest {

    @Test
    fun normalNameIsKept() {
        assertEquals("b2-backup-2026-10-01.json", FileNames.backupName("b2-backup-2026-10-01.json"))
    }

    @Test
    fun missingNameGetsDefault() {
        assertEquals("b2-backup.json", FileNames.backupName(null))
        assertEquals("b2-backup.json", FileNames.backupName(""))
        assertEquals("b2-backup.json", FileNames.backupName("..."))
    }

    @Test
    fun spacesAreReplaced() {
        assertEquals("a_b.json", FileNames.backupName("a b.json"))
    }

    @Test
    fun extensionIsAdded() {
        assertEquals("backup.json", FileNames.backupName("backup"))
    }

    @Test
    fun pathTraversalIsNeutralised() {
        val result = FileNames.backupName("../../etc/passwd")
        assertFalse(result.contains("/"))
        assertFalse(result.startsWith("."))
        assertTrue(result.endsWith(".json"))
    }

    @Test
    fun longNamesAreCut() {
        val result = FileNames.backupName("a".repeat(100))
        assertEquals(65, result.length)
        assertTrue(result.endsWith(".json"))
    }
}
