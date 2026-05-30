package com.aichat.app.network

import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.MessageRole
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiRequestPayloadsTest {
    private val json = Json

    @Test
    fun openAiChat_buildsChatCompletionsPayload() {
        val payload = json.parseToJsonElement(
            AiRequestPayloads.openAiChat(
                modelName = "deepseek-chat",
                messages = listOf(ChatMessage(MessageRole.USER, "你好")),
                stream = true,
            ),
        ).jsonObject

        assertEquals("deepseek-chat", payload["model"]?.jsonPrimitive?.content)
        assertTrue(payload["stream"]?.jsonPrimitive?.boolean ?: false)
        assertEquals("user", payload["messages"]?.jsonArray?.first()?.jsonObject?.get("role")?.jsonPrimitive?.content)
    }

    @Test
    fun claudeMessages_movesSystemMessageToSystemField() {
        val payload = json.parseToJsonElement(
            AiRequestPayloads.claudeMessages(
                modelName = "claude-3-5-sonnet-latest",
                messages = listOf(
                    ChatMessage(MessageRole.SYSTEM, "用中文回答"),
                    ChatMessage(MessageRole.USER, "你好"),
                ),
                stream = false,
            ),
        ).jsonObject

        assertEquals("用中文回答", payload["system"]?.jsonPrimitive?.content)
        assertEquals(4096, payload["max_tokens"]?.jsonPrimitive?.int)
        assertFalse(payload["stream"]?.jsonPrimitive?.boolean ?: true)
        assertEquals(1, payload["messages"]?.jsonArray?.size)
    }

    @Test
    fun geminiContents_mapsAssistantRoleToModel() {
        val payload = json.parseToJsonElement(
            AiRequestPayloads.geminiContents(
                listOf(
                    ChatMessage(MessageRole.USER, "你好"),
                    ChatMessage(MessageRole.ASSISTANT, "你好，有什么可以帮你？"),
                ),
            ),
        ).jsonObject

        val contents = payload["contents"]!!.jsonArray
        assertEquals("user", contents[0].jsonObject["role"]?.jsonPrimitive?.content)
        assertEquals("model", contents[1].jsonObject["role"]?.jsonPrimitive?.content)
    }
}
