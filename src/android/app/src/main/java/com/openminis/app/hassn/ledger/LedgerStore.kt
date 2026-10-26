package com.openminis.app.hassn.ledger

import android.content.Context
import android.content.SharedPreferences
import com.openminis.app.hassn.ledger.LedgerCore.Box
import com.openminis.app.hassn.ledger.LedgerCore.PStatus
import com.openminis.app.hassn.ledger.LedgerCore.Received
import com.openminis.app.hassn.ledger.LedgerCore.Signup
import com.openminis.app.hassn.ledger.LedgerCore.Source
import com.openminis.app.hassn.ledger.LedgerCore.State
import com.openminis.app.logging.AppLogger
import com.openminis.app.util.EncryptedPrefsFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.security.SecureRandom

/**
 * [Hassn-P1 临时邮箱台账] Android side of [LedgerCore]. Everything lives in the app's existing
 * encrypted store ([EncryptedPrefsFactory], Android Keystore AES-GCM), in its own file so it never
 * mixes with the user's own logins or provider keys:
 *  - `state`: boxes / signups / codes (JSON, no secrets)
 *  - `boxpw_<boxId>` / `boxtok_<boxId>`: the box's own password / token / session cookie
 *  - `pw_<signupId>`: the generated platform password
 *  - `gmail_base`: the user's Gmail for 别名
 * Log lines carry ids and masked addresses only, never a password, token or full code.
 */
object LedgerStore {
    private const val FILE = "hassn_ledger_secure"
    private const val KEY_STATE = "state"
    private const val KEY_GMAIL = "gmail_base"
    private const val TAG = "HassnLedger"

    fun boxPassKey(boxId: String) = "boxpw_$boxId"
    fun boxTokenKey(boxId: String) = "boxtok_$boxId"
    private fun pwKey(signupId: String) = "pw_$signupId"

    @Volatile private var prefs: SharedPreferences? = null
    private val rnd = SecureRandom()
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    fun init(c: Context) {
        if (prefs != null) return
        synchronized(this) {
            if (prefs != null) return
            val p = EncryptedPrefsFactory.safeCreate(c.applicationContext, FILE)
            _state.value = LedgerCore.fromJson(p.getString(KEY_STATE, null))
            prefs = p
        }
    }

    private fun p(c: Context): SharedPreferences { init(c); return prefs!! }

    fun secrets(c: Context): LedgerSecrets {
        val sp = p(c)
        return object : LedgerSecrets {
            override fun get(key: String): String? = sp.getString(key, null)
            override fun put(key: String, value: String?) {
                sp.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply()
            }
        }
    }

    @Synchronized
    private fun update(c: Context, f: (State) -> State): State {
        val sp = p(c)
        val n = f(_state.value)
        _state.value = n
        sp.edit().putString(KEY_STATE, LedgerCore.toJson(n)).apply()
        return n
    }

    private fun note(s: String) = AppLogger.info(TAG, s)

    // ── Gmail 别名 base ──

    fun gmailBase(c: Context): String? = p(c).getString(KEY_GMAIL, null)?.takeIf { it.isNotBlank() }
    fun setGmailBase(c: Context, v: String?) {
        p(c).edit().apply { if (v.isNullOrBlank()) remove(KEY_GMAIL) else putString(KEY_GMAIL, v.trim().lowercase()) }.apply()
    }

    // ── boxes ──

    fun box(boxId: String): Box? = _state.value.boxes.firstOrNull { it.id == boxId }

    /** Full address, only for typing into a page / copying to the clipboard. */
    fun addressOf(boxId: String): String? = box(boxId)?.address

    /**
     * New box from [source] (Gmail 别名 uses [domain] as the tag). Returns the box or a plain Chinese reason.
     * Network work runs on IO.
     */
    suspend fun newBox(c: Context, source: Source, domain: String?, tag: String? = null): Result<Box> = withContext(Dispatchers.IO) {
        runCatching {
            init(c)
            if (!source.ready) error("${source.label} 还没接入")
            val id = LedgerCore.newId(rnd, "b")
            val now = System.currentTimeMillis()
            val created: CreatedBox = if (source == Source.GMAIL_ALIAS) {
                val base = gmailBase(c) ?: error("先在台账里填你的 Gmail 地址")
                val t = tag?.takeIf { it.isNotBlank() } ?: LedgerCore.newId(rnd, "h").drop(1).take(5)
                val a = LedgerCore.gmailAlias(base, t) ?: error("Gmail 地址不对")
                if (_state.value.boxes.any { it.address.equals(a, true) }) error("这个别名已经在台账里")
                CreatedBox(a, 0L)
            } else {
                LedgerSources.of(source).create(id, domain, LedgerCore.newLocalPart(rnd), secrets(c)).getOrThrow()
            }
            val b = Box(id = id, address = created.address, source = source, createdAt = now, expiresAt = created.expiresAt,
                autoExtend = source.extendable, lastExtendAt = now)
            update(c) { s -> s.copy(boxes = s.boxes + b) }
            note("box created ${b.id} ${LedgerCore.maskBox(b.address)} ${source.id}")
            LedgerKeepAlive.schedule(c)
            b
        }
    }

    fun setKeepAlive(c: Context, boxId: String, autoExtend: Boolean? = null, probe: Boolean? = null, probeEveryH: Int? = null) {
        update(c) { s ->
            s.copy(boxes = s.boxes.map { b ->
                if (b.id != boxId) b else b.copy(
                    autoExtend = autoExtend ?: b.autoExtend,
                    probe = probe ?: b.probe,
                    probeEveryH = probeEveryH ?: b.probeEveryH,
                )
            })
        }
        LedgerKeepAlive.schedule(c)
    }

    /** Deletes the box, its rows and every secret that belonged to it. */
    fun deleteBox(c: Context, boxId: String) {
        val sp = p(c)
        val ids = _state.value.signups.filter { it.boxId == boxId }.map { it.id }
        sp.edit().apply {
            remove(boxPassKey(boxId)); remove(boxTokenKey(boxId))
            ids.forEach { remove(pwKey(it)) }
        }.apply()
        update(c) { s -> LedgerCore.removeBox(s, boxId) }
        note("box deleted $boxId")
        LedgerKeepAlive.schedule(c)
    }

    // ── signups ──

    /**
     * Records a platform for [boxId] and makes its password (once; an existing one is kept).
     * Returns the signup; the password itself is only reachable through [passwordOf].
     */
    fun recordSignup(c: Context, boxId: String, platform: String, url: String): Signup {
        init(c)
        val now = System.currentTimeMillis()
        val nu = LedgerCore.normalizeUrl(url)
        val existing = _state.value.signups.firstOrNull {
            it.boxId == boxId && LedgerCore.hostOf(nu).isNotBlank() && it.host == LedgerCore.hostOf(nu)
        }
        val id = existing?.id ?: LedgerCore.newId(rnd, "s")
        val sp = p(c)
        if (sp.getString(pwKey(id), null) == null) {
            sp.edit().putString(pwKey(id), LedgerCore.generatePassword(rnd, 18)).apply()
        }
        val r = Signup(id = id, boxId = boxId, platform = platform.trim(), url = nu, status = existing?.status ?: PStatus.PENDING,
            createdAt = existing?.createdAt ?: now, updatedAt = now, hasPassword = true)
        update(c) { s -> s.copy(signups = LedgerCore.upsertSignup(s.signups, r)) }
        note("signup recorded ${r.id} box=$boxId host=${r.host}")
        return _state.value.signups.first { it.id == id }
    }

    fun mark(c: Context, signupId: String, status: PStatus, why: String = "") {
        update(c) { s ->
            s.copy(signups = s.signups.map {
                if (it.id != signupId) it else it.copy(status = status, note = why.take(80).ifBlank { it.note }, updatedAt = System.currentTimeMillis())
            })
        }
        note("signup $signupId → ${status.name}")
    }

    fun deleteSignup(c: Context, signupId: String) {
        p(c).edit().remove(pwKey(signupId)).apply()
        update(c) { s -> s.copy(signups = s.signups.filterNot { it.id == signupId }) }
    }

    /** The plain password: only for the browser fill and the fingerprint-gated reveal / copy. */
    fun passwordOf(c: Context, signupId: String): String? = p(c).getString(pwKey(signupId), null)

    // ── codes ──

    /** 重新收码: reads the box now and stores what codes / links it holds. */
    suspend fun refreshCodes(c: Context, boxId: String, sender: String? = null, host: String = ""): Result<List<Received>> =
        withContext(Dispatchers.IO) {
            init(c)
            val b = box(boxId) ?: return@withContext Result.failure(IllegalStateException("台账里没有这个邮箱"))
            LedgerSources.of(b.source).inbox(b, secrets(c)).map { mails ->
                val found = LedgerCore.codesFrom(b.id, mails, sender, host)
                val newest = mails.maxOfOrNull { it.at } ?: 0L
                update(c) { s ->
                    s.copy(
                        received = LedgerCore.addReceived(s.received, LedgerCore.codesFrom(b.id, mails)),
                        boxes = s.boxes.map { if (it.id == b.id && newest > it.lastMailAt) it.copy(lastMailAt = newest) else it },
                    )
                }
                found
            }
        }

    fun markUsed(c: Context, r: Received) {
        update(c) { s -> s.copy(received = s.received.map { if (it == r) it.copy(used = true) else it }) }
    }

    // ── keep-alive ──

    /** Runs what is due (or everything switched on when [force]); returns one line per action. */
    suspend fun runDue(c: Context, force: Boolean = false): List<String> = withContext(Dispatchers.IO) {
        init(c)
        val out = ArrayList<String>()
        val now = System.currentTimeMillis()
        for (b in _state.value.boxes) {
            if (b.autoExtend && b.source.extendable && (force || LedgerCore.extendDue(b, now))) {
                val r = LedgerSources.of(b.source).extend(b, secrets(c))
                update(c) { s ->
                    s.copy(boxes = s.boxes.map {
                        if (it.id != b.id) it else r.fold(
                            { exp -> it.copy(lastExtendAt = now, expiresAt = if (exp > 0) exp else it.expiresAt, lastNote = "") },
                            { e -> it.copy(lastExtendAt = now, lastNote = e.message ?: "延时没成功") },
                        )
                    })
                }
                out += "extend ${b.id} " + (if (r.isSuccess) "ok" else "fail")
            }
            val b2 = box(b.id) ?: continue
            if (b2.probe && b2.source.readable && (force || LedgerCore.probeDue(b2, now))) {
                val r = refreshCodes(c, b2.id)
                update(c) { s ->
                    s.copy(boxes = s.boxes.map {
                        if (it.id != b2.id) it else it.copy(lastProbeAt = now, lastProbeOk = r.isSuccess,
                            lastNote = if (r.isSuccess) it.lastNote else (r.exceptionOrNull()?.message ?: "探活失败"))
                    })
                }
                out += "probe ${b2.id} " + (if (r.isSuccess) "ok" else "fail")
            }
        }
        out.forEach { note(it) }
        out
    }
}
