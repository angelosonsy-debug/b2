package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class BridgeResultTest {

    @Test
    fun successJson() {
        assertEquals(
            "{\"ok\":true,\"code\":\"launched\",\"message\":\"done\"}",
            BridgeResult.success("launched", "done").toJson()
        )
    }

    @Test
    fun failureJson() {
        assertEquals(
            "{\"ok\":false,\"code\":\"not_installed\",\"message\":\"x\"}",
            BridgeResult.failure("not_installed", "x").toJson()
        )
    }

    @Test
    fun dataIsEmbeddedRaw() {
        assertEquals(
            "{\"ok\":true,\"code\":\"ok\",\"message\":\"\",\"data\":[1,2]}",
            BridgeResult.success("ok", "", "[1,2]").toJson()
        )
    }

    @Test
    fun specialCharactersAreEscaped() {
        assertEquals("a\\\"b\\\\c\\nd\\te", BridgeResult.escape("a\"b\\c\nd\te"))
    }

    @Test
    fun controlCharactersUseUnicodeEscape() {
        assertEquals("\\u0001", BridgeResult.escape("\u0001"))
    }

    @Test
    fun arabicIsKeptAsIs() {
        assertEquals("اتنسخ", BridgeResult.escape("اتنسخ"))
    }
}
