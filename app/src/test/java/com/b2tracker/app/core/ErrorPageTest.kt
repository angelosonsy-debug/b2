package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorPageTest {

    @Test
    fun escapesHtml() {
        assertEquals("&lt;b&gt; &amp; &quot;x&quot;", ErrorPage.escape("<b> & \"x\""))
    }

    @Test
    fun detailIsEscapedInPage() {
        val html = ErrorPage.build("<script>alert(1)</script>")
        assertFalse(html.contains("<script>"))
        assertTrue(html.contains("&lt;script&gt;"))
    }

    @Test
    fun nullDetailHasFallbackText() {
        assertTrue(ErrorPage.build(null).contains("سبب غير معروف"))
    }

    @Test
    fun pageLinksBackToApp() {
        assertTrue(ErrorPage.build("x").contains(LinkPolicy.START_URL))
    }
}
