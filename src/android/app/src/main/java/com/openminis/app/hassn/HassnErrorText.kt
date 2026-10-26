package com.openminis.app.hassn

/**
 * [Hassn] Short Chinese wording for common LLM/gateway errors shown in the chat error card.
 * The raw text stays available (tap to expand, long-press to copy); this only picks a headline.
 */
object HassnErrorText {
    data class Friendly(val headline: String, val retrying: Boolean, val translated: Boolean)

    private val retryRe = Regex("""retrying\s*\((\d+)\s*/\s*(\d+)\)""", RegexOption.IGNORE_CASE)
    private val codeRe = Regex("""(?:\[|\b(?:HTTP|status|code)[\s:=]*)(\d{3})\b""", RegexOption.IGNORE_CASE)

    fun friendly(raw: String): Friendly {
        val s = raw.lowercase()
        val code = codeRe.find(raw)?.groupValues?.get(1)?.toIntOrNull()
        val base: String? = when {
            code == 429 || "rate limit" in s || "too many requests" in s || "rate_limit" in s -> "请求太频繁"
            code == 402 || "insufficient" in s || "quota" in s || "余额" in raw || "billing" in s -> "额度或余额不足"
            code == 401 || "unauthorized" in s || "invalid api key" in s || "invalid_api_key" in s || "incorrect api key" in s -> "密钥无效"
            code == 403 || "forbidden" in s -> "没有访问权限"
            code == 404 || "model_not_found" in s || "model not found" in s || "no available channel" in s -> "模型不可用"
            code == 503 || "overloaded" in s || "unavailable" in s || "busy" in s || "capacity" in s -> "服务繁忙"
            code == 504 || "timeout" in s || "timed out" in s || "deadline" in s -> "请求超时"
            code == 502 || "bad gateway" in s -> "网关异常"
            code == 500 || "internal server error" in s -> "服务器出错"
            "unknownhost" in s || "unable to resolve host" in s || "connection" in s || "network" in s ||
                "socket" in s || "ssl" in s || "failed to connect" in s -> "网络连接失败"
            "context" in s && ("length" in s || "too long" in s || "maximum" in s) -> "对话太长，超出模型上下文"
            else -> null
        }
        val m = retryRe.find(raw)
        val retryPart = m?.let { "，正在重试（${it.groupValues[1]}/${it.groupValues[2]}）…" }
        return when {
            base != null -> Friendly(base + (retryPart ?: ""), m != null, true)
            m != null -> Friendly("请求出错$retryPart", true, true)
            else -> Friendly(raw.removePrefix("Transient error: "), false, false)
        }
    }
}
