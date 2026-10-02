package com.b2tracker.app.core

import org.junit.Assert.assertEquals
import org.junit.Test

class AppListTest {

    @Test
    fun sortsByLabelIgnoringCase() {
        val sorted = AppList.sorted(
            listOf(AppInfo("Zebra", "z.z"), AppInfo("apple", "a.a"), AppInfo("Mango", "m.m"))
        )
        assertEquals(listOf("apple", "Mango", "Zebra"), sorted.map { it.label })
    }

    @Test
    fun emptyListIsEmptyJsonArray() {
        assertEquals("[]", AppList.toJson(emptyList()))
    }

    @Test
    fun jsonContainsLabelAndPackage() {
        assertEquals(
            "[{\"label\":\"حصيلتي\",\"pkg\":\"com.x.y\"},{\"label\":\"a\\\"b\",\"pkg\":\"c.d\"}]",
            AppList.toJson(listOf(AppInfo("حصيلتي", "com.x.y"), AppInfo("a\"b", "c.d")))
        )
    }
}
