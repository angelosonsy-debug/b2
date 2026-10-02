package com.b2tracker.app.core

object PackageNames {
    const val CHATGPT = "com.openai.chatgpt"

    private val PATTERN = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+\$")

    fun isValid(name: String?): Boolean = !name.isNullOrBlank() && PATTERN.matches(name)
}
