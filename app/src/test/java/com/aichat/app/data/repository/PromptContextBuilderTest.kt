package com.aichat.app.data.repository

import com.aichat.app.data.db.MessageEntity
import com.aichat.app.data.model.MessageRole
import com.aichat.app.data.model.MessageStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class PromptContextBuilderTest {
    @Test
    fun selectMessages_keepsLatestMessagesByLimit() {
        val messages = (1..5).map { index ->
            message(
                id = index.toLong(),
                role = if (index % 2 == 0) MessageRole.ASSISTANT else MessageRole.USER,
                content = "message $index",
                createdAt = index.toLong(),
            )
        }

        val selected = PromptContextBuilder.selectMessages(messages, limit = 3)

        assertEquals(listOf("message 3", "message 4", "message 5"), selected.map { it.content })
    }

    @Test
    fun selectMessages_skipsBlankAndErrorMessages() {
        val messages = listOf(
            message(id = 1, role = MessageRole.USER, content = "hello", createdAt = 1),
            message(id = 2, role = MessageRole.ASSISTANT, content = "", createdAt = 2),
            message(id = 3, role = MessageRole.ASSISTANT, content = "failed", createdAt = 3, status = MessageStatus.ERROR),
            message(id = 4, role = MessageRole.USER, content = "again", createdAt = 4),
        )

        val selected = PromptContextBuilder.selectMessages(messages, limit = 20)

        assertEquals(listOf("hello", "again"), selected.map { it.content })
    }

    private fun message(
        id: Long,
        role: MessageRole,
        content: String,
        createdAt: Long,
        status: MessageStatus = MessageStatus.COMPLETE,
    ): MessageEntity = MessageEntity(
        id = id,
        conversationId = 1,
        role = role.name,
        content = content,
        status = status.name,
        createdAt = createdAt,
    )
}
