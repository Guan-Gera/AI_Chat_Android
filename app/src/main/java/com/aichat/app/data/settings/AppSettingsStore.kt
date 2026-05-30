package com.aichat.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aichat.app.data.model.AppSettings
import com.aichat.app.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.appSettingsDataStore by preferencesDataStore("app_settings")

class AppSettingsStore(private val context: Context) {
    private object Keys {
        val Theme = stringPreferencesKey("theme_mode")
        val ContextLimit = intPreferencesKey("context_message_limit")
        val DefaultModelId = stringPreferencesKey("default_model_id")
    }

    val settingsFlow: Flow<AppSettings> = context.appSettingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = ThemeMode.fromStored(prefs[Keys.Theme] ?: ThemeMode.SYSTEM.name),
            contextMessageLimit = (prefs[Keys.ContextLimit] ?: 20).coerceIn(2, 200),
            defaultModelId = prefs[Keys.DefaultModelId],
        )
    }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[Keys.Theme] = themeMode.name
        }
    }

    suspend fun setContextMessageLimit(limit: Int) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[Keys.ContextLimit] = limit.coerceIn(2, 200)
        }
    }

    suspend fun setDefaultModelId(modelId: String?) {
        context.appSettingsDataStore.edit { prefs ->
            if (modelId == null) {
                prefs.remove(Keys.DefaultModelId)
            } else {
                prefs[Keys.DefaultModelId] = modelId
            }
        }
    }
}
