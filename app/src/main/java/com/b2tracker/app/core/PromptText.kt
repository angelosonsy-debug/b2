package com.b2tracker.app.core

object PromptText {
    const val MAX_LENGTH = 8000

    /** ينضّف النص قبل ما يتبعت لتطبيق تاني. يرجّع null لو فاضي. */
    fun sanitize(text: String?): String? {
        val t = text?.trim().orEmpty()
        if (t.isEmpty()) return null
        return if (t.length > MAX_LENGTH) t.substring(0, MAX_LENGTH) else t
    }
}
