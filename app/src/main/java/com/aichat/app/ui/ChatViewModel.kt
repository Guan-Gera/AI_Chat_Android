package com.aichat.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aichat.app.data.db.ConversationEntity
import com.aichat.app.data.db.MessageEntity
import com.aichat.app.data.db.ModelConfigEntity
import com.aichat.app.data.model.ApiProtocol
import com.aichat.app.data.model.AppSettings
import com.aichat.app.data.model.MessageStatus
import com.aichat.app.data.model.ModelConfigInput
import com.aichat.app.data.model.ModelPreset
import com.aichat.app.data.model.ThemeMode
import com.aichat.app.data.repository.ChatRepository
import com.aichat.app.network.AiStreamEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModel(
    private val repository: ChatRepository,
) : ViewModel() {
    private val currentConversationId = MutableStateFlow<Long?>(null)
    private val selectedModelId = MutableStateFlow<String?>(null)
    private val inputText = MutableStateFlow("")
    private val historyQuery = MutableStateFlow("")
    private val isSending = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private val activeScreen = MutableStateFlow(AppScreen.CHAT)
    private val modelEditor = MutableStateFlow<ModelEditorState?>(null)
    private val renameDialog = MutableStateFlow<RenameDialogState?>(null)
    private val confirmDialog = MutableStateFlow<ConfirmDialogState?>(null)

    private val conversations = repository.conversations
    private val modelConfigs = repository.modelConfigs
    private val settings = repository.settings
    private val messages = currentConversationId.flatMapLatest { conversationId ->
        if (conversationId == null) flowOf(emptyList()) else repository.observeMessages(conversationId)
    }

    private var cachedSettings = AppSettings()
    private var cachedModels: List<ModelConfigEntity> = emptyList()
    private var cachedConversations: List<ConversationEntity> = emptyList()
    private var didAutoSelectConversation = false
    private var sendJob: Job? = null

    @Suppress("UNCHECKED_CAST")
    val uiState = combine(
        conversations,
        messages,
        modelConfigs,
        settings,
        currentConversationId,
        selectedModelId,
        inputText,
        historyQuery,
        isSending,
        errorMessage,
        activeScreen,
        modelEditor,
        renameDialog,
        confirmDialog,
    ) { values ->
        ChatUiState(
            conversations = values[0] as List<ConversationEntity>,
            messages = values[1] as List<MessageEntity>,
            modelConfigs = values[2] as List<ModelConfigEntity>,
            settings = values[3] as AppSettings,
            currentConversationId = values[4] as Long?,
            selectedModelId = values[5] as String?,
            inputText = values[6] as String,
            historyQuery = values[7] as String,
            isSending = values[8] as Boolean,
            errorMessage = values[9] as String?,
            activeScreen = values[10] as AppScreen,
            modelEditor = values[11] as ModelEditorState?,
            renameDialog = values[12] as RenameDialogState?,
            confirmDialog = values[13] as ConfirmDialogState?,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatUiState())

    init {
        viewModelScope.launch {
            repository.settings.collect { cachedSettings = it }
        }
        viewModelScope.launch {
            repository.modelConfigs.collect { models ->
                cachedModels = models
                val current = selectedModelId.value
                if (current == null || models.none { it.id == current }) {
                    selectedModelId.value = chooseDefaultModelId(models, cachedSettings)
                }
            }
        }
        viewModelScope.launch {
            repository.conversations.collect { list ->
                cachedConversations = list
                if (!didAutoSelectConversation && currentConversationId.value == null && list.isNotEmpty()) {
                    didAutoSelectConversation = true
                    currentConversationId.value = list.first().id
                    list.first().defaultModelId?.let { selectedModelId.value = it }
                }
            }
        }
    }

    fun onInputChange(value: String) {
        if (value.length <= MAX_INPUT_LENGTH) {
            inputText.value = value
        } else {
            inputText.value = value.take(MAX_INPUT_LENGTH)
            errorMessage.value = "输入内容已达到 ${MAX_INPUT_LENGTH} 字上限"
        }
    }

    fun clearInput() {
        inputText.value = ""
    }

    fun onHistoryQueryChange(value: String) {
        historyQuery.value = value
    }

    fun selectConversation(conversationId: Long) {
        currentConversationId.value = conversationId
        cachedConversations.firstOrNull { it.id == conversationId }?.defaultModelId?.let {
            selectedModelId.value = it
        }
        activeScreen.value = AppScreen.CHAT
    }

    fun startNewConversation() {
        currentConversationId.value = null
        inputText.value = ""
        activeScreen.value = AppScreen.CHAT
    }

    fun selectModel(modelId: String) {
        selectedModelId.value = modelId
    }

    fun openSettings() {
        activeScreen.value = AppScreen.SETTINGS
    }

    fun openChat() {
        activeScreen.value = AppScreen.CHAT
    }

    fun clearError() {
        errorMessage.value = null
    }

    fun sendMessage() {
        val prompt = inputText.value.trim()
        if (prompt.isBlank() || isSending.value) return
        val modelId = selectedModelId.value
        if (modelId == null) {
            errorMessage.value = "请先在设置中添加模型配置"
            return
        }
        inputText.value = ""
        sendJob = viewModelScope.launch {
            isSending.value = true
            var assistantMessageId: Long? = null
            val content = StringBuilder()
            try {
                val conversationId = currentConversationId.value
                    ?: repository.createConversation(makeTitle(prompt), modelId).also {
                        currentConversationId.value = it
                    }
                repository.insertUserMessage(conversationId, prompt)
                assistantMessageId = repository.insertAssistantPlaceholder(conversationId)
                generateAssistantResponse(conversationId, assistantMessageId, modelId, content)
            } catch (cancellation: CancellationException) {
                assistantMessageId?.let { id ->
                    if (content.isBlank()) {
                        repository.deleteMessage(id)
                    } else {
                        repository.updateAssistantContent(id, content.toString(), MessageStatus.COMPLETE)
                    }
                }
            } catch (throwable: Throwable) {
                val message = throwable.message ?: "请求失败"
                assistantMessageId?.let { id ->
                    repository.markAssistantError(id, content.toString().ifBlank { "请求失败。" }, message)
                }
                errorMessage.value = message
            } finally {
                isSending.value = false
                sendJob = null
            }
        }
    }

    fun stopGeneration() {
        sendJob?.cancel()
    }

    fun regenerateLastAnswer() {
        val conversationId = currentConversationId.value ?: return
        val modelId = selectedModelId.value ?: return
        if (isSending.value) return

        sendJob = viewModelScope.launch {
            isSending.value = true
            var assistantMessageId: Long? = null
            val content = StringBuilder()
            try {
                val lastUser = repository.getLastUserMessage(conversationId)
                    ?: error("当前对话还没有可重新生成的用户消息")
                repository.deleteAssistantMessagesAfter(conversationId, lastUser.createdAt)
                assistantMessageId = repository.insertAssistantPlaceholder(conversationId)
                generateAssistantResponse(conversationId, assistantMessageId, modelId, content)
            } catch (cancellation: CancellationException) {
                assistantMessageId?.let { id ->
                    if (content.isBlank()) {
                        repository.deleteMessage(id)
                    } else {
                        repository.updateAssistantContent(id, content.toString(), MessageStatus.COMPLETE)
                    }
                }
            } catch (throwable: Throwable) {
                val message = throwable.message ?: "重新生成失败"
                assistantMessageId?.let { id ->
                    repository.markAssistantError(id, content.toString().ifBlank { "重新生成失败。" }, message)
                }
                errorMessage.value = message
            } finally {
                isSending.value = false
                sendJob = null
            }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun requestRenameConversation(conversation: ConversationEntity) {
        renameDialog.value = RenameDialogState(conversation.id, conversation.title)
    }

    fun updateRenameTitle(title: String) {
        renameDialog.value = renameDialog.value?.copy(title = title)
    }

    fun confirmRenameConversation() {
        val dialog = renameDialog.value ?: return
        viewModelScope.launch {
            repository.renameConversation(dialog.conversationId, dialog.title)
            renameDialog.value = null
        }
    }

    fun dismissRenameConversation() {
        renameDialog.value = null
    }

    fun deleteConversation(conversationId: Long) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            if (currentConversationId.value == conversationId) currentConversationId.value = null
        }
    }

    fun requestDeleteConversation(conversation: ConversationEntity) {
        confirmDialog.value = ConfirmDialogState(
            action = ConfirmAction.DELETE_CONVERSATION,
            title = "删除对话",
            message = "确定删除「${conversation.title}」吗？这会删除其中的所有消息。",
            targetId = conversation.id.toString(),
        )
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            currentConversationId.value = null
        }
    }

    fun requestClearHistory() {
        confirmDialog.value = ConfirmDialogState(
            action = ConfirmAction.CLEAR_HISTORY,
            title = "清空历史记录",
            message = "确定清空所有对话历史吗？这不会删除模型配置和 API Key。",
        )
    }

    fun openNewModelEditor() {
        modelEditor.value = ModelEditorState(
            isDefault = cachedModels.isEmpty(),
        )
    }

    fun openEditModelEditor(model: ModelConfigEntity) {
        modelEditor.value = ModelEditorState(
            id = model.id,
            displayName = model.displayName,
            protocol = ApiProtocol.fromStored(model.protocol),
            baseUrl = model.baseUrl,
            modelName = model.modelName,
            apiKey = "",
            streamEnabled = model.streamEnabled,
            isDefault = model.isDefault,
            isEditing = true,
        )
    }

    fun dismissModelEditor() {
        modelEditor.value = null
    }

    fun updateModelEditor(state: ModelEditorState) {
        modelEditor.value = state
    }

    fun updateModelEditorProtocol(protocol: ApiProtocol) {
        val current = modelEditor.value ?: return
        val shouldReplaceBaseUrl = current.baseUrl.isBlank() ||
            current.baseUrl == defaultBaseUrl(current.protocol)
        modelEditor.value = current.copy(
            protocol = protocol,
            baseUrl = if (shouldReplaceBaseUrl) defaultBaseUrl(protocol) else current.baseUrl,
        )
    }

    fun applyModelPreset(preset: ModelPreset) {
        val current = modelEditor.value ?: return
        modelEditor.value = current.copy(
            displayName = preset.displayName,
            protocol = preset.protocol,
            baseUrl = preset.baseUrl,
            modelName = preset.modelName,
        )
    }

    fun saveModelEditor() {
        val state = modelEditor.value ?: return
        viewModelScope.launch {
            try {
                val savedId = repository.saveModelConfig(
                    ModelConfigInput(
                        id = state.id,
                        displayName = state.displayName,
                        protocol = state.protocol,
                        baseUrl = state.baseUrl,
                        modelName = state.modelName,
                        apiKey = state.apiKey.ifBlank { null },
                        streamEnabled = state.streamEnabled,
                        isDefault = state.isDefault,
                    ),
                )
                if (state.isDefault || selectedModelId.value == null) selectedModelId.value = savedId
                modelEditor.value = null
            } catch (throwable: Throwable) {
                errorMessage.value = throwable.message ?: "保存模型失败"
            }
        }
    }

    fun deleteModel(modelId: String) {
        viewModelScope.launch {
            repository.deleteModelConfig(modelId)
            if (selectedModelId.value == modelId) selectedModelId.value = null
        }
    }

    fun requestDeleteModel(model: ModelConfigEntity) {
        confirmDialog.value = ConfirmDialogState(
            action = ConfirmAction.DELETE_MODEL,
            title = "删除模型",
            message = "确定删除「${model.displayName}」吗？对应的 API Key 也会从本机删除。",
            targetId = model.id,
        )
    }

    fun dismissConfirmDialog() {
        confirmDialog.value = null
    }

    fun confirmPendingAction() {
        val dialog = confirmDialog.value ?: return
        confirmDialog.value = null
        when (dialog.action) {
            ConfirmAction.DELETE_CONVERSATION -> dialog.targetId?.toLongOrNull()?.let(::deleteConversation)
            ConfirmAction.DELETE_MODEL -> dialog.targetId?.let(::deleteModel)
            ConfirmAction.CLEAR_HISTORY -> clearHistory()
        }
    }

    fun setDefaultModel(modelId: String) {
        viewModelScope.launch {
            repository.setDefaultModel(modelId)
            selectedModelId.value = modelId
        }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(themeMode)
        }
    }

    fun setContextMessageLimit(limit: Int) {
        viewModelScope.launch {
            repository.setContextMessageLimit(limit)
        }
    }

    private suspend fun generateAssistantResponse(
        conversationId: Long,
        assistantMessageId: Long,
        modelId: String,
        content: StringBuilder,
    ) {
        val model = repository.getModelWithKey(modelId)
            ?: error("模型配置不可用，请检查 API Key")
        val context = repository.getMessagesForPrompt(conversationId, cachedSettings.contextMessageLimit)
        var lastPersistAt = 0L
        var lastPersistedLength = 0

        suspend fun persistAssistantContent(status: MessageStatus = MessageStatus.STREAMING, force: Boolean = false) {
            val now = System.currentTimeMillis()
            val shouldPersist = force ||
                content.length - lastPersistedLength >= STREAM_PERSIST_CHARS ||
                now - lastPersistAt >= STREAM_PERSIST_INTERVAL_MS
            if (!shouldPersist) return
            repository.updateAssistantContent(
                assistantMessageId,
                content.toString().ifBlank { if (status == MessageStatus.COMPLETE) "（没有收到内容）" else "" },
                status,
            )
            lastPersistAt = now
            lastPersistedLength = content.length
        }

        repository.streamChat(model, context).collect { event ->
            when (event) {
                is AiStreamEvent.Started -> Unit
                is AiStreamEvent.Delta -> {
                    content.append(event.text)
                    persistAssistantContent()
                }
                is AiStreamEvent.Finished -> {
                    persistAssistantContent(MessageStatus.COMPLETE, force = true)
                }
            }
        }
    }

    private fun chooseDefaultModelId(models: List<ModelConfigEntity>, settings: AppSettings): String? {
        return settings.defaultModelId?.takeIf { configuredId ->
            models.any { it.id == configuredId }
        } ?: models.firstOrNull { it.isDefault }?.id ?: models.firstOrNull()?.id
    }

    private fun makeTitle(prompt: String): String {
        val oneLine = prompt.lineSequence().firstOrNull().orEmpty().trim()
        return oneLine.take(24).ifBlank { "新对话" }
    }

    private fun defaultBaseUrl(protocol: ApiProtocol): String =
        when (protocol) {
            ApiProtocol.OPENAI_COMPATIBLE -> "https://api.openai.com/v1"
            ApiProtocol.CLAUDE -> "https://api.anthropic.com/v1"
            ApiProtocol.GEMINI -> "https://generativelanguage.googleapis.com/v1beta"
        }

    companion object {
        private const val MAX_INPUT_LENGTH = 12_000
        private const val STREAM_PERSIST_CHARS = 80
        private const val STREAM_PERSIST_INTERVAL_MS = 120L

        fun factory(repository: ChatRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ChatViewModel(repository) as T
                }
            }
    }
}
