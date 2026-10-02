package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class LinkPolicyTest {

    @Test
    fun appAssetsStayInApp() {
        assertEquals(
            LinkAction.LoadInApp,
            LinkPolicy.decide("https://appassets.androidplatform.net/assets/index.html")
        )
    }

    @Test
    fun hostComparisonIsCaseInsensitive() {
        assertEquals(
            LinkAction.LoadInApp,
            LinkPolicy.decide("HTTPS://APPASSETS.ANDROIDPLATFORM.NET/assets/index.html")
        )
    }

    @Test
    fun youtubeOpensExternally() {
        assertEquals(
            LinkAction.OpenExternal("https://www.youtube.com/results?search_query=a"),
            LinkPolicy.decide("  https://www.youtube.com/results?search_query=a  ")
        )
    }

    @Test
    fun plainHttpOpensExternally() {
        assertEquals(
            LinkAction.OpenExternal("http://example.com/x"),
            LinkPolicy.decide("http://example.com/x")
        )
    }

    @Test
    fun lookalikeHostIsNotTreatedAsApp() {
        assertEquals(
            LinkAction.OpenExternal("https://appassets.androidplatform.net.evil.com/x"),
            LinkPolicy.decide("https://appassets.androidplatform.net.evil.com/x")
        )
    }

    @Test
    fun dangerousSchemesAreBlocked() {
        listOf(
            "javascript:alert(1)",
            "file:///etc/passwd",
            "intent://chatgpt.com/#Intent;scheme=https;end",
            "content://media/external/file/1",
            "hasiltee://open"
        ).forEach { assertEquals(it, LinkAction.Block, LinkPolicy.decide(it)) }
    }

    @Test
    fun emptyAndNullAreBlocked() {
        assertEquals(LinkAction.Block, LinkPolicy.decide(null))
        assertEquals(LinkAction.Block, LinkPolicy.decide(""))
        assertEquals(LinkAction.Block, LinkPolicy.decide("   "))
    }

    @Test
    fun startUrlIsServedByAppHost() {
        assertEquals(LinkAction.LoadInApp, LinkPolicy.decide(LinkPolicy.START_URL))
    }
}
