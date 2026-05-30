package com.aichat.app.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [Index(value = ["updatedAt"])]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val defaultModelId: String?,
)

@Entity(
    tableName = "messages",
    indices = [Index(value = ["conversationId", "createdAt"])]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val role: String,
    val content: String,
    val status: String,
    val createdAt: Long,
    val errorMessage: String? = null,
)

@Entity(tableName = "model_configs")
data class ModelConfigEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val protocol: String,
    val baseUrl: String,
    val modelName: String,
    val streamEnabled: Boolean,
    val isDefault: Boolean,
    val apiKeyRef: String,
    val createdAt: Long,
    val updatedAt: Long,
)
