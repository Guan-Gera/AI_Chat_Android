package com.aichat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aichat.app.ui.AIChatApp
import com.aichat.app.ui.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AIChatRoot()
        }
    }
}

@Composable
private fun AIChatRoot() {
    val app = LocalContext.current.applicationContext as AIChatApplication
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModel.factory(app.container.repository),
    )
    AIChatApp(viewModel = viewModel)
}
