package com.b2tracker.app.core

sealed class LinkAction {
    object LoadInApp : LinkAction()
    data class OpenExternal(val url: String) : LinkAction()
    object Block : LinkAction()
}

/** يحدد إيه اللي يحصل لأي رابط بتضغط عليه جوه التطبيق. */
object LinkPolicy {
    const val APP_HOST = "appassets.androidplatform.net"
    const val START_URL = "https://$APP_HOST/assets/index.html"

    fun decide(url: String?): LinkAction {
        val clean = url?.trim().orEmpty()
        if (clean.isEmpty()) return LinkAction.Block
        val lower = clean.lowercase()
        return when {
            lower.startsWith("https://$APP_HOST/") -> LinkAction.LoadInApp
            lower.startsWith("https://") || lower.startsWith("http://") -> LinkAction.OpenExternal(clean)
            else -> LinkAction.Block
        }
    }
}
