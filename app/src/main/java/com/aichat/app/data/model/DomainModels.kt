package com.aichat.app.data.model

enum class ApiProtocol(val label: String) {
    OPENAI_COMPATIBLE("OpenAI兼容"),
    CLAUDE("Claude"),
    GEMINI("Gemini");

    companion object {
        fun fromStored(value: String): ApiProtocol =
            entries.firstOrNull { it.name == value } ?: OPENAI_COMPATIBLE
    }
}

enum class MessageRole(val wireValue: String, val label: String) {
    USER("user", "我"),
    ASSISTANT("assistant", "AI"),
    SYSTEM("system", "系统");

    companion object {
        fun fromStored(value: String): MessageRole =
            entries.firstOrNull { it.name == value } ?: USER
    }
}

enum class MessageStatus {
    STREAMING,
    COMPLETE,
    ERROR
}

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    LIGHT("浅色"),
    DARK("深色");

    companion object {
        fun fromStored(value: String): ThemeMode =
            entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val contextMessageLimit: Int = 20,
    val defaultModelId: String? = null,
)

data class ChatMessage(
    val role: MessageRole,
    val content: String,
)

data class ModelConfigWithKey(
    val id: String,
    val displayName: String,
    val protocol: ApiProtocol,
    val baseUrl: String,
    val modelName: String,
    val streamEnabled: Boolean,
    val isDefault: Boolean,
    val apiKey: String,
)

data class ModelConfigInput(
    val id: String?,
    val displayName: String,
    val protocol: ApiProtocol,
    val baseUrl: String,
    val modelName: String,
    val apiKey: String?,
    val streamEnabled: Boolean,
    val isDefault: Boolean,
)
