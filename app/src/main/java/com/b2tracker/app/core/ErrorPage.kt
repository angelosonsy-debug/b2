package com.b2tracker.app.core

object ErrorPage {
    fun escape(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    fun build(detail: String?): String {
        val safe = escape(detail ?: "سبب غير معروف")
        return "<!DOCTYPE html><html dir=\"rtl\" lang=\"ar\"><head><meta charset=\"UTF-8\">" +
            "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>" +
            "<body style=\"background:#1B1918;color:#EDE7DC;font-family:sans-serif;padding:40px 20px;text-align:center;line-height:2\">" +
            "<h3>تعذّر تحميل التطبيق</h3><p>$safe</p>" +
            "<p><a style=\"color:#C9974B\" href=\"" + LinkPolicy.START_URL + "\">إعادة المحاولة</a></p>" +
            "</body></html>"
    }
}
