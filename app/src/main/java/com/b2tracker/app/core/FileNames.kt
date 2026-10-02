package com.b2tracker.app.core

object FileNames {
    private val UNSAFE = Regex("[^A-Za-z0-9._-]")

    /** اسم ملف نسخة احتياطية آمن (من غير مسارات) وبينتهي بـ .json */
    fun backupName(raw: String?): String {
        val cleaned = (raw ?: "").replace(UNSAFE, "_").trimStart('.')
        val base = if (cleaned.isBlank()) "b2-backup" else cleaned
        val limited = if (base.length > 60) base.substring(0, 60) else base
        return if (limited.endsWith(".json")) limited else "$limited.json"
    }
}
