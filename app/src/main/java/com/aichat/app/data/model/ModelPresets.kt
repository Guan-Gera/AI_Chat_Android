package com.aichat.app.data.model

data class ModelPreset(
    val label: String,
    val displayName: String,
    val protocol: ApiProtocol,
    val baseUrl: String,
    val modelName: String,
)

object ModelPresets {
    val defaults: List<ModelPreset> = listOf(
        ModelPreset(
            label = "OpenAI",
            displayName = "OpenAI GPT-4.1",
            protocol = ApiProtocol.OPENAI_COMPATIBLE,
            baseUrl = "https://api.openai.com/v1",
            modelName = "gpt-4.1",
        ),
        ModelPreset(
            label = "DeepSeek",
            displayName = "DeepSeek Chat",
            protocol = ApiProtocol.OPENAI_COMPATIBLE,
            baseUrl = "https://api.deepseek.com",
            modelName = "deepseek-chat",
        ),
        ModelPreset(
            label = "小米 MiMo",
            displayName = "小米 MiMo Pro",
            protocol = ApiProtocol.OPENAI_COMPATIBLE,
            baseUrl = "https://api.xiaomimimo.com/v1",
            modelName = "mimo-v2.5-pro",
        ),
        ModelPreset(
            label = "Claude",
            displayName = "Claude Sonnet",
            protocol = ApiProtocol.CLAUDE,
            baseUrl = "https://api.anthropic.com/v1",
            modelName = "claude-3-5-sonnet-latest",
        ),
        ModelPreset(
            label = "Gemini",
            displayName = "Gemini 2.5 Pro",
            protocol = ApiProtocol.GEMINI,
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            modelName = "gemini-2.5-pro",
        ),
    )
}
