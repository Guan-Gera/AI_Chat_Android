package com.aichat.app

import android.app.Application
import android.content.Context
import com.aichat.app.data.db.AppDatabase
import com.aichat.app.data.repository.ChatRepository
import com.aichat.app.data.security.SecureApiKeyStore
import com.aichat.app.data.settings.AppSettingsStore
import com.aichat.app.network.AiService

class AIChatApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(context: Context) {
    private val database = AppDatabase.create(context)
    private val apiKeyStore = SecureApiKeyStore(context)
    private val settingsStore = AppSettingsStore(context)
    private val aiService = AiService()

    val repository = ChatRepository(
        database = database,
        apiKeyStore = apiKeyStore,
        settingsStore = settingsStore,
        aiService = aiService,
    )
}
