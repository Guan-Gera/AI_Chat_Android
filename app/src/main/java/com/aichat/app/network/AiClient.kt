package com.aichat.app.network

import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.ModelConfigWithKey
import kotlinx.coroutines.flow.Flow

interface AiClient {
    fun streamChat(
        model: ModelConfigWithKey,
        messages: List<ChatMessage>,
    ): Flow<AiStreamEvent>
}
