package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PromptTextTest {

    @Test
    fun nullAndBlankGiveNull() {
        assertNull(PromptText.sanitize(null))
        assertNull(PromptText.sanitize(""))
        assertNull(PromptText.sanitize("   \n  "))
    }

    @Test
    fun trimsWhitespace() {
        assertEquals("hi", PromptText.sanitize("  hi \n"))
    }

    @Test
    fun longTextIsCut() {
        val result = PromptText.sanitize("a".repeat(9000))
        assertEquals(PromptText.MAX_LENGTH, result?.length)
    }

    @Test
    fun exactLimitIsKept() {
        val text = "b".repeat(PromptText.MAX_LENGTH)
        assertEquals(text, PromptText.sanitize(text))
    }

    @Test
    fun arabicTextSurvives() {
        assertEquals("اسألني عن يومي", PromptText.sanitize("  اسألني عن يومي "))
    }
}
