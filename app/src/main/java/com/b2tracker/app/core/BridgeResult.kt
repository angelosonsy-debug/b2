package com.b2tracker.app.core

/** رد موحّد للـ JavaScript: { ok, code, message, data? } */
data class BridgeResult(
    val ok: Boolean,
    val code: String,
    val message: String,
    val data: String? = null
) {
    fun toJson(): String {
        val sb = StringBuilder()
        sb.append("{\"ok\":").append(ok)
        sb.append(",\"code\":\"").append(escape(code)).append("\"")
        sb.append(",\"message\":\"").append(escape(message)).append("\"")
        if (data != null) sb.append(",\"data\":").append(data)
        sb.append("}")
        return sb.toString()
    }

    companion object {
        fun success(code: String = "ok", message: String = "", data: String? = null): BridgeResult =
            BridgeResult(true, code, message, data)

        fun failure(code: String, message: String): BridgeResult =
            BridgeResult(false, code, message)

        fun escape(s: String): String {
            val sb = StringBuilder()
            for (ch in s) {
                when (ch) {
                    '\\' -> sb.append("\\\\")
                    '"' -> sb.append("\\\"")
                    '\n' -> sb.append("\\n")
                    '\r' -> sb.append("\\r")
                    '\t' -> sb.append("\\t")
                    else -> if (ch < ' ') {
                        sb.append(String.format("\\u%04x", ch.code))
                    } else {
                        sb.append(ch)
                    }
                }
            }
            return sb.toString()
        }
    }
}
