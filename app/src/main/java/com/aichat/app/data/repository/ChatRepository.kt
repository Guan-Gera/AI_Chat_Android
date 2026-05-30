package com.aichat.app.data.repository

import androidx.room.withTransaction
import com.aichat.app.data.db.AppDatabase
import com.aichat.app.data.db.ConversationEntity
import com.aichat.app.data.db.MessageEntity
import com.aichat.app.data.db.ModelConfigEntity
import com.aichat.app.data.model.ApiProtocol
import com.aichat.app.data.model.AppSettings
import com.aichat.app.data.model.ChatMessage
import com.aichat.app.data.model.MessageRole
import com.aichat.app.data.model.MessageStatus
import com.aichat.app.data.model.ModelConfigInput
import com.aichat.app.data.model.ModelConfigWithKey
import com.aichat.app.data.security.SecureApiKeyStore
import com.aichat.app.data.settings.AppSettingsStore
import com.aichat.app.network.AiService
import com.aichat.app.network.AiStreamEvent
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val database: AppDatabase,
    private val apiKeyStore: SecureApiKeyStore,
    private val settingsStore: AppSettingsStore,
    private val aiService: AiService,
) {
    private val dao = database.chatDao()

    val conversations: Flow<List<ConversationEntity>> = dao.observeConversations()
    val modelConfigs: Flow<List<ModelConfigEntity>> = dao.observeModelConfigs()
    val settings: Flow<AppSettings> = settingsStore.settingsFlow

    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>> =
        dao.observeMessages(conversationId)

    suspend fun createConversation(title: String, defaultModelId: String?): Long {
        val now = System.currentTimeMillis()
        return dao.insertConversation(
            ConversationEntity(
                title = title.ifBlank { "新对话" },
                createdAt = now,
                updatedAt = now,
                defaultModelId = defaultModelId,
            ),
        )
    }

    suspend fun renameConversation(conversationId: Long, title: String) {
        val conversation = dao.getConversation(conversationId) ?: return
        dao.updateConversation(
            conversation.copy(
                title = title.ifBlank { conversation.title },
                updatedAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteConversation(conversationId: Long) {
        database.withTransaction {
            dao.deleteMessagesForConversation(conversationId)
            dao.deleteConversation(conversationId)
        }
    }

    suspend fun clearHistory() {
        database.withTransaction {
            dao.deleteAllMessages()
            dao.deleteAllConversations()
        }
    }

    suspend fun insertUserMessage(conversationId: Long, content: String): Long =
        insertMessage(conversationId, MessageRole.USER, content, MessageStatus.COMPLETE)

    suspend fun insertAssistantPlaceholder(conversationId: Long): Long =
        insertMessage(conversationId, MessageRole.ASSISTANT, "", MessageStatus.STREAMING)

    suspend fun updateAssistantContent(messageId: Long, content: String, status: MessageStatus = MessageStatus.STREAMING) {
        val message = dao.getMessage(messageId) ?: return
        dao.updateMessage(
            message.copy(
                content = content,
                status = status.name,
                errorMessage = null,
            ),
        )
        touchConversation(message.conversationId)
    }

    suspend fun markAssistantError(messageId: Long, content: String, errorMessage: String) {
        val message = dao.getMessage(messageId) ?: return
        dao.updateMessage(
            message.copy(
                content = content,
                status = MessageStatus.ERROR.name,
                errorMessage = errorMessage,
            ),
        )
        touchConversation(message.conversationId)
    }

    suspend fun deleteMessage(messageId: Long) {
        val message = dao.getMessage(messageId) ?: return
        dao.deleteMessage(messageId)
        touchConversation(message.conversationId)
    }

    suspend fun deleteAssistantMessagesAfter(conversationId: Long, userMessageCreatedAt: Long) {
        dao.deleteMessagesAfter(conversationId, userMessageCreatedAt)
        touchConversation(conversationId)
    }

    suspend fun getMessagesForPrompt(conversationId: Long, limit: Int): List<ChatMessage> =
        PromptContextBuilder.selectMessages(dao.getMessages(conversationId), limit)

    suspend fun getLastUserMessage(conversationId: Long): MessageEntity? =
        dao.getMessages(conversationId)
            .lastOrNull { it.role == MessageRole.USER.name && it.content.isNotBlank() }

    fun streamChat(
        model: ModelConfigWithKey,
        messages: List<ChatMessage>,
    ): Flow<AiStreamEvent> = aiService.streamChat(model, messages)

    suspend fun saveModelConfig(input: ModelConfigInput): String {
        require(input.displayName.isNotBlank()) { "请填写模型显示名称" }
        require(input.baseUrl.isNotBlank()) { "请填写 Base URL" }
        require(input.modelName.isNotBlank()) { "请填写模型名称" }

        val now = System.currentTimeMillis()
        val id = input.id ?: UUID.randomUUID().toString()
        val existing = input.id?.let { dao.getModelConfig(it) }
        val apiKeyRef = existing?.apiKeyRef ?: UUID.randomUUID().toString()
        val apiKey = input.apiKey?.trim().orEmpty()
        if (existing == null && apiKey.isBlank()) {
            error("请填写 API Key")
        }
        if (apiKey.isNotBlank()) {
            apiKeyStore.put(apiKeyRef, apiKey)
        }

        database.withTransaction {
            if (input.isDefault) dao.clearDefaultModelFlags()
            dao.upsertModelConfig(
                ModelConfigEntity(
                    id = id,
                    displayName = input.displayName.trim(),
                    protocol = input.protocol.name,
                    baseUrl = input.baseUrl.trim().trimEnd('/'),
                    modelName = input.modelName.trim(),
                    streamEnabled = input.streamEnabled,
                    isDefault = input.isDefault,
                    apiKeyRef = apiKeyRef,
                    createdAt = existing?.createdAt ?: now,
                    updatedAt = now,
                ),
            )
        }
        if (input.isDefault) settingsStore.setDefaultModelId(id)
        return id
    }

    suspend fun deleteModelConfig(modelId: String) {
        val existing = dao.getModelConfig(modelId) ?: return
        dao.deleteModelConfig(modelId)
        apiKeyStore.delete(existing.apiKeyRef)
    }

    suspend fun getModelWithKey(modelId: String): ModelConfigWithKey? {
        val entity = dao.getModelConfig(modelId) ?: return null
        val apiKey = apiKeyStore.get(entity.apiKeyRef).orEmpty()
        if (apiKey.isBlank()) return null
        return ModelConfigWithKey(
            id = entity.id,
            displayName = entity.displayName,
            protocol = ApiProtocol.fromStored(entity.protocol),
            baseUrl = entity.baseUrl,
            modelName = entity.modelName,
            streamEnabled = entity.streamEnabled,
            isDefault = entity.isDefault,
            apiKey = apiKey,
        )
    }

    suspend fun setDefaultModel(modelId: String?) {
        database.withTransaction {
            dao.clearDefaultModelFlags()
            if (modelId != null) {
                val model = dao.getModelConfig(modelId)
                if (model != null) dao.upsertModelConfig(model.copy(isDefault = true))
            }
        }
        settingsStore.setDefaultModelId(modelId)
    }

    suspend fun setThemeMode(themeMode: com.aichat.app.data.model.ThemeMode) {
        settingsStore.setThemeMode(themeMode)
    }

    suspend fun setContextMessageLimit(limit: Int) {
        settingsStore.setContextMessageLimit(limit)
    }

    private suspend fun insertMessage(
        conversationId: Long,
        role: MessageRole,
        content: String,
        status: MessageStatus,
    ): Long {
        val now = System.currentTimeMillis()
        val id = dao.insertMessage(
            MessageEntity(
                conversationId = conversationId,
                role = role.name,
                content = content,
                status = status.name,
                createdAt = now,
            ),
        )
        touchConversation(conversationId)
        return id
    }

    private suspend fun touchConversation(conversationId: Long) {
        val conversation = dao.getConversation(conversationId) ?: return
        dao.updateConversation(conversation.copy(updatedAt = System.currentTimeMillis()))
    }
}
