package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeActions : SystemActions {
    val installed = mutableSetOf<String>()
    var launchError: Exception? = null
    val launched = mutableListOf<String>()

    val failShareFor = mutableSetOf<String?>()
    val shares = mutableListOf<String?>()

    var clipboardError: Exception? = null
    val copied = mutableListOf<String>()

    var fileError: Exception? = null
    val files = mutableListOf<Triple<String, String, String>>()

    var apps: List<AppInfo> = emptyList()
    var appsError: Exception? = null

    override fun canLaunch(pkg: String): Boolean = installed.contains(pkg)

    override fun launch(pkg: String) {
        val err = launchError
        if (err != null) throw err
        launched.add(pkg)
    }

    override fun shareText(targetPkg: String?, text: String) {
        if (failShareFor.contains(targetPkg)) throw ActionException("not_found", "no app")
        shares.add(targetPkg)
    }

    override fun copyToClipboard(text: String) {
        val err = clipboardError
        if (err != null) throw err
        copied.add(text)
    }

    override fun shareFile(fileName: String, mime: String, content: String) {
        val err = fileError
        if (err != null) throw err
        files.add(Triple(fileName, mime, content))
    }

    override fun launchableApps(): List<AppInfo> {
        val err = appsError
        if (err != null) throw err
        return apps
    }
}

class BridgeLogicTest {

    private val fake = FakeActions()
    private val logic = BridgeLogic(fake)

    // ---- openApp ----

    @Test
    fun openApp_invalidPackage_failsWithoutLaunching() {
        val r = logic.openApp("not a package")
        assertFalse(r.ok)
        assertEquals("invalid_package", r.code)
        assertTrue(fake.launched.isEmpty())
    }

    @Test
    fun openApp_nullPackage_fails() {
        assertEquals("invalid_package", logic.openApp(null).code)
    }

    @Test
    fun openApp_notInstalled_failsAndDoesNotLaunch() {
        val r = logic.openApp("com.example.hasiltee")
        assertFalse(r.ok)
        assertEquals("not_installed", r.code)
        assertTrue(fake.launched.isEmpty())
    }

    @Test
    fun openApp_launchFailure_isReportedNotSwallowed() {
        fake.installed.add("com.example.hasiltee")
        fake.launchError = ActionException("security", "refused")
        val r = logic.openApp("com.example.hasiltee")
        assertFalse(r.ok)
        assertEquals("security", r.code)
    }

    @Test
    fun openApp_unexpectedException_isReported() {
        fake.installed.add("com.example.hasiltee")
        fake.launchError = IllegalStateException("boom")
        val r = logic.openApp("com.example.hasiltee")
        assertFalse(r.ok)
        assertEquals("unexpected", r.code)
    }

    @Test
    fun openApp_success() {
        fake.installed.add("com.example.hasiltee")
        val r = logic.openApp("  com.example.hasiltee ")
        assertTrue(r.ok)
        assertEquals("launched", r.code)
        assertEquals(listOf("com.example.hasiltee"), fake.launched)
    }

    // ---- shareToChatGPT ----

    @Test
    fun chatgpt_emptyText_failsWithoutSharing() {
        val r = logic.shareToChatGPT("   ")
        assertFalse(r.ok)
        assertEquals("empty_text", r.code)
        assertTrue(fake.shares.isEmpty())
    }

    @Test
    fun chatgpt_directShareWhenAppAvailable() {
        val r = logic.shareToChatGPT("hello")
        assertTrue(r.ok)
        assertEquals("chatgpt_app", r.code)
        assertEquals(listOf<String?>(PackageNames.CHATGPT), fake.shares)
    }

    @Test
    fun chatgpt_fallsBackToChooser() {
        fake.failShareFor.add(PackageNames.CHATGPT)
        val r = logic.shareToChatGPT("hello")
        assertTrue(r.ok)
        assertEquals("chooser", r.code)
        assertEquals(listOf<String?>(null), fake.shares)
    }

    @Test
    fun chatgpt_failsWhenNothingCanShare() {
        fake.failShareFor.add(PackageNames.CHATGPT)
        fake.failShareFor.add(null)
        val r = logic.shareToChatGPT("hello")
        assertFalse(r.ok)
        assertEquals("share_failed", r.code)
    }

    // ---- copyText ----

    @Test
    fun copy_success() {
        val r = logic.copyText("  hi ")
        assertTrue(r.ok)
        assertEquals(listOf("hi"), fake.copied)
    }

    @Test
    fun copy_emptyText_fails() {
        assertEquals("empty_text", logic.copyText("").code)
    }

    @Test
    fun copy_failureIsReported() {
        fake.clipboardError = ActionException("no_clipboard", "x")
        val r = logic.copyText("hi")
        assertFalse(r.ok)
        assertEquals("no_clipboard", r.code)
    }

    // ---- shareBackup ----

    @Test
    fun backup_success_usesSafeFileName() {
        val r = logic.shareBackup("../evil name", "{\"a\":1}")
        assertTrue(r.ok)
        assertEquals(1, fake.files.size)
        val (name, mime, content) = fake.files[0]
        assertFalse(name.contains("/"))
        assertTrue(name.endsWith(".json"))
        assertEquals("application/json", mime)
        assertEquals("{\"a\":1}", content)
    }

    @Test
    fun backup_emptyJson_fails() {
        assertEquals("empty_backup", logic.shareBackup("x.json", "  ").code)
        assertEquals("empty_backup", logic.shareBackup("x.json", null).code)
        assertTrue(fake.files.isEmpty())
    }

    @Test
    fun backup_tooLarge_fails() {
        val big = "x".repeat(BridgeLogic.MAX_BACKUP_CHARS + 1)
        val r = logic.shareBackup("x.json", big)
        assertFalse(r.ok)
        assertEquals("too_large", r.code)
        assertTrue(fake.files.isEmpty())
    }

    @Test
    fun backup_ioFailureIsReported() {
        fake.fileError = ActionException("io", "disk")
        val r = logic.shareBackup("x.json", "{}")
        assertFalse(r.ok)
        assertEquals("io", r.code)
    }

    // ---- listApps ----

    @Test
    fun listApps_sortedAndSerialised() {
        fake.apps = listOf(AppInfo("Zed", "z.z"), AppInfo("حصيلتي", "com.x.hasil"), AppInfo("Alpha", "a.a"))
        val r = logic.listApps()
        assertTrue(r.ok)
        val json = r.toJson()
        assertTrue(json.contains("\"data\":[{\"label\":\"Alpha\""))
        assertTrue(json.contains("com.x.hasil"))
    }

    @Test
    fun listApps_failureIsReported() {
        fake.appsError = ActionException("security", "no")
        val r = logic.listApps()
        assertFalse(r.ok)
        assertEquals("security", r.code)
    }
}
