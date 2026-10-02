package com.b2tracker.app.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PackageNamesTest {

    @Test
    fun validNames() {
        assertTrue(PackageNames.isValid("com.openai.chatgpt"))
        assertTrue(PackageNames.isValid("com.example.hasiltee"))
        assertTrue(PackageNames.isValid("a.b"))
        assertTrue(PackageNames.isValid("com.my_app.v2"))
        assertTrue(PackageNames.isValid(PackageNames.CHATGPT))
    }

    @Test
    fun invalidNames() {
        assertFalse(PackageNames.isValid(null))
        assertFalse(PackageNames.isValid(""))
        assertFalse(PackageNames.isValid("   "))
        assertFalse(PackageNames.isValid("chatgpt"))
        assertFalse(PackageNames.isValid("com..x"))
        assertFalse(PackageNames.isValid("com.1abc"))
        assertFalse(PackageNames.isValid("com.x y"))
        assertFalse(PackageNames.isValid(" com.x.y"))
        assertFalse(PackageNames.isValid("com.x.y; rm -rf"))
    }
}
