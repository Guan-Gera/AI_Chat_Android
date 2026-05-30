package com.aichat.app.network

sealed interface AiStreamEvent {
    data object Started : AiStreamEvent
    data class Delta(val text: String) : AiStreamEvent
    data object Finished : AiStreamEvent
}
