package com.openminis.app.hassn

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HassnGatewayTest {

    @Test
    fun `models url accepts base with or without v1`() {
        assertEquals("https://x.org/v1/models", HassnGateway.modelsUrl("https://x.org/v1"))
        assertEquals("https://x.org/v1/models", HassnGateway.modelsUrl("https://x.org/v1/"))
        assertEquals("https://x.org/v1/models", HassnGateway.modelsUrl("https://x.org"))
    }

    @Test
    fun `parses and groups by owned_by`() {
        val body = """{"object":"list","data":[
            {"id":"glm-5","owned_by":"zhipu"},
            {"id":"deepseek-v4","owned_by":"deepseek"},
            {"id":"GLM-4-flash","owned_by":"zhipu"},
            {"id":"","owned_by":"x"},
            {"id":"misc"}
        ]}"""
        val list = HassnGateway.parseModels(body)
        assertEquals(4, list.size)
        val groups = HassnGateway.group(list)
        assertEquals("zhipu", groups.first().name)
        assertEquals(listOf("GLM-4-flash", "glm-5"), groups.first().models.map { it.id })
        assertEquals("other", list.last().ownedBy)
    }

    @Test
    fun `error message from new-api body`() {
        assertEquals("Invalid token", HassnGateway.errorMessage("""{"error":{"message":"Invalid token","type":"new_api_error"}}"""))
        assertNull(HassnGateway.errorMessage("not json"))
    }

    @Test
    fun `mask never shows the whole key`() {
        assertEquals("sk-a…wxyz", HassnGateway.mask("sk-abcdefghijklmnopqrstuvwxyz"))
        assertEquals("•••", HassnGateway.mask("abc"))
    }
}
