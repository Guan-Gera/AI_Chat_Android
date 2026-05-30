package com.aichat.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Query("SELECT * FROM conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversation(conversationId: Long): ConversationEntity?

    @Insert
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: Long)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    fun observeMessages(conversationId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC, id ASC")
    suspend fun getMessages(conversationId: Long): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessage(messageId: Long): MessageEntity?

    @Insert
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: Long)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId AND createdAt > :createdAt")
    suspend fun deleteMessagesAfter(conversationId: Long, createdAt: Long)

    @Query("SELECT * FROM model_configs ORDER BY isDefault DESC, updatedAt DESC, displayName COLLATE NOCASE ASC")
    fun observeModelConfigs(): Flow<List<ModelConfigEntity>>

    @Query("SELECT * FROM model_configs WHERE id = :modelId")
    suspend fun getModelConfig(modelId: String): ModelConfigEntity?

    @Query("SELECT * FROM model_configs WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefaultModelConfig(): ModelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertModelConfig(modelConfig: ModelConfigEntity)

    @Query("UPDATE model_configs SET isDefault = 0")
    suspend fun clearDefaultModelFlags()

    @Query("DELETE FROM model_configs WHERE id = :modelId")
    suspend fun deleteModelConfig(modelId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("DELETE FROM conversations")
    suspend fun deleteAllConversations()
}
