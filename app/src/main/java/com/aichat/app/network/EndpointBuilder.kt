package com.aichat.app.network

import java.net.URLEncoder

object EndpointBuilder {
    fun openAiChatCompletions(baseUrl: String): String =
        appendEndpoint(baseUrl, "chat/completions")

    fun claudeMessages(baseUrl: String): String =
        appendEndpoint(baseUrl, "messages")

    fun geminiContent(baseUrl: String, modelName: String, apiKey: String, stream: Boolean): String {
        val cleanBase = baseUrl.trim().trimEnd('/')
        val method = if (stream) "streamGenerateContent" else "generateContent"
        val encodedModel = encode(modelName.trim())
        val encodedKey = encode(apiKey.trim())
        val alt = if (stream) "&alt=sse" else ""
        return "$cleanBase/models/$encodedModel:$method?key=$encodedKey$alt"
    }

    private fun appendEndpoint(baseUrl: String, endpoint: String): String {
        val cleanBase = baseUrl.trim().trimEnd('/')
        return if (cleanBase.endsWith(endpoint)) cleanBase else "$cleanBase/$endpoint"
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name())
}
