package com.aichat.app.network

import com.aichat.app.data.model.ApiProtocol
import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.ModelConfigWithKey
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AiService {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(0, TimeUnit.SECONDS)
        .build()

    private val openAiCompatibleClient = OpenAiCompatibleClient(httpClient)
    private val claudeClient = ClaudeClient(httpClient)
    private val geminiClient = GeminiClient(httpClient)

    fun streamChat(
        model: ModelConfigWithKey,
        messages: List<ChatMessage>,
    ): Flow<AiStreamEvent> {
        val client = when (model.protocol) {
            ApiProtocol.OPENAI_COMPATIBLE -> openAiCompatibleClient
            ApiProtocol.CLAUDE -> claudeClient
            ApiProtocol.GEMINI -> geminiClient
        }
        return client.streamChat(model, messages)
    }
}
