package com.aichat.app.data.repository

import com.aichat.app.data.db.MessageEntity
import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.MessageRole
import com.aichat.app.data.model.MessageStatus

object PromptContextBuilder {
    fun selectMessages(
        messages: List<MessageEntity>,
        limit: Int,
    ): List<ChatMessage> {
        val eligible = messages
            .filter { it.content.isNotBlank() }
            .filter { it.status != MessageStatus.ERROR.name }
            .map {
                ChatMessage(
                    role = MessageRole.fromStored(it.role),
                    content = it.content,
                )
            }
        return if (limit <= 0) eligible else eligible.takeLast(limit)
    }
}
