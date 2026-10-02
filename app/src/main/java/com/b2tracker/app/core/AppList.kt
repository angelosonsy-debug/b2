package com.b2tracker.app.core

object AppList {
    fun sorted(apps: List<AppInfo>): List<AppInfo> = apps.sortedBy { it.label.lowercase() }

    fun toJson(apps: List<AppInfo>): String =
        apps.joinToString(prefix = "[", postfix = "]", separator = ",") {
            "{\"label\":\"" + BridgeResult.escape(it.label) + "\",\"pkg\":\"" + BridgeResult.escape(it.pkg) + "\"}"
        }
}
