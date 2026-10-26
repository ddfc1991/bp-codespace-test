package com.openminis.app.hassn.ledger

import android.content.Context
import com.openminis.app.data.model.AgentToolDefinition
import com.openminis.app.data.model.AgentToolParam
import com.openminis.app.hassn.ledger.LedgerCore.PStatus
import com.openminis.app.hassn.ledger.LedgerCore.Source
import com.openminis.app.tools.ToolExecutionResult
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

/**
 * [Hassn-P1 临时邮箱台账] Agent tools over the ledger (Mines' accounts register_pick / register_done,
 * reworked for temp mailboxes). They need no credentials from the user and never touch the
 * user's own accounts or vault logins.
 *
 * Secrets never reach the model: tools return masked addresses plus fill placeholders
 * (`{{ledger_email:<id>}}`, `{{ledger_pw:<id>}}`). The agent types a placeholder with
 * browser_use action=type; [resolveBrowserArgs] swaps in the real value just before the page is
 * touched, so the plain password is never in chat, tool output or logs.
 */
object LedgerTools {
    const val NEW_MAILBOX = "ledger_new_mailbox"
    const val RECORD_SIGNUP = "ledger_record_signup"
    const val WAIT_CODE = "ledger_wait_code"
    const val MARK = "ledger_mark"
    const val LIST = "ledger_list"
    val NAMES: Set<String> = setOf(NEW_MAILBOX, RECORD_SIGNUP, WAIT_CODE, MARK, LIST)

    /** The system-prompt rule (user requirement 2026-10-26). */
    const val PROMPT_RULE = "用临时邮箱注册时必须使用台账工具，不要用用户自己的账号。" +
        " Temp-mailbox sign-ups: ledger_new_mailbox → ledger_record_signup → type the returned {{ledger_email:…}} / {{ledger_pw:…}} placeholders with browser_use type (never ask for or print the real values) → ledger_wait_code → ledger_mark. " +
        "Captcha, phone number or payment: stop, mark needs_user and hand the page to the user; never try to bypass them."

    private fun title(desc: String) = AgentToolParam("string", "A concise 5-10 word summary of what this tool call does, shown to the user (e.g. '$desc'). Use the same language as the user.")
    private val mailboxParam = AgentToolParam("string", "The mailbox_id returned by ledger_new_mailbox / ledger_list (the masked address also works when unique).")

    fun definitions(): List<AgentToolDefinition> = listOf(
        AgentToolDefinition(
            name = NEW_MAILBOX,
            description = "临时邮箱台账: create a new mailbox for signing up somewhere. Use this (never the user's own e-mail) whenever the user asks to register with a temporary mailbox. " +
                "Returns mailbox_id, the masked address and email_fill — a placeholder to type into the sign-up form with browser_use type. " +
                "Sources: mailtm (Mail.tm, default), moakt (Moakt, 1 h life, auto-extended; domains: ${LedgerCore.MOAKT_DOMAINS.joinToString(" ")}), gmail_alias (+tag on the user's Gmail; codes must be read by the user), shared_gmail (not available yet).",
            parameters = mapOf(
                "tool_title" to title("Create temp mailbox"),
                "source" to AgentToolParam("string", "Where the mailbox comes from (default mailtm).", enumValues = Source.entries.map { it.id }),
                "domain" to AgentToolParam("string", "Optional domain (moakt: one of its domains; mailtm: a live Mail.tm domain). For gmail_alias this is the +tag."),
            ),
            required = listOf("tool_title"),
            propertyOrdering = listOf("tool_title", "source", "domain"),
        ),
        AgentToolDefinition(
            name = RECORD_SIGNUP,
            description = "临时邮箱台账: record that you are signing up on a platform with a ledger mailbox, before you submit the form. Generates a strong password (16+ chars) and stores it only in the app's encrypted storage. " +
                "Returns signup_id, email_fill and password_fill placeholders. Type them exactly as given with browser_use action=type (text = the placeholder); the app fills the real value. Never ask the user for a password and never try to read the real one.",
            parameters = mapOf(
                "tool_title" to title("Record sign-up in ledger"),
                "mailbox" to mailboxParam,
                "platform" to AgentToolParam("string", "Platform name, e.g. OpenRouter."),
                "url" to AgentToolParam("string", "Sign-up page URL (or the site's domain)."),
            ),
            required = listOf("tool_title", "mailbox", "url"),
            propertyOrdering = listOf("tool_title", "mailbox", "platform", "url"),
        ),
        AgentToolDefinition(
            name = WAIT_CODE,
            description = "临时邮箱台账: wait for a verification code or link to arrive in a ledger mailbox (polls every 5 s). Returns the newest code / verification link that arrived after the call started (or the newest one in the box when nothing new comes before the timeout).",
            parameters = mapOf(
                "tool_title" to title("Wait for verification code"),
                "mailbox" to mailboxParam,
                "sender" to AgentToolParam("string", "Optional: only mail whose sender or subject contains this (e.g. openrouter)."),
                "timeout" to AgentToolParam("integer", "Seconds to wait (default 90, max 240)."),
            ),
            required = listOf("tool_title", "mailbox"),
            propertyOrdering = listOf("tool_title", "mailbox", "sender", "timeout"),
        ),
        AgentToolDefinition(
            name = MARK,
            description = "临时邮箱台账: set the result of a sign-up. verified = account created and e-mail confirmed; pending = not finished; needs_user = stopped at a captcha / phone / payment step the user must do; failed = gave up.",
            parameters = mapOf(
                "tool_title" to title("Update sign-up status"),
                "mailbox" to mailboxParam,
                "platform" to AgentToolParam("string", "The platform name, URL or signup_id used in ledger_record_signup."),
                "status" to AgentToolParam("string", "New status.", enumValues = listOf("verified", "pending", "needs_user", "failed")),
                "note" to AgentToolParam("string", "Optional few words (e.g. 卡在手机号)."),
            ),
            required = listOf("tool_title", "mailbox", "platform", "status"),
            propertyOrdering = listOf("tool_title", "mailbox", "platform", "status", "note"),
        ),
        AgentToolDefinition(
            name = LIST,
            description = "临时邮箱台账: list the ledger mailboxes (masked) with the platforms registered on each and their status.",
            parameters = mapOf("tool_title" to title("List temp mailboxes")),
            required = listOf("tool_title"),
            propertyOrdering = listOf("tool_title"),
        ),
    )

    private fun ok(title: String, o: JSONObject) = ToolExecutionResult(o.toString(2), true, toolTitle = title)
    private fun fail(title: String, msg: String) = ToolExecutionResult(msg, false, toolTitle = title)

    suspend fun execute(name: String, argsJson: String, c: Context): ToolExecutionResult {
        val a = runCatching { JSONObject(argsJson) }.getOrDefault(JSONObject())
        val title = a.optString("tool_title").ifBlank { name }
        LedgerStore.init(c)
        val s = LedgerStore.state.value
        return when (name) {
            NEW_MAILBOX -> {
                val src = Source.of(a.optString("source").ifBlank { "mailtm" })
                val dom = a.optString("domain").trim().removePrefix("@").ifBlank { null }
                LedgerStore.newBox(c, src, if (src == Source.GMAIL_ALIAS) null else dom, if (src == Source.GMAIL_ALIAS) dom else null).fold(
                    { b ->
                        ok(title, JSONObject()
                            .put("mailbox_id", b.id)
                            .put("address_masked", LedgerCore.maskBox(b.address))
                            .put("email_fill", LedgerCore.emailToken(b.id))
                            .put("source", b.source.label)
                            .put("expires", LedgerCore.expiryLine(b.expiresAt, System.currentTimeMillis()).ifBlank { "unknown / kept alive" })
                            .put("next", "Call $RECORD_SIGNUP with this mailbox_id before filling the form."))
                    },
                    { e -> fail(title, "Could not create the mailbox: ${e.message ?: e.javaClass.simpleName}") },
                )
            }
            RECORD_SIGNUP -> {
                val b = LedgerCore.findBox(s, a.optString("mailbox")) ?: return fail(title, "No such ledger mailbox. Use $LIST or $NEW_MAILBOX.")
                val url = a.optString("url").trim()
                if (url.isBlank()) return fail(title, "url is required")
                val r = LedgerStore.recordSignup(c, b.id, a.optString("platform"), url)
                ok(title, JSONObject()
                    .put("signup_id", r.id)
                    .put("mailbox_id", b.id)
                    .put("address_masked", LedgerCore.maskBox(b.address))
                    .put("platform", LedgerCore.platformTitle(r.platform, r.url))
                    .put("email_fill", LedgerCore.emailToken(b.id))
                    .put("password_fill", LedgerCore.passwordToken(r.id))
                    .put("how", "browser_use action=type text=<placeholder> for the e-mail / password / confirm-password fields. The real values are filled by the app and are never shown to you.")
                    .put("codes", if (b.source.readable) "Use $WAIT_CODE after submitting." else "This mailbox can't be read by the app: ask the user for the code."))
            }
            WAIT_CODE -> {
                val b = LedgerCore.findBox(s, a.optString("mailbox")) ?: return fail(title, "No such ledger mailbox. Use $LIST.")
                if (!b.source.readable) return fail(title, "${b.source.label} can't be read automatically yet. Ask the user to read the code and tell you.")
                val timeoutS = a.optInt("timeout", 90).coerceIn(5, 240)
                val sender = a.optString("sender").trim().ifBlank { null }
                val host = LedgerCore.signupsOf(s, b.id).firstOrNull()?.host.orEmpty()
                val start = System.currentTimeMillis()
                val deadline = start + timeoutS * 1000L
                var last: List<LedgerCore.Received> = emptyList()
                var lastErr: String? = null
                while (true) {
                    val r = LedgerStore.refreshCodes(c, b.id, sender, host)
                    r.onSuccess { last = it; lastErr = null }.onFailure { lastErr = it.message }
                    // a little slack for clock skew between the mail server and the phone
                    val fresh = last.firstOrNull { it.at == 0L || it.at >= start - 120_000L }
                    if (fresh != null) return ok(title, codeJson(fresh, true))
                    if (System.currentTimeMillis() + 5_000L > deadline) break
                    delay(5_000L)
                }
                val newest = last.firstOrNull()
                when {
                    newest != null -> ok(title, codeJson(newest, false))
                    lastErr != null -> fail(title, "Could not read the mailbox: $lastErr")
                    else -> fail(title, "No code or verification link arrived within ${timeoutS}s. Check the form was submitted, then call again or try resend.")
                }
            }
            MARK -> {
                val b = LedgerCore.findBox(s, a.optString("mailbox")) ?: return fail(title, "No such ledger mailbox. Use $LIST.")
                val r = LedgerCore.findSignup(s, b, a.optString("platform"))
                    ?: return fail(title, "No sign-up for that platform on this mailbox. Call $RECORD_SIGNUP first.")
                val st = PStatus.of(a.optString("status"))
                LedgerStore.mark(c, r.id, st, a.optString("note"))
                ok(title, JSONObject().put("signup_id", r.id).put("platform", LedgerCore.platformTitle(r.platform, r.url)).put("status", st.zh))
            }
            LIST -> {
                val now = System.currentTimeMillis()
                val arr = JSONArray()
                LedgerCore.rows(s).forEach { row ->
                    val b = row.box
                    arr.put(JSONObject()
                        .put("mailbox_id", b.id)
                        .put("address_masked", LedgerCore.maskBox(b.address))
                        .put("source", b.source.label)
                        .put("expires", LedgerCore.expiryLine(b.expiresAt, now))
                        .put("platforms", JSONArray().apply {
                            LedgerCore.signupsOf(s, b.id).forEach { su ->
                                put(JSONObject().put("signup_id", su.id).put("platform", LedgerCore.platformTitle(su.platform, su.url))
                                    .put("url", su.url).put("status", su.status.zh))
                            }
                        }))
                }
                val (nb, np) = LedgerCore.totals(s)
                ok(title, JSONObject().put("mailboxes", nb).put("platforms", np).put("items", arr))
            }
            else -> fail(title, "Unknown ledger tool: $name")
        }
    }

    private fun codeJson(r: LedgerCore.Received, fresh: Boolean): JSONObject = JSONObject()
        .put(if (r.kind == LedgerCore.Kind.CODE) "code" else "link", r.value)
        .put("from", r.from)
        .put("subject", r.subject)
        .put("fresh", fresh)
        .apply { if (!fresh) put("note", "Nothing new arrived; this is the newest one already in the box.") }

    /**
     * browser_use args with ledger placeholders in `text` swapped for the real values.
     * Returns [argsJson] unchanged when there is nothing to fill, null when a placeholder is unknown
     * (the caller fails the call instead of typing a half-filled value).
     */
    fun resolveBrowserArgs(c: Context, argsJson: String): String? {
        if (!LedgerCore.hasFill(argsJson)) return argsJson
        val o = runCatching { JSONObject(argsJson) }.getOrNull() ?: return null
        val text = o.optString("text")
        if (!LedgerCore.hasFill(text)) return argsJson
        LedgerStore.init(c)
        val filled = LedgerCore.resolveFill(
            text,
            email = { id -> LedgerStore.addressOf(id) },
            password = { id -> LedgerStore.passwordOf(c, id) },
        ) ?: return null
        return o.put("text", filled).toString()
    }
}
