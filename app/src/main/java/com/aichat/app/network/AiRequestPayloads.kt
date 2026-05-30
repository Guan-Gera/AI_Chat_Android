package com.aichat.app.network

import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.MessageRole
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object AiRequestPayloads {
    fun openAiChat(modelName: String, messages: List<ChatMessage>, stream: Boolean): String =
        buildJsonObject {
            put("model", modelName)
            put("stream", stream)
            put(
                "messages",
                buildJsonArray {
                    messages.forEach { message ->
                        add(
                            buildJsonObject {
                                put("role", message.role.wireValue)
                                put("content", message.content)
                            },
                        )
                    }
                },
            )
        }.toString()

    fun claudeMessages(modelName: String, messages: List<ChatMessage>, stream: Boolean): String {
        val systemText = messages
            .filter { it.role == MessageRole.SYSTEM }
            .joinToString("\n\n") { it.content }

        return buildJsonObject {
            put("model", modelName)
            put("max_tokens", 4096)
            put("stream", stream)
            if (systemText.isNotBlank()) put("system", systemText)
            put(
                "messages",
                buildJsonArray {
                    messages
                        .filter { it.role != MessageRole.SYSTEM }
                        .forEach { message ->
                            add(
                                buildJsonObject {
                                    put(
                                        "role",
                                        if (message.role == MessageRole.ASSISTANT) "assistant" else "user",
                                    )
                                    put("content", message.content)
                                },
                            )
                        }
                },
            )
        }.toString()
    }

    fun geminiContents(messages: List<ChatMessage>): String =
        buildJsonObject {
            put(
                "contents",
                buildJsonArray {
                    messages.filter { it.role != MessageRole.SYSTEM }.forEach { message ->
                        add(
                            buildJsonObject {
                                put("role", if (message.role == MessageRole.ASSISTANT) "model" else "user")
                                put(
                                    "parts",
                                    buildJsonArray {
                                        add(
                                            buildJsonObject {
                                                put("text", message.content)
                                            },
                                        )
                                    },
                                )
                            },
                        )
                    }
                },
            )
        }.toString()
}
