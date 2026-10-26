package com.openminis.app.hassn.ledger

import com.openminis.app.hassn.ledger.LedgerCore.Box
import com.openminis.app.hassn.ledger.LedgerCore.PStatus
import com.openminis.app.hassn.ledger.LedgerCore.Signup
import com.openminis.app.hassn.ledger.LedgerCore.Source
import com.openminis.app.hassn.ledger.LedgerCore.State
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Random

class LedgerCoreTest {

    private fun box(id: String, addr: String, src: Source = Source.MAILTM, created: Long = 1_000L, lastMail: Long = 0L) =
        Box(id = id, address = addr, source = src, createdAt = created, lastMailAt = lastMail)

    private fun su(id: String, boxId: String, name: String, url: String, at: Long, st: PStatus = PStatus.PENDING) =
        Signup(id = id, boxId = boxId, platform = name, url = url, status = st, createdAt = at, updatedAt = at)

    // ── masking ──

    @Test fun maskLongTempBoxKeepsBothEnds() {
        assertEquals("hsnq••e5r1@tmpbox.net", LedgerCore.maskBox("hsnq7w3e5r1@tmpbox.net"))
    }

    @Test fun twoSimilarBoxesNeverMaskTheSame() {
        assertNotEquals(LedgerCore.maskBox("hsnab12xy34@teml.net"), LedgerCore.maskBox("hsnab12zz99@teml.net"))
    }

    @Test fun maskShortAndPlusAlias() {
        assertEquals("ab••ef@x.io", LedgerCore.maskBox("abcdef@x.io"))
        assertEquals("a••@x.io", LedgerCore.maskBox("abc@x.io"))
        assertEquals("deningyang••+ai@gmail.com", LedgerCore.maskBox("deningyang740+ai@gmail.com"))
        assertEquals("not-an-address", LedgerCore.maskBox("not-an-address"))
    }

    @Test fun maskNeverShowsFullLocalPart() {
        val a = "hsnq7w3e5r1@tmpbox.net"
        assertFalse(LedgerCore.maskBox(a).contains("hsnq7w3e5r1"))
    }

    // ── password ──

    @Test fun passwordIsStrongAndLongEnough() {
        val r = SecureRandom()
        repeat(200) {
            val p = LedgerCore.generatePassword(r)
            assertTrue(p, p.length >= 16)
            assertTrue(p, LedgerCore.isStrong(p))
            assertFalse(p, p.any { it in "0O1lI" })
        }
    }

    @Test fun passwordLengthIsClampedTo16() {
        assertEquals(16, LedgerCore.generatePassword(Random(1), 8).length)
        assertEquals(24, LedgerCore.generatePassword(Random(1), 24).length)
    }

    @Test fun passwordsDiffer() {
        val r = SecureRandom()
        val set = (1..50).map { LedgerCore.generatePassword(r) }.toSet()
        assertEquals(50, set.size)
    }

    @Test fun gmailAlias() {
        assertEquals("deningyang740+ai@gmail.com", LedgerCore.gmailAlias("DeningYang740@gmail.com", "AI"))
        assertNull(LedgerCore.gmailAlias("someone@163.com", "ai"))
        assertNull(LedgerCore.gmailAlias("a@gmail.com", "!!"))
    }

    // ── grouping / counts ──

    private val state = State(
        boxes = listOf(
            box("b1", "hsnq7w3e5r1@tmpbox.net", Source.MOAKT, created = 1_000L),
            box("b2", "hsnab12xy34@indigobook.com", Source.MAILTM, created = 2_000L, lastMail = 9_000L),
            box("b3", "deningyang740+dev@gmail.com", Source.GMAIL_ALIAS, created = 3_000L),
        ),
        signups = listOf(
            su("s1", "b1", "OpenRouter", "https://openrouter.ai/signup", 4_000L, PStatus.VERIFIED),
            su("s2", "b1", "Groq", "https://console.groq.com", 5_000L),
            su("s3", "b2", "", "https://dash.cloudflare.com/sign-up", 6_000L),
            su("orphan", "gone", "X", "https://x.example", 7_000L),
        ),
    )

    @Test fun rowsAreMailboxesNewestFirstWithCounts() {
        val rows = LedgerCore.rows(state)
        assertEquals(listOf("b2", "b1", "b3"), rows.map { it.box.id })
        assertEquals(1, rows[0].platforms)
        assertEquals(2, rows[1].platforms)
        assertEquals(0, rows[2].platforms)
        assertEquals(9_000L, rows[0].lastAt)
        assertEquals(5_000L, rows[1].lastAt)
    }

    @Test fun totalsIgnoreOrphanSignups() {
        assertEquals(3 to 3, LedgerCore.totals(state))
    }

    @Test fun searchByPlatformHostOrAddress() {
        assertEquals(listOf("b1"), LedgerCore.rows(state, "groq").map { it.box.id })
        assertEquals(listOf("b2"), LedgerCore.rows(state, "cloudflare").map { it.box.id })
        assertEquals(listOf("b3"), LedgerCore.rows(state, "+dev").map { it.box.id })
        assertTrue(LedgerCore.rows(state, "nothing-like-this").isEmpty())
    }

    @Test fun upsertSamesiteUpdatesInsteadOfDuplicating() {
        val l = LedgerCore.upsertSignup(state.signups, su("new", "b1", "", "https://www.openrouter.ai/x", 8_000L, PStatus.PENDING))
        val s1 = l.filter { it.boxId == "b1" && it.host == "openrouter.ai" }
        assertEquals(1, s1.size)
        assertEquals("s1", s1[0].id)
        assertEquals("OpenRouter", s1[0].platform)
        assertEquals(4_000L, s1[0].createdAt)
    }

    @Test fun removeBoxDropsItsRows() {
        val s = LedgerCore.removeBox(state, "b1")
        assertEquals(2, s.boxes.size)
        assertTrue(s.signups.none { it.boxId == "b1" })
    }

    @Test fun findBoxByIdAddressOrMask() {
        assertEquals("b1", LedgerCore.findBox(state, "b1")?.id)
        assertEquals("b1", LedgerCore.findBox(state, "HSNQ7W3E5R1@tmpbox.net")?.id)
        assertEquals("b1", LedgerCore.findBox(state, "hsnq••e5r1@tmpbox.net")?.id)
        assertNull(LedgerCore.findBox(state, "nope"))
    }

    @Test fun platformTitleNeverRawIp() {
        assertEquals("OpenRouter", LedgerCore.platformTitle("OpenRouter", "https://openrouter.ai"))
        assertEquals("dash.cloudflare.com", LedgerCore.platformTitle("", "https://dash.cloudflare.com/sign-up"))
        assertEquals("自定义网站", LedgerCore.platformTitle("", "http://8.8.8.8:3000/"))
        assertEquals("自定义网站", LedgerCore.platformTitle("http://1.2.3.4", "http://1.2.3.4"))
    }

    // ── keep-alive ──

    @Test fun moaktExtendIsDueTwentyMinutesBeforeExpiry() {
        val now = 10_000_000L
        val b = box("m", "a@teml.net", Source.MOAKT).copy(expiresAt = now + 30 * 60_000L)
        assertFalse(LedgerCore.extendDue(b, now))
        assertTrue(LedgerCore.extendDue(b, now + 11 * 60_000L))
        assertFalse(LedgerCore.extendDue(b.copy(autoExtend = false), now + 60 * 60_000L))
    }

    @Test fun alarmIntervalFollowsSwitches() {
        val off = State(boxes = listOf(box("a", "x@y.z").copy(autoExtend = false)))
        assertNull(LedgerCore.alarmIntervalMs(off))
        assertEquals(2 * 3600_000L, LedgerCore.alarmIntervalMs(State(boxes = listOf(box("a", "x@y.z")))))
        assertEquals(15 * 60_000L, LedgerCore.alarmIntervalMs(State(boxes = listOf(box("m", "x@teml.net", Source.MOAKT)))))
        // gmail alias has nothing to keep alive
        assertNull(LedgerCore.alarmIntervalMs(State(boxes = listOf(box("g", "a+b@gmail.com", Source.GMAIL_ALIAS)))))
    }

    @Test fun expiryLine() {
        val now = 0L
        assertEquals("", LedgerCore.expiryLine(0L, now))
        assertEquals("有效期剩 42 分钟", LedgerCore.expiryLine(42 * 60_000L, now))
        assertEquals("有效期剩 26 天", LedgerCore.expiryLine(26 * 86_400_000L, now + 1))
        assertEquals("已过期", LedgerCore.expiryLine(5L, 10L))
    }

    @Test fun whenLabel() {
        val z = ZoneId.of("Asia/Shanghai")
        fun ms(s: String) = LocalDateTime.parse(s).atZone(z).toInstant().toEpochMilli()
        val now = ms("2026-10-26T14:20:00")
        assertEquals("今天 10:42", LedgerCore.whenLabel(ms("2026-10-26T10:42:00"), now, z))
        assertEquals("昨天 21:06", LedgerCore.whenLabel(ms("2026-10-25T21:06:00"), now, z))
        assertEquals("10月20日", LedgerCore.whenLabel(ms("2026-10-20T09:00:00"), now, z))
        assertEquals("2025年10月20日", LedgerCore.whenLabel(ms("2025-10-20T09:00:00"), now, z))
    }

    // ── codes ──

    @Test fun extractsCodes() {
        assertEquals("482913", LedgerCore.extractCode("Your verification code is 482913. It expires in 10 minutes."))
        assertEquals("482913", LedgerCore.extractCode("验证码：482 913"))
        assertNull(LedgerCore.extractCode("Order total ¥4829 on 2026"))
        assertNull(LedgerCore.extractCode("Hello there, welcome aboard"))
    }

    @Test fun picksVerifyLinkOverNoise() {
        val t = "Confirm: https://openrouter.ai/auth/verify?token=abc&amp;x=1 \n https://openrouter.ai/privacy https://twitter.com/x"
        assertEquals("https://openrouter.ai/auth/verify?token=abc&x=1", LedgerCore.pickLink(t, "openrouter.ai"))
    }

    @Test fun codesFromFiltersBySender() {
        val mails = listOf(
            LedgerCore.Mail("1", "noreply@groq.com", "Groq code", "Your code is 111222", 2_000L),
            LedgerCore.Mail("2", "hi@openrouter.ai", "Verify", "Click https://openrouter.ai/verify?token=z", 3_000L),
        )
        val all = LedgerCore.codesFrom("b", mails)
        assertEquals(2, all.size)
        assertEquals(LedgerCore.Kind.LINK, all[0].kind)
        val groq = LedgerCore.codesFrom("b", mails, sender = "groq")
        assertEquals(listOf("111222"), groq.map { it.value })
    }

    // ── fill placeholders ──

    @Test fun resolvesPlaceholdersAndRefusesUnknown() {
        val e = { id: String -> if (id == "b1abc") "real@box.io" else null }
        val p = { id: String -> if (id == "s9xyz") "S3cret!Pass-word" else null }
        assertEquals("real@box.io", LedgerCore.resolveFill(LedgerCore.emailToken("b1abc"), e, p))
        assertEquals("S3cret!Pass-word", LedgerCore.resolveFill(LedgerCore.passwordToken("s9xyz"), e, p))
        assertNull(LedgerCore.resolveFill(LedgerCore.passwordToken("snope1"), e, p))
        assertEquals("plain", LedgerCore.resolveFill("plain", e, p))
        assertTrue(LedgerCore.hasFill("x {{ledger_pw:s9xyz}} y"))
        assertFalse(LedgerCore.hasFill("{{secret:1}}"))
    }

    // ── json ──

    @Test fun jsonRoundTripHasNoSecrets() {
        val s = state.copy(received = listOf(LedgerCore.Received("b1", LedgerCore.Kind.CODE, "482913", "a@b", "s", 5L)))
        val j = LedgerCore.toJson(s)
        assertEquals(s, LedgerCore.fromJson(j))
        assertFalse(j.contains("password"))
        assertEquals(State(), LedgerCore.fromJson("not json"))
    }
}
