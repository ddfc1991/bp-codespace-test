package com.openminis.app.hassn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HassnErrorTextTest {
    @Test
    fun `503 overloaded with retry`() {
        val f = HassnErrorText.friendly(
            "Transient error: [503] system cpu overloaded (current: 92.7%, threshold: 90%) — retrying (2/3)…",
        )
        assertEquals("服务繁忙，正在重试（2/3）…", f.headline)
        assertTrue(f.retrying)
    }

    @Test
    fun `429 and timeout`() {
        assertEquals("请求太频繁", HassnErrorText.friendly("HTTP 429 Too Many Requests").headline)
        assertEquals("请求超时，正在重试（1/3）…", HassnErrorText.friendly("Transient error: Read timed out — retrying (1/3)…").headline)
        assertEquals("密钥无效", HassnErrorText.friendly("[401] invalid api key").headline)
    }

    @Test
    fun `unknown stays raw`() {
        val f = HassnErrorText.friendly("Something odd")
        assertEquals("Something odd", f.headline)
        assertFalse(f.translated)
    }
}
