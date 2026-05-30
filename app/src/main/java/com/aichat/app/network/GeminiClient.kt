package com.aichat.app.network

import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.ModelConfigWithKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.job
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class GeminiClient(private val httpClient: OkHttpClient) : AiClient {
    override fun streamChat(
        model: ModelConfigWithKey,
        messages: List<ChatMessage>,
    ): Flow<AiStreamEvent> = flow {
        emit(AiStreamEvent.Started)
        val request = Request.Builder()
            .url(EndpointBuilder.geminiContent(model.baseUrl, model.modelName, model.apiKey, model.streamEnabled))
            .addHeader("Content-Type", JSON_MEDIA_TYPE)
            .post(AiRequestPayloads.geminiContents(messages).toRequestBody(JSON_MEDIA_TYPE.toMediaType()))
            .build()
        val call = httpClient.newCall(request)
        currentCoroutineContext().job.invokeOnCompletion { cause ->
            if (cause != null) call.cancel()
        }

        call.execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Gemini 请求失败：HTTP ${response.code} ${response.body.string().take(400)}")
            }
            if (!model.streamEnabled) {
                val text = parseText(response.body.string())
                if (text.isNotBlank()) emit(AiStreamEvent.Delta(text))
                emit(AiStreamEvent.Finished)
                return@flow
            }

            val source = response.body.source()
            while (!source.exhausted()) {
                val line = source.readUtf8Line() ?: continue
                if (!line.startsWith("data:")) continue
                val payload = line.removePrefix("data:").trim()
                if (payload.isBlank()) continue
                if (payload == "[DONE]") break
                val delta = parseText(payload)
                if (delta.isNotBlank()) emit(AiStreamEvent.Delta(delta))
            }
            emit(AiStreamEvent.Finished)
        }
    }.flowOn(Dispatchers.IO)

    private fun parseText(payload: String): String {
        val root = NetworkJson.parseToJsonElement(payload).jsonObject
        return root["candidates"]
            ?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?.get("content")
            ?.jsonObject
            ?.get("parts")
            ?.jsonArray
            ?.mapNotNull { part ->
                part.jsonObject["text"]?.jsonPrimitive?.contentOrNull
            }
            ?.joinToString("")
            .orEmpty()
    }

    private companion object {
        const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }
}
