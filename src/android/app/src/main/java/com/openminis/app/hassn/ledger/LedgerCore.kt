package com.openminis.app.hassn.ledger

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Random

/**
 * [Hassn-P1 临时邮箱台账] Pure side of the temp-mailbox ledger (no Android imports; unit-tested in
 * LedgerCoreTest). Ported from Mines feat/register-auto (RegLedgerCore / RegisterAutoCore / MailParse)
 * and simplified to the approved mockups 14 / 14b / 14c:
 *
 * - [Box]: one mailbox (Mail.tm, Moakt, Gmail 别名, 共享临时 Gmail) with its keep-alive switches.
 * - [Signup]: one platform registered with a box (name, URL, status, time). The platform password
 *   is generated here ([generatePassword]) and lives only in the encrypted store, keyed by the
 *   signup id; this file never sees it after generation.
 * - [Received]: codes / verification links read from a box.
 *
 * The 台账 is separate from the user's own accounts (账号): no login-status checks, nothing from
 * the vault logins. Captcha / phone / payment are never automated: those rows get [PStatus.NEEDS_USER].
 */
object LedgerCore {

    // ── sources ──

    enum class Source(
        val id: String,
        /** the selector label: 「临时 · Moakt」 */
        val label: String,
        /** the tag under an address: 「临时 · Moakt」 / 「Gmail 别名」 / 「共享」 */
        val tag: String,
        val temp: Boolean,
        /** creating a box works now; false = stub that tells the user it is not wired yet */
        val ready: Boolean,
        /** codes can be read by the app itself */
        val readable: Boolean,
        /** the source has an expiry that can be pushed back */
        val extendable: Boolean,
    ) {
        MAILTM("mailtm", "临时 · Mail.tm", "临时 · Mail.tm", temp = true, ready = true, readable = true, extendable = true),
        MOAKT("moakt", "临时 · Moakt", "临时 · Moakt", temp = true, ready = true, readable = true, extendable = true),
        GMAIL_ALIAS("gmail_alias", "Gmail 别名", "Gmail 别名", temp = false, ready = true, readable = false, extendable = false),
        SHARED_GMAIL("shared_gmail", "共享临时 Gmail", "共享", temp = true, ready = false, readable = false, extendable = false);

        companion object {
            fun of(id: String?): Source = entries.firstOrNull { it.id.equals(id, true) } ?: MAILTM
        }
    }

    /** Moakt's public domains (moakt.com form, checked 2026-10-26). */
    val MOAKT_DOMAINS = listOf(
        "drmail.in", "teml.net", "tmpeml.com", "tmpbox.net", "moakt.cc", "disbox.net", "tmpmail.org",
        "tmpmail.net", "tmails.net", "disbox.org", "moakt.co", "moakt.ws", "tmail.ws", "bareed.ws",
    )

    /** Moakt boxes live 1 h; extend when less than this is left. */
    const val MOAKT_LEAD_MS = 20 * 60_000L
    /** Sources with a long or unknown expiry: re-login once a day keeps them alive. */
    const val DAILY_MS = 24 * 3600_000L
    val PROBE_HOURS = listOf(6, 12, 24)

    // ── records ──

    enum class PStatus(val zh: String) {
        VERIFIED("已验证"),
        PENDING("未完成"),
        /** captcha / phone / payment: the user finishes it by hand */
        NEEDS_USER("等你处理"),
        FAILED("失败");

        companion object {
            fun of(s: String?): PStatus = entries.firstOrNull { it.name.equals(s, true) }
                ?: when (s?.trim()?.lowercase()) {
                    "verified", "ok", "done", "已验证" -> VERIFIED
                    "needs_user", "captcha", "phone", "payment", "等你处理" -> NEEDS_USER
                    "failed", "fail", "失败" -> FAILED
                    else -> PENDING
                }
        }
    }

    data class Box(
        val id: String,
        val address: String,
        val source: Source,
        val createdAt: Long,
        /** 0 = the source does not say */
        val expiresAt: Long = 0L,
        val autoExtend: Boolean = true,
        val probe: Boolean = false,
        val probeEveryH: Int = 6,
        val lastExtendAt: Long = 0L,
        val lastProbeAt: Long = 0L,
        val lastProbeOk: Boolean = true,
        val lastNote: String = "",
        /** newest mail seen in the box (0 = none yet) */
        val lastMailAt: Long = 0L,
    ) {
        val domain: String get() = address.substringAfter('@', "")
    }

    data class Signup(
        val id: String,
        val boxId: String,
        val platform: String,
        val url: String,
        val status: PStatus = PStatus.PENDING,
        val createdAt: Long,
        val updatedAt: Long = createdAt,
        val note: String = "",
        /** a password for this signup is in the encrypted store */
        val hasPassword: Boolean = false,
    ) {
        val host: String get() = hostOf(url)
    }

    enum class Kind { CODE, LINK }

    data class Received(
        val boxId: String,
        val kind: Kind,
        val value: String,
        val from: String,
        val subject: String,
        val at: Long,
        val used: Boolean = false,
    )

    data class State(
        val boxes: List<Box> = emptyList(),
        val signups: List<Signup> = emptyList(),
        val received: List<Received> = emptyList(),
    )

    // ── masking ──

    /**
     * Masked address for every screen and every tool reply. Temp boxes share a prefix, so the last
     * characters stay visible and two boxes never look the same:
     * `hsnq7w3e5r1@tmpbox.net` → `hsnq••e5r1@tmpbox.net`, `deningyang740+ai@gmail.com` → `deningyang••+ai@gmail.com`.
     */
    fun maskBox(address: String): String {
        val a = address.trim()
        val at = a.indexOf('@')
        if (at <= 0) return a
        val local = a.substring(0, at)
        val domain = a.substring(at)
        val plus = local.indexOf('+')
        if (plus > 0) {
            val base = local.substring(0, plus)
            val tag = local.substring(plus)
            val keep = if (base.length > 4) base.length - 3 else 1
            return base.take(keep) + "••" + tag + domain
        }
        return maskLocal(local) + domain
    }

    private fun maskLocal(u: String): String = when {
        u.length >= 10 -> u.take(4) + "••" + u.takeLast(4)
        u.length >= 6 -> u.take(2) + "••" + u.takeLast(2)
        else -> u.take(1) + "••"
    }

    /** `••••••` of a fixed length: the real length is never shown. */
    const val PASSWORD_DOTS = "••••••"

    // ── password ──

    private const val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ"
    private const val LOWER = "abcdefghijkmnopqrstuvwxyz"
    private const val DIGIT = "23456789"
    private const val SYMBOL = "!@#%^*-_=+?"

    /** Strong password, at least 16 chars, one of each class, no look-alike chars (0/O, 1/l/I). */
    fun generatePassword(rnd: Random, length: Int = 18): String {
        val n = length.coerceIn(16, 64)
        val all = UPPER + LOWER + DIGIT + SYMBOL
        val chars = ArrayList<Char>(n)
        chars += UPPER[rnd.nextInt(UPPER.length)]
        chars += LOWER[rnd.nextInt(LOWER.length)]
        chars += DIGIT[rnd.nextInt(DIGIT.length)]
        chars += SYMBOL[rnd.nextInt(SYMBOL.length)]
        while (chars.size < n) chars += all[rnd.nextInt(all.length)]
        for (i in chars.size - 1 downTo 1) {
            val j = rnd.nextInt(i + 1)
            val t = chars[i]; chars[i] = chars[j]; chars[j] = t
        }
        return chars.joinToString("")
    }

    fun isStrong(p: String): Boolean =
        p.length >= 16 && p.any { it.isUpperCase() } && p.any { it.isLowerCase() } && p.any { it.isDigit() } && p.any { it in SYMBOL }

    /** Local part for a new temp box: `hsn` + 8 lowercase letters/digits. */
    fun newLocalPart(rnd: Random): String {
        val abc = "abcdefghijkmnpqrstuvwxyz23456789"
        return "hsn" + (1..8).map { abc[rnd.nextInt(abc.length)] }.joinToString("")
    }

    fun newId(rnd: Random, prefix: String): String {
        val abc = "abcdefghijkmnpqrstuvwxyz23456789"
        return prefix + (1..8).map { abc[rnd.nextInt(abc.length)] }.joinToString("")
    }

    /** Gmail 别名: `name@gmail.com` + tag → `name+tag@gmail.com`; null when the base is not a Gmail address. */
    fun gmailAlias(base: String, tag: String): String? {
        val b = base.trim().lowercase()
        if (!Regex("^[a-z0-9.]+@(gmail|googlemail)\\.com$").matches(b)) return null
        val t = tag.trim().lowercase().replace(Regex("[^a-z0-9]"), "").take(20).ifEmpty { return null }
        return b.substringBefore('@').substringBefore('+') + "+" + t + "@" + b.substringAfter('@')
    }

    // ── platforms ──

    fun hostOf(url: String): String =
        url.trim().lowercase().substringAfter("://").substringBefore('/').substringBefore('?')
            .substringBefore('#').substringBefore(':').removePrefix("www.")

    private val IP = Regex("""^\d{1,3}(?:\.\d{1,3}){3}$""")

    /** The name a row shows: the given name, else the domain; never a raw IP or URL (「自定义网站」). */
    fun platformTitle(platform: String, url: String): String {
        val p = platform.trim()
        if (p.isNotBlank() && !p.contains("://") && !p.contains('/') && !IP.matches(p)) return p
        val h = hostOf(url)
        return if (h.isNotBlank() && !IP.matches(h) && h != "localhost") h else "自定义网站"
    }

    /** The letter tile on a platform row. */
    fun letter(title: String): String = title.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

    fun normalizeUrl(url: String): String {
        val u = url.trim()
        if (u.isBlank()) return ""
        return if (u.contains("://")) u else "https://$u"
    }

    // ── list / grouping ──

    data class BoxRow(val box: Box, val platforms: Int, val lastAt: Long, val platformNames: List<String>)

    /** Newest activity of a box: its last mail, else its newest signup, else when it was made. */
    fun lastActivity(box: Box, signups: List<Signup>): Long =
        maxOf(box.lastMailAt, signups.filter { it.boxId == box.id }.maxOfOrNull { it.updatedAt } ?: 0L, box.createdAt)

    /** The list: only mailboxes, newest first, filtered by [query] (address, platform name or host). */
    fun rows(s: State, query: String = ""): List<BoxRow> {
        val q = query.trim().lowercase()
        return s.boxes.map { b ->
            val su = s.signups.filter { it.boxId == b.id }
            BoxRow(b, su.size, lastActivity(b, s.signups), su.map { platformTitle(it.platform, it.url) })
        }.filter { r ->
            q.isEmpty() || r.box.address.lowercase().contains(q) || maskBox(r.box.address).lowercase().contains(q) ||
                r.platformNames.any { it.lowercase().contains(q) } ||
                s.signups.any { it.boxId == r.box.id && it.host.contains(q) }
        }.sortedByDescending { it.lastAt }
    }

    /** 「6 个邮箱 · 18 个平台」 */
    fun totals(s: State): Pair<Int, Int> = s.boxes.size to s.signups.count { su -> s.boxes.any { it.id == su.boxId } }

    fun signupsOf(s: State, boxId: String): List<Signup> =
        s.signups.filter { it.boxId == boxId }.sortedByDescending { it.createdAt }

    fun receivedOf(s: State, boxId: String, max: Int = 6): List<Received> =
        s.received.filter { it.boxId == boxId }.sortedByDescending { it.at }.take(max)

    /** One signup per (box, host): a second record for the same site updates the first. */
    fun upsertSignup(list: List<Signup>, r: Signup): List<Signup> {
        val old = list.firstOrNull { it.id == r.id }
            ?: list.firstOrNull { it.boxId == r.boxId && r.host.isNotBlank() && it.host == r.host }
        val merged = if (old == null) r else r.copy(
            id = old.id,
            platform = r.platform.ifBlank { old.platform },
            url = r.url.ifBlank { old.url },
            createdAt = old.createdAt,
            hasPassword = r.hasPassword || old.hasPassword,
        )
        return list.filterNot { it.id == merged.id } + merged
    }

    /** Keeps the newest 30 codes/links per box, newest first, no duplicates. */
    fun addReceived(list: List<Received>, new: List<Received>): List<Received> {
        val all = (new + list).distinctBy { it.boxId + "|" + it.kind + "|" + it.value }
        return all.groupBy { it.boxId }.values.flatMap { l -> l.sortedByDescending { it.at }.take(30) }
    }

    fun removeBox(s: State, boxId: String): State = State(
        boxes = s.boxes.filterNot { it.id == boxId },
        signups = s.signups.filterNot { it.boxId == boxId },
        received = s.received.filterNot { it.boxId == boxId },
    )

    /** A box named by id, full address or masked address (the agent only ever sees the last two). */
    fun findBox(s: State, ref: String): Box? {
        val r = ref.trim()
        if (r.isEmpty()) return null
        return s.boxes.firstOrNull { it.id == r }
            ?: s.boxes.firstOrNull { it.address.equals(r, true) }
            ?: s.boxes.filter { maskBox(it.address).equals(r, true) }.singleOrNull()
    }

    fun findSignup(s: State, box: Box, platformOrUrl: String): Signup? {
        val p = platformOrUrl.trim()
        if (p.isEmpty()) return null
        val h = hostOf(p)
        return s.signups.filter { it.boxId == box.id }.firstOrNull {
            it.id == p || it.platform.equals(p, true) || (h.isNotBlank() && it.host == h)
        }
    }

    // ── keep-alive ──

    /** When the next re-login / extend is due (0 = now). */
    fun nextExtendAt(b: Box): Long = when {
        !b.autoExtend || !b.source.extendable -> Long.MAX_VALUE
        b.expiresAt > 0 -> b.expiresAt - (if (b.source == Source.MOAKT) MOAKT_LEAD_MS else 3 * DAILY_MS)
        b.lastExtendAt == 0L -> 0L
        else -> b.lastExtendAt + DAILY_MS
    }

    fun extendDue(b: Box, now: Long): Boolean = b.autoExtend && b.source.extendable && now >= nextExtendAt(b)

    fun probeDue(b: Box, now: Long): Boolean =
        b.probe && b.source.readable && (b.lastProbeAt == 0L || now >= b.lastProbeAt + b.probeEveryH * 3600_000L)

    fun cycleProbeHours(h: Int): Int = PROBE_HOURS[(PROBE_HOURS.indexOf(h).coerceAtLeast(0) + 1) % PROBE_HOURS.size]

    /**
     * How often the keep-alive alarm should wake (null = nothing switched on, cancel it). A Moakt box
     * lives 1 h, so it needs the 15-minute floor AlarmManager allows; anything else is fine at 2 h.
     */
    fun alarmIntervalMs(s: State): Long? {
        val on = s.boxes.filter { (it.autoExtend && it.source.extendable) || (it.probe && it.source.readable) }
        if (on.isEmpty()) return null
        return if (on.any { it.source == Source.MOAKT && it.autoExtend }) 15 * 60_000L else 2 * 3600_000L
    }

    fun daysLeft(expiresAt: Long, now: Long): Int =
        if (expiresAt <= 0) -1 else ((expiresAt - now + 86_399_999L) / 86_400_000L).toInt().coerceAtLeast(0)

    /** 「有效期剩 26 天」 / 「有效期剩 42 分钟」 / 「已过期」 / "" when unknown. */
    fun expiryLine(expiresAt: Long, now: Long): String = when {
        expiresAt <= 0 -> ""
        expiresAt <= now -> "已过期"
        expiresAt - now < 3600_000L -> "有效期剩 ${((expiresAt - now) / 60_000L).coerceAtLeast(1)} 分钟"
        expiresAt - now < 86_400_000L -> "有效期剩 ${(expiresAt - now) / 3600_000L} 小时"
        else -> "有效期剩 ${daysLeft(expiresAt, now)} 天"
    }

    // ── times ──

    /** 「今天 10:42」 / 「昨天 21:06」 / 「10月20日」 / 「2025年10月20日」 */
    fun whenLabel(at: Long, now: Long, zone: ZoneId): String {
        if (at <= 0) return ""
        val t = Instant.ofEpochMilli(at).atZone(zone)
        val today: LocalDate = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val d = t.toLocalDate()
        val hm = t.format(DateTimeFormatter.ofPattern("HH:mm"))
        return when {
            d == today -> "今天 $hm"
            d == today.minusDays(1) -> "昨天 $hm"
            d.year == today.year -> "${d.monthValue}月${d.dayOfMonth}日"
            else -> "${d.year}年${d.monthValue}月${d.dayOfMonth}日"
        }
    }

    // ── mail parsing (ported from Mines MailParse / RegisterAutoCore) ──

    data class Mail(val id: String, val from: String, val subject: String, val text: String, val at: Long)

    private val KEYWORDS = Regex(
        "(验证码|校验码|动态码|確認碼|驗證碼|认证码|动态密码|安全码|登录码|激活码|verification|verify|security code|one[- ]time|passcode|sign[- ]in code|login code|confirmation code|\\bcode\\b|\\botp\\b|\\b2fa\\b)",
        RegexOption.IGNORE_CASE,
    )
    private val DIGITS = Regex("(?<![0-9A-Za-z])([0-9]{4,8})(?![0-9])")
    private val SPACED = Regex("(?<![0-9])([0-9]{3})[ \\-]([0-9]{3})(?![0-9])")

    /** The one-time code nearest a code keyword, or null. Years, amounts and times are skipped. */
    fun extractCode(text: String): String? {
        if (text.isBlank()) return null
        val t = text.replace('\u00a0', ' ')
        val kw = KEYWORDS.findAll(t).map { it.range.first }.toList()
        if (kw.isEmpty()) return null
        data class C(val v: String, val pos: Int)
        val cands = ArrayList<C>()
        DIGITS.findAll(t).forEach { m ->
            val v = m.groupValues[1]
            val before = t.getOrNull(m.range.first - 1)
            val after = t.getOrNull(m.range.last + 1)
            val year = v.length == 4 && (v.startsWith("19") || v.startsWith("20"))
            val nearKw = kw.any { k -> kotlin.math.abs(k - m.range.first) <= 40 }
            val skip = before == '¥' || before == '$' || before == '￥' || before == '#' ||
                after == '年' || after == '元' || after == '%' ||
                (after == '.' && t.getOrNull(m.range.last + 2)?.isDigit() == true) ||
                before == '.' || (before == ':' && t.getOrNull(m.range.first - 2)?.isDigit() == true) ||
                (year && !nearKw)
            if (!skip) cands += C(v, m.range.first)
        }
        SPACED.findAll(t).forEach { m -> cands += C(m.groupValues[1] + m.groupValues[2], m.range.first) }
        if (cands.isEmpty()) return null
        val best = cands.minByOrNull { c -> kw.minOf { k -> val d = c.pos - k; if (d >= 0) d else -d * 2 } } ?: return null
        val dist = kw.minOf { k -> kotlin.math.abs(best.pos - k) }
        return if (dist <= 300) best.v else null
    }

    private val LINK_RE = Regex("""https?://[^\s"'<>()\]]+""")
    private val VERIFY_WORDS = Regex("(?i)(verif|confirm|activat|validat|magic|token=|/auth/|signup|register|email-?link|code=|激活|验证)")
    private val NOT_VERIFY = Regex("(?i)(unsubscribe|privacy|terms|/help|support|facebook\\.com|twitter\\.com|x\\.com/|linkedin|instagram|\\.png|\\.jpg|\\.gif)")

    /** The confirmation link in a mail (same site as [host] when possible); null = none. */
    fun pickLink(text: String, host: String): String? {
        val links = LINK_RE.findAll(text).map { it.value.trimEnd('.', ',', ';').replace("&amp;", "&") }
            .distinct().filter { !NOT_VERIFY.containsMatchIn(it) }.toList()
        val verify = links.filter { VERIFY_WORDS.containsMatchIn(it) }
        val base = host.split('.').takeLast(2).joinToString(".")
        return verify.firstOrNull { base.isNotBlank() && hostOf(it).endsWith(base) } ?: verify.firstOrNull()
    }

    /** Codes and links found in [mails], newest first; [sender] filters by sender / subject text. */
    fun codesFrom(boxId: String, mails: List<Mail>, sender: String? = null, host: String = ""): List<Received> {
        val f = sender?.trim()?.lowercase().orEmpty()
        return mails.filter { m ->
            f.isEmpty() || m.from.lowercase().contains(f) || m.subject.lowercase().contains(f)
        }.sortedByDescending { it.at }.mapNotNull { m ->
            val body = m.subject + "\n" + m.text
            extractCode(body)?.let { Received(boxId, Kind.CODE, it, m.from, m.subject, m.at) }
                ?: pickLink(m.text, host)?.let { Received(boxId, Kind.LINK, it, m.from, m.subject, m.at) }
        }
    }

    /** Plain text of an HTML body, keeping every href so links can still be found. */
    fun htmlText(html: String): String {
        val hrefs = Regex("""href\s*=\s*["']([^"']+)["']""", RegexOption.IGNORE_CASE).findAll(html)
            .map { it.groupValues[1].replace("&amp;", "&") }.joinToString("\n")
        val text = html.replace(Regex("(?is)<(script|style)[^>]*>.*?</\\1>"), " ")
            .replace(Regex("(?i)<br\\s*/?>|</p>|</div>|</tr>"), "\n")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ").replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
            .replace(Regex("[ \\t]+"), " ").replace(Regex("\\n\\s*\\n+"), "\n").trim()
        return listOf(text, hrefs).filter { it.isNotBlank() }.joinToString("\n")
    }

    // ── fill placeholders (the agent types these; the app swaps in the real value) ──

    private val FILL_RE = Regex("""\{\{ledger_(email|pw):([a-z0-9_]{3,40})\}\}""")

    fun emailToken(boxId: String) = "{{ledger_email:$boxId}}"
    fun passwordToken(signupId: String) = "{{ledger_pw:$signupId}}"

    fun hasFill(text: String?): Boolean = text != null && FILL_RE.containsMatchIn(text)

    /**
     * Replaces every placeholder in [text]. [email] maps a box id to its address, [password] a signup id
     * to its password. Returns null when any placeholder is unknown, so nothing half-filled is typed.
     */
    fun resolveFill(text: String, email: (String) -> String?, password: (String) -> String?): String? {
        var missing = false
        val out = FILL_RE.replace(text) { m ->
            val v = if (m.groupValues[1] == "email") email(m.groupValues[2]) else password(m.groupValues[2])
            if (v == null) { missing = true; "" } else v
        }
        return if (missing) null else out
    }

    // ── JSON (no secrets in here: passwords / tokens / cookies are in the encrypted store) ──

    fun toJson(s: State): String = JSONObject()
        .put("v", 1)
        .put("boxes", JSONArray().apply {
            s.boxes.forEach { b ->
                put(JSONObject().put("id", b.id).put("a", b.address).put("src", b.source.id).put("c", b.createdAt)
                    .put("x", b.expiresAt).put("ae", b.autoExtend).put("pr", b.probe).put("ph", b.probeEveryH)
                    .put("le", b.lastExtendAt).put("lp", b.lastProbeAt).put("lo", b.lastProbeOk).put("ln", b.lastNote)
                    .put("lm", b.lastMailAt))
            }
        })
        .put("signups", JSONArray().apply {
            s.signups.forEach { r ->
                put(JSONObject().put("id", r.id).put("b", r.boxId).put("p", r.platform).put("u", r.url)
                    .put("s", r.status.name).put("c", r.createdAt).put("t", r.updatedAt).put("n", r.note).put("pw", r.hasPassword))
            }
        })
        .put("received", JSONArray().apply {
            s.received.forEach { r ->
                put(JSONObject().put("b", r.boxId).put("k", r.kind.name).put("v", r.value).put("f", r.from)
                    .put("s", r.subject).put("at", r.at).put("u", r.used))
            }
        })
        .toString()

    fun fromJson(json: String?): State {
        if (json.isNullOrBlank()) return State()
        return runCatching {
            val o = JSONObject(json)
            val boxes = o.optJSONArray("boxes").objects().map { b ->
                Box(
                    id = b.getString("id"), address = b.getString("a"), source = Source.of(b.optString("src")),
                    createdAt = b.optLong("c"), expiresAt = b.optLong("x"), autoExtend = b.optBoolean("ae", true),
                    probe = b.optBoolean("pr"), probeEveryH = b.optInt("ph", 6).let { if (it in PROBE_HOURS) it else 6 },
                    lastExtendAt = b.optLong("le"), lastProbeAt = b.optLong("lp"), lastProbeOk = b.optBoolean("lo", true),
                    lastNote = b.optString("ln"), lastMailAt = b.optLong("lm"),
                )
            }
            val signups = o.optJSONArray("signups").objects().map { r ->
                Signup(
                    id = r.getString("id"), boxId = r.getString("b"), platform = r.optString("p"), url = r.optString("u"),
                    status = PStatus.of(r.optString("s")), createdAt = r.optLong("c"), updatedAt = r.optLong("t"),
                    note = r.optString("n"), hasPassword = r.optBoolean("pw"),
                )
            }
            val received = o.optJSONArray("received").objects().map { r ->
                Received(
                    boxId = r.getString("b"), kind = if (r.optString("k") == "LINK") Kind.LINK else Kind.CODE,
                    value = r.optString("v"), from = r.optString("f"), subject = r.optString("s"), at = r.optLong("at"),
                    used = r.optBoolean("u"),
                )
            }
            State(boxes, signups, received)
        }.getOrDefault(State())
    }

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).mapNotNull { optJSONObject(it) }
}
