package com.openminis.app.hassn.ledger

import com.openminis.app.hassn.ledger.LedgerCore.Box
import com.openminis.app.hassn.ledger.LedgerCore.Mail
import com.openminis.app.hassn.ledger.LedgerCore.Source
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

/**
 * [Hassn-P1 临时邮箱台账] Where a mailbox comes from. Every call is blocking (run it on
 * Dispatchers.IO). Box secrets (Mail.tm password / token, Moakt session cookie) go through
 * [LedgerSecrets], i.e. the app's encrypted store; nothing here logs them.
 */
interface LedgerSecrets {
    fun get(key: String): String?
    fun put(key: String, value: String?)
}

data class CreatedBox(val address: String, val expiresAt: Long = 0L)

interface BoxSource {
    val source: Source

    /** Domains for the 域名 dropdown (may hit the network). */
    fun domains(): List<String>

    /** Makes a new box. [boxId] keys its secrets. */
    fun create(boxId: String, domain: String?, localPart: String, secrets: LedgerSecrets): Result<CreatedBox>

    /** Newest mail first (full text incl. links). Failure = the box can not be read (gone / not wired). */
    fun inbox(box: Box, secrets: LedgerSecrets, max: Int = 10): Result<List<Mail>>

    /** Keep the box alive; returns the new expiry (0 = the source does not say). */
    fun extend(box: Box, secrets: LedgerSecrets): Result<Long>
}

object LedgerSources {
    internal val http: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun of(s: Source): BoxSource = when (s) {
        Source.MAILTM -> MailTmSource
        Source.MOAKT -> MoaktSource
        Source.GMAIL_ALIAS -> GmailAliasSource
        Source.SHARED_GMAIL -> SharedGmailSource
    }
}

/** Mail.tm public API (api.mail.tm, no key, ≤ 8 req/s): /domains → /accounts → /token → /messages. */
object MailTmSource : BoxSource {
    override val source = Source.MAILTM
    private const val BASE = "https://api.mail.tm"
    private val JSON = "application/json".toMediaType()

    private fun call(method: String, path: String, body: JSONObject? = null, bearer: String? = null): Pair<Int, String> {
        val b = Request.Builder().url(BASE + path).header("Accept", "application/json")
        if (bearer != null) b.header("Authorization", "Bearer $bearer")
        if (body != null) b.method(method, body.toString().toRequestBody(JSON)) else b.get()
        return LedgerSources.http.newCall(b.build()).execute().use { it.code to (it.body?.string().orEmpty()) }
    }

    private fun members(json: String): JSONArray {
        val t = json.trim()
        return if (t.startsWith("[")) JSONArray(t) else JSONObject(t).let { it.optJSONArray("hydra:member") ?: it.optJSONArray("member") ?: JSONArray() }
    }

    override fun domains(): List<String> = runCatching {
        val (c, body) = call("GET", "/domains")
        if (c !in 200..299) return@runCatching emptyList()
        val a = members(body)
        (0 until a.length()).mapNotNull { a.optJSONObject(it) }
            .filter { it.optBoolean("isActive", true) && !it.optBoolean("isPrivate", false) }
            .map { it.optString("domain") }.filter { it.isNotBlank() }
    }.getOrDefault(emptyList())

    override fun create(boxId: String, domain: String?, localPart: String, secrets: LedgerSecrets): Result<CreatedBox> = runCatching {
        val doms = domains()
        val dom = domain?.takeIf { it in doms } ?: doms.firstOrNull() ?: error("Mail.tm 现在没有可用域名")
        val pass = LedgerCore.generatePassword(java.security.SecureRandom(), 20)
        var addr = "$localPart@$dom"
        var (code, _) = call("POST", "/accounts", JSONObject().put("address", addr).put("password", pass))
        if (code == 422) { // name taken: one more try
            addr = localPart + (100..999).random() + "@" + dom
            code = call("POST", "/accounts", JSONObject().put("address", addr).put("password", pass)).first
        }
        if (code == 429) error("Mail.tm 请求太快，稍后再试")
        if (code !in 200..299) error("Mail.tm 建邮箱没成功（HTTP $code）")
        secrets.put(LedgerStore.boxPassKey(boxId), pass)
        token(boxId, addr, secrets)
        CreatedBox(addr, 0L)
    }

    /** The saved token if it still works, else a new login with the saved box password. */
    private fun token(boxId: String, address: String, secrets: LedgerSecrets): String? {
        secrets.get(LedgerStore.boxTokenKey(boxId))?.let { t -> if (call("GET", "/me", bearer = t).first in 200..299) return t }
        val pass = secrets.get(LedgerStore.boxPassKey(boxId)) ?: return null
        val (c, body) = call("POST", "/token", JSONObject().put("address", address).put("password", pass))
        val t = if (c in 200..299) runCatching { JSONObject(body).optString("token") }.getOrNull()?.ifBlank { null } else null
        if (t != null) secrets.put(LedgerStore.boxTokenKey(boxId), t)
        return t
    }

    private fun isoMs(s: String): Long = runCatching { java.time.OffsetDateTime.parse(s).toInstant().toEpochMilli() }.getOrDefault(0L)

    override fun inbox(box: Box, secrets: LedgerSecrets, max: Int): Result<List<Mail>> = runCatching {
        val t = token(box.id, box.address, secrets) ?: error("邮箱登录失败（可能已被回收）")
        val (c, body) = call("GET", "/messages?page=1", bearer = t)
        if (c !in 200..299) error("读邮件失败（HTTP $c）")
        val a = members(body)
        (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { m ->
            Mail(m.optString("id"), m.optJSONObject("from")?.optString("address").orEmpty(), m.optString("subject"),
                m.optString("intro"), isoMs(m.optString("createdAt")))
        }.sortedByDescending { it.at }.take(max).map { m ->
            val (c2, full) = call("GET", "/messages/" + m.id, bearer = t)
            if (c2 !in 200..299) m else runCatching {
                val o = JSONObject(full)
                val html = o.optJSONArray("html")?.let { h -> (0 until h.length()).joinToString("\n") { h.optString(it) } } ?: o.optString("html")
                m.copy(text = listOf(o.optString("text"), LedgerCore.htmlText(html)).filter { it.isNotBlank() }.joinToString("\n"))
            }.getOrDefault(m)
        }
    }

    /** Mail.tm keeps a box that is used: a fresh login is the extend. */
    override fun extend(box: Box, secrets: LedgerSecrets): Result<Long> = runCatching {
        secrets.put(LedgerStore.boxTokenKey(box.id), null)
        token(box.id, box.address, secrets) ?: error("邮箱登录失败（可能已被回收）")
        0L
    }
}

/**
 * Moakt (moakt.com) through the endpoints its own web page uses (checked 2026-10-26):
 * POST /en/inbox (domain, username, setemail) → 302 + `tm_session` cookie; GET /en/inbox?getJson=true
 * (X-Requested-With) → address, expires_at, emails; POST /en/inbox/extend getJson=true → +1 h.
 * The box lives 1 h; when the session is gone the same name is asked for again.
 * TODO(moakt): the `emails` item shape was not seen with real mail yet; [parseEmails] reads the
 * likely field names and falls back to the message page's text.
 */
object MoaktSource : BoxSource {
    override val source = Source.MOAKT
    private const val BASE = "https://www.moakt.com/en"
    private const val UA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"
    private val noRedirect: OkHttpClient by lazy { LedgerSources.http.newBuilder().followRedirects(false).followSslRedirects(false).build() }

    override fun domains(): List<String> = LedgerCore.MOAKT_DOMAINS

    private fun cookieKey(boxId: String) = LedgerStore.boxTokenKey(boxId)

    private fun sessionFrom(headers: List<String>): String? =
        headers.firstNotNullOfOrNull { h -> Regex("tm_session=([^;]+)").find(h)?.groupValues?.get(1) }

    private fun open(boxId: String, address: String, secrets: LedgerSecrets): String {
        val req = Request.Builder().url("$BASE/inbox").header("User-Agent", UA)
            .post(FormBody.Builder().add("domain", address.substringAfter('@')).add("username", address.substringBefore('@')).add("setemail", "").build())
            .build()
        val session = noRedirect.newCall(req).execute().use { r ->
            if (r.code !in 200..399) error("Moakt 建邮箱没成功（HTTP ${r.code}）")
            sessionFrom(r.headers("Set-Cookie"))
        } ?: error("Moakt 没有给会话")
        secrets.put(cookieKey(boxId), session)
        return session
    }

    private fun get(path: String, session: String): Pair<Int, String> {
        val req = Request.Builder().url(BASE + path).header("User-Agent", UA).header("X-Requested-With", "XMLHttpRequest")
            .header("Cookie", "tm_session=$session").get().build()
        return LedgerSources.http.newCall(req).execute().use { it.code to (it.body?.string().orEmpty()) }
    }

    private fun post(path: String, session: String, form: Map<String, String>): Pair<Int, String> {
        val fb = FormBody.Builder().apply { form.forEach { (k, v) -> add(k, v) } }.build()
        val req = Request.Builder().url(BASE + path).header("User-Agent", UA).header("X-Requested-With", "XMLHttpRequest")
            .header("Cookie", "tm_session=$session").post(fb).build()
        return LedgerSources.http.newCall(req).execute().use { it.code to (it.body?.string().orEmpty()) }
    }

    /** "2026-10-26 05:21:00" (UTC on Moakt) → epoch ms */
    internal fun utcMs(s: String): Long = runCatching {
        LocalDateTime.parse(s.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).toInstant(ZoneOffset.UTC).toEpochMilli()
    }.getOrDefault(0L)

    private data class Inbox(val email: String, val expiresAt: Long, val emails: JSONArray?)

    private fun inboxJson(session: String): Inbox? {
        val (c, body) = get("/inbox?getJson=true", session)
        if (c !in 200..299 || !body.trimStart().startsWith("{")) return null
        val d = JSONObject(body).optJSONObject("data") ?: return null
        val a = d.optJSONObject("address") ?: return null
        val email = a.optString("email").ifBlank { return null }
        val exp = utcMs(a.optString("expires_at")).takeIf { it > 0 }
            ?: (System.currentTimeMillis() + a.optLong("seconds_left") * 1000L)
        return Inbox(email, exp, d.optJSONArray("emails"))
    }

    /** A working session for [box]: the saved one if it still shows this address, else ask for the same name again. */
    private fun session(box: Box, secrets: LedgerSecrets): Pair<String, Inbox> {
        secrets.get(cookieKey(box.id))?.let { s -> inboxJson(s)?.let { ib -> if (ib.email.equals(box.address, true)) return s to ib } }
        val s = open(box.id, box.address, secrets)
        val ib = inboxJson(s) ?: error("Moakt 收件箱打不开")
        if (!ib.email.equals(box.address, true)) error("这个地址已经被回收，换一个邮箱吧")
        return s to ib
    }

    override fun create(boxId: String, domain: String?, localPart: String, secrets: LedgerSecrets): Result<CreatedBox> = runCatching {
        val dom = domain?.takeIf { it in LedgerCore.MOAKT_DOMAINS } ?: LedgerCore.MOAKT_DOMAINS.first()
        val s = open(boxId, "$localPart@$dom", secrets)
        val ib = inboxJson(s) ?: error("Moakt 收件箱打不开")
        CreatedBox(ib.email.lowercase(), ib.expiresAt)
    }

    internal fun parseEmails(a: JSONArray?): List<Mail> {
        if (a == null) return emptyList()
        return (0 until a.length()).mapNotNull { a.optJSONObject(it) }.map { o ->
            val id = listOf("id", "uuid", "mail_id", "email_id").firstNotNullOfOrNull { k -> o.optString(k).takeIf { it.isNotBlank() } }.orEmpty()
            val from = o.optJSONObject("sender")?.let { it.optString("email").ifBlank { it.optString("address") } }
                ?: o.optJSONObject("from")?.let { it.optString("email").ifBlank { it.optString("address") } }
                ?: o.optString("from").ifBlank { o.optString("sender") }
            val text = listOf("text", "body", "content", "html", "intro", "preview").firstNotNullOfOrNull { k -> o.optString(k).takeIf { it.isNotBlank() } }.orEmpty()
            val at = listOf("created_at", "date", "received_at", "time").firstNotNullOfOrNull { k -> utcMs(o.optString(k)).takeIf { it > 0 } } ?: 0L
            Mail(id, from, o.optString("subject").ifBlank { o.optString("title") }, if (text.contains('<')) LedgerCore.htmlText(text) else text, at)
        }
    }

    override fun inbox(box: Box, secrets: LedgerSecrets, max: Int): Result<List<Mail>> = runCatching {
        val (s, ib) = session(box, secrets)
        parseEmails(ib.emails).sortedByDescending { it.at }.take(max).map { m ->
            if (m.id.isBlank() || LedgerCore.extractCode(m.subject + "\n" + m.text) != null) m else {
                val (c, body) = get("/email/${m.id}?getJson=true", s)
                if (c !in 200..299) m else {
                    val full = runCatching {
                        val d = JSONObject(body).optJSONObject("data") ?: JSONObject(body)
                        listOf("text", "body", "content", "html").firstNotNullOfOrNull { k -> d.optString(k).takeIf { it.isNotBlank() } }
                    }.getOrNull() ?: body
                    m.copy(text = m.text + "\n" + LedgerCore.htmlText(full))
                }
            }
        }
    }

    override fun extend(box: Box, secrets: LedgerSecrets): Result<Long> = runCatching {
        val (s, _) = session(box, secrets)
        val (c, body) = post("/inbox/extend", s, mapOf("getJson" to "true"))
        if (c !in 200..299) error("延时没成功（HTTP $c）")
        val a = runCatching { JSONObject(body).getJSONObject("data").getJSONObject("address") }.getOrNull()
        utcMs(a?.optString("expires_at").orEmpty()).takeIf { it > 0 }
            ?: (System.currentTimeMillis() + (a?.optLong("seconds_left") ?: 3600L) * 1000L)
    }
}

/**
 * Gmail 别名: `name+tag@gmail.com` on the user's own Gmail (address typed once on the 台账 page).
 * Nothing is created on Google's side. TODO(gmail): read codes through the Gmail mail connector;
 * until then the user reads them in Gmail and the agent asks the user.
 */
object GmailAliasSource : BoxSource {
    override val source = Source.GMAIL_ALIAS
    override fun domains(): List<String> = listOf("gmail.com")
    override fun create(boxId: String, domain: String?, localPart: String, secrets: LedgerSecrets): Result<CreatedBox> =
        Result.failure(IllegalStateException("Gmail 别名由台账直接生成"))
    override fun inbox(box: Box, secrets: LedgerSecrets, max: Int): Result<List<Mail>> =
        Result.failure(IllegalStateException("Gmail 别名的验证码还不能自动读取，请在 Gmail 里查看"))
    override fun extend(box: Box, secrets: LedgerSecrets): Result<Long> = Result.success(0L)
}

/** 共享临时 Gmail: TODO(shared-gmail) not wired; shown in the selector as 即将支持. */
object SharedGmailSource : BoxSource {
    override val source = Source.SHARED_GMAIL
    override fun domains(): List<String> = emptyList()
    override fun create(boxId: String, domain: String?, localPart: String, secrets: LedgerSecrets): Result<CreatedBox> =
        Result.failure(IllegalStateException("共享临时 Gmail 还没接入"))
    override fun inbox(box: Box, secrets: LedgerSecrets, max: Int): Result<List<Mail>> =
        Result.failure(IllegalStateException("共享临时 Gmail 还没接入"))
    override fun extend(box: Box, secrets: LedgerSecrets): Result<Long> = Result.success(0L)
}
