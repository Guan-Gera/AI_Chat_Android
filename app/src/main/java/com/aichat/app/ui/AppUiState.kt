package com.aichat.app.ui

import com.aichat.app.data.db.ConversationEntity
import com.aichat.app.data.db.MessageEntity
import com.aichat.app.data.db.ModelConfigEntity
import com.aichat.app.data.model.ApiProtocol
import com.aichat.app.data.model.AppSettings
import com.aichat.app.data.model.ThemeMode

enum class AppScreen {
    CHAT,
    SETTINGS,
}

enum class ConfirmAction {
    DELETE_CONVERSATION,
    DELETE_MODEL,
    CLEAR_HISTORY,
}

data class ModelEditorState(
    val id: String? = null,
    val displayName: String = "",
    val protocol: ApiProtocol = ApiProtocol.OPENAI_COMPATIBLE,
    val baseUrl: String = "https://api.openai.com/v1",
    val modelName: String = "",
    val apiKey: String = "",
    val streamEnabled: Boolean = true,
    val isDefault: Boolean = false,
    val isEditing: Boolean = false,
)

data class RenameDialogState(
    val conversationId: Long,
    val title: String,
)

data class ConfirmDialogState(
    val action: ConfirmAction,
    val title: String,
    val message: String,
    val targetId: String? = null,
)

data class ChatUiState(
    val conversations: List<ConversationEntity> = emptyList(),
    val messages: List<MessageEntity> = emptyList(),
    val modelConfigs: List<ModelConfigEntity> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val currentConversationId: Long? = null,
    val selectedModelId: String? = null,
    val inputText: String = "",
    val historyQuery: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val activeScreen: AppScreen = AppScreen.CHAT,
    val modelEditor: ModelEditorState? = null,
    val renameDialog: RenameDialogState? = null,
    val confirmDialog: ConfirmDialogState? = null,
) {
    val selectedModel: ModelConfigEntity?
        get() = modelConfigs.firstOrNull { it.id == selectedModelId }

    val themeMode: ThemeMode
        get() = settings.themeMode
}
