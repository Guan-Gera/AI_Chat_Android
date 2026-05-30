package com.aichat.app.network

import org.junit.Assert.assertEquals
import org.junit.Test

class EndpointBuilderTest {
    @Test
    fun openAiChatCompletions_appendsEndpointToBaseUrl() {
        assertEquals(
            "https://api.openai.com/v1/chat/completions",
            EndpointBuilder.openAiChatCompletions("https://api.openai.com/v1"),
        )
    }

    @Test
    fun openAiChatCompletions_keepsFullEndpoint() {
        assertEquals(
            "https://example.com/v1/chat/completions",
            EndpointBuilder.openAiChatCompletions("https://example.com/v1/chat/completions"),
        )
    }

    @Test
    fun geminiContent_buildsStreamingUrl() {
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:streamGenerateContent?key=abc123&alt=sse",
            EndpointBuilder.geminiContent(
                baseUrl = "https://generativelanguage.googleapis.com/v1beta",
                modelName = "gemini-2.5-pro",
                apiKey = "abc123",
                stream = true,
            ),
        )
    }
}
