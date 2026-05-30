package com.aichat.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aichat.app.data.db.ConversationEntity
import com.aichat.app.data.db.MessageEntity
import com.aichat.app.data.db.ModelConfigEntity
import com.aichat.app.data.model.ApiProtocol
import com.aichat.app.data.model.MessageRole
import com.aichat.app.data.model.MessageStatus
import com.aichat.app.data.model.ModelPreset
import com.aichat.app.data.model.ModelPresets
import com.aichat.app.data.model.ThemeMode
import com.aichat.app.ui.markdown.MarkdownMessage
import com.aichat.app.ui.theme.AIChatTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AIChatApp(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AIChatTheme(themeMode = state.themeMode) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }
        var showModelSheet by remember { mutableStateOf(false) }

        LaunchedEffect(state.errorMessage) {
            val message = state.errorMessage
            if (!message.isNullOrBlank()) {
                snackbarHostState.showSnackbar(message)
                viewModel.clearError()
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = state.activeScreen == AppScreen.CHAT,
                drawerContent = {
                    HistoryDrawer(
                        state = state,
                        onNewChat = {
                            viewModel.startNewConversation()
                            scope.launch { drawerState.close() }
                        },
                        onConversationClick = {
                            viewModel.selectConversation(it)
                            scope.launch { drawerState.close() }
                        },
                        onHistoryQueryChange = viewModel::onHistoryQueryChange,
                        onRename = viewModel::requestRenameConversation,
                        onDelete = viewModel::requestDeleteConversation,
                        onSettings = {
                            viewModel.openSettings()
                            scope.launch { drawerState.close() }
                        },
                    )
                },
            ) {
                when (state.activeScreen) {
                    AppScreen.CHAT -> ChatScreen(
                        state = state,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onOpenSettings = viewModel::openSettings,
                        onOpenModelPicker = { showModelSheet = true },
                        onInputChange = viewModel::onInputChange,
                        onClearInput = viewModel::clearInput,
                        onSend = viewModel::sendMessage,
                        onStop = viewModel::stopGeneration,
                        onDeleteMessage = viewModel::deleteMessage,
                        onRegenerate = viewModel::regenerateLastAnswer,
                    )

                    AppScreen.SETTINGS -> SettingsScreen(
                        state = state,
                        onBack = viewModel::openChat,
                        onAddModel = viewModel::openNewModelEditor,
                        onEditModel = viewModel::openEditModelEditor,
                        onDeleteModel = viewModel::requestDeleteModel,
                        onSetDefaultModel = viewModel::setDefaultModel,
                        onThemeChange = viewModel::setThemeMode,
                        onContextLimitChange = viewModel::setContextMessageLimit,
                        onClearHistory = viewModel::requestClearHistory,
                    )
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }

        if (showModelSheet) {
            ModelPickerSheet(
                models = state.modelConfigs,
                selectedModelId = state.selectedModelId,
                onSelectModel = {
                    viewModel.selectModel(it)
                    showModelSheet = false
                },
                onDismiss = { showModelSheet = false },
            )
        }

        state.modelEditor?.let { editor ->
            ModelEditorScreen(
                state = editor,
                onChange = viewModel::updateModelEditor,
                onProtocolChange = viewModel::updateModelEditorProtocol,
                onApplyPreset = viewModel::applyModelPreset,
                onDismiss = viewModel::dismissModelEditor,
                onSave = viewModel::saveModelEditor,
            )
        }

        state.renameDialog?.let { dialog ->
            RenameConversationDialog(
                state = dialog,
                onChange = viewModel::updateRenameTitle,
                onDismiss = viewModel::dismissRenameConversation,
                onSave = viewModel::confirmRenameConversation,
            )
        }

        state.confirmDialog?.let { dialog ->
            ConfirmActionDialog(
                state = dialog,
                onDismiss = viewModel::dismissConfirmDialog,
                onConfirm = viewModel::confirmPendingAction,
            )
        }
    }
}

@Composable
private fun HistoryDrawer(
    state: ChatUiState,
    onNewChat: () -> Unit,
    onConversationClick: (Long) -> Unit,
    onHistoryQueryChange: (String) -> Unit,
    onRename: (ConversationEntity) -> Unit,
    onDelete: (ConversationEntity) -> Unit,
    onSettings: () -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 336.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "AI Chat",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onSettings) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        }
        Button(
            onClick = onNewChat,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("新建对话")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = state.historyQuery,
            onValueChange = onHistoryQueryChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (state.historyQuery.isNotBlank()) {
                    IconButton(onClick = { onHistoryQueryChange("") }) {
                        Icon(Icons.Default.Close, contentDescription = "清空搜索")
                    }
                }
            },
            placeholder = { Text("搜索历史") },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()
        val filteredConversations = remember(state.conversations, state.historyQuery) {
            val query = state.historyQuery.trim()
            if (query.isBlank()) {
                state.conversations
            } else {
                state.conversations.filter { conversation ->
                    conversation.title.contains(query, ignoreCase = true)
                }
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredConversations, key = { it.id }) { conversation ->
                ListItem(
                    headlineContent = {
                        Text(
                            conversation.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    supportingContent = {
                        Text(formatTime(conversation.updatedAt))
                    },
                    trailingContent = {
                        Row {
                            IconButton(onClick = { onRename(conversation) }) {
                                Icon(Icons.Default.Edit, contentDescription = "重命名")
                            }
                            IconButton(onClick = { onDelete(conversation) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    },
                    modifier = Modifier.clickable { onConversationClick(conversation.id) },
                )
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    thickness = DividerDefaults.Thickness,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreen(
    state: ChatUiState,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onInputChange: (String) -> Unit,
    onClearInput: () -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onRegenerate: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "历史记录")
                    }
                },
                title = {
                    ModelSelector(
                        models = state.modelConfigs,
                        selectedModelId = state.selectedModelId,
                        onOpenModelPicker = onOpenModelPicker,
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(exportConversationMarkdown(state.messages)))
                        },
                        enabled = state.messages.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "复制当前对话")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
            )
        },
        bottomBar = {
            ChatInputBar(
                text = state.inputText,
                isSending = state.isSending,
                enabled = state.modelConfigs.isNotEmpty(),
                onTextChange = onInputChange,
                onClearInput = onClearInput,
                onSend = onSend,
                onStop = onStop,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (state.messages.isEmpty()) {
                EmptyChat(
                    hasModels = state.modelConfigs.isNotEmpty(),
                    onOpenSettings = onOpenSettings,
                )
            } else {
                MessageList(
                    messages = state.messages,
                    onDeleteMessage = onDeleteMessage,
                    onRegenerate = onRegenerate,
                )
            }
        }
    }
}

@Composable
private fun ModelSelector(
    models: List<ModelConfigEntity>,
    selectedModelId: String?,
    onOpenModelPicker: () -> Unit,
) {
    val selected = models.firstOrNull { it.id == selectedModelId }
    TextButton(onClick = onOpenModelPicker) {
        Text(
            text = selected?.displayName ?: "未配置模型",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelPickerSheet(
    models: List<ModelConfigEntity>,
    selectedModelId: String?,
    onSelectModel: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = "切换模型",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        if (models.isEmpty()) {
            Text(
                text = "还没有模型配置，请先到设置里添加。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(20.dp),
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(models, key = { it.id }) { model ->
                    ListItem(
                        headlineContent = { Text(model.displayName) },
                        supportingContent = {
                            Text("${ApiProtocol.fromStored(model.protocol).label} · ${model.modelName}")
                        },
                        trailingContent = {
                            if (model.id == selectedModelId) {
                                AssistChip(onClick = {}, label = { Text("当前") })
                            } else if (model.isDefault) {
                                AssistChip(onClick = {}, label = { Text("默认") })
                            }
                        },
                        modifier = Modifier.clickable { onSelectModel(model.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyChat(
    hasModels: Boolean,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (hasModels) "开始新的对话" else "请先添加模型",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (hasModels) {
                "输入问题后即可开始聊天。"
            } else {
                "在设置里填写 Base URL、API Key 和模型名称。"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (!hasModels) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onOpenSettings, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("打开设置")
            }
        }
    }
}

@Composable
private fun MessageList(
    messages: List<MessageEntity>,
    onDeleteMessage: (Long) -> Unit,
    onRegenerate: () -> Unit,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size, messages.lastOrNull()?.content) {
        if (messages.isNotEmpty()) {
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val shouldAutoScroll = messages.size <= 2 || lastVisibleIndex >= messages.lastIndex - 2
            if (shouldAutoScroll) listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(
                message = message,
                onDelete = { onDeleteMessage(message.id) },
                onRegenerate = onRegenerate,
            )
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    onDelete: () -> Unit,
    onRegenerate: () -> Unit,
) {
    val role = MessageRole.fromStored(message.role)
    val isUser = role == MessageRole.USER
    val clipboard = LocalClipboardManager.current
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.92f),
        ) {
            Surface(
                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                shape = RoundedCornerShape(8.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    SelectionContainer {
                        val visibleContent = message.content.ifBlank {
                            if (message.status == MessageStatus.STREAMING.name) "正在生成..." else ""
                        }
                        if (isUser || visibleContent == "正在生成...") {
                            Text(
                                text = visibleContent,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            MarkdownMessage(content = visibleContent)
                        }
                    }
                    if (message.status == MessageStatus.ERROR.name && !message.errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message.errorMessage.orEmpty(),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(40.dp),
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = "消息操作")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("复制") },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                        enabled = message.content.isNotBlank(),
                        onClick = {
                            clipboard.setText(AnnotatedString(message.content))
                            menuExpanded = false
                        },
                    )
                    if (!isUser) {
                        DropdownMenuItem(
                            text = { Text("重新生成") },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onRegenerate()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    isSending: Boolean,
    enabled: Boolean,
    onTextChange: (String) -> Unit,
    onClearInput: () -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                enabled = enabled && !isSending,
                placeholder = { Text(if (enabled) "输入消息" else "先添加模型") },
                trailingIcon = {
                    if (text.isNotBlank() && !isSending) {
                        IconButton(onClick = onClearInput) {
                            Icon(Icons.Default.Close, contentDescription = "清空输入")
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                minLines = 1,
                maxLines = 8,
                shape = RoundedCornerShape(8.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (isSending) {
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.Stop, contentDescription = "停止")
                }
            } else {
                IconButton(onClick = onSend, enabled = enabled && text.isNotBlank()) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SettingsScreen(
    state: ChatUiState,
    onBack: () -> Unit,
    onAddModel: () -> Unit,
    onEditModel: (ModelConfigEntity) -> Unit,
    onDeleteModel: (ModelConfigEntity) -> Unit,
    onSetDefaultModel: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    onContextLimitChange: (Int) -> Unit,
    onClearHistory: () -> Unit,
) {
    var contextLimitText by remember(state.settings.contextMessageLimit) {
        mutableStateOf(state.settings.contextMessageLimit.toString())
    }
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                title = { Text("设置") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader(title = "模型")
                Button(onClick = onAddModel, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加模型")
                }
            }
            items(state.modelConfigs, key = { it.id }) { model ->
                ModelConfigItem(
                    model = model,
                    onEdit = { onEditModel(model) },
                    onDelete = { onDeleteModel(model) },
                    onSetDefault = { onSetDefaultModel(model.id) },
                )
            }
            item {
                SectionHeader(title = "上下文")
                OutlinedTextField(
                    value = contextLimitText,
                    onValueChange = {
                        contextLimitText = it.filter(Char::isDigit).take(3)
                        contextLimitText.toIntOrNull()?.let(onContextLimitChange)
                    },
                    label = { Text("每次发送最近多少条消息") },
                    supportingText = { Text("默认 20，范围 2 到 200。") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                )
            }
            item {
                SectionHeader(title = "主题")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ThemeMode.entries.forEach { mode ->
                        FilterChip(
                            selected = state.settings.themeMode == mode,
                            onClick = { onThemeChange(mode) },
                            label = { Text(mode.label) },
                        )
                    }
                }
            }
            item {
                SectionHeader(title = "数据")
                Text(
                    text = "聊天页右上角的复制按钮可以把当前对话导出为 Markdown 文本。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onClearHistory,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清空历史记录")
                }
            }
            item {
                SectionHeader(title = "关于")
                Text(
                    text = "AI Chat 0.3.0 · 手机优先交互、全屏模型编辑和云端 APK 打包。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun ModelConfigItem(
    model: ModelConfigEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSetDefault: () -> Unit,
) {
    OutlinedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "${ApiProtocol.fromStored(model.protocol).label} · ${model.modelName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (model.isDefault) AssistChip(onClick = {}, label = { Text("默认") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = model.baseUrl,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                TextButton(onClick = onSetDefault, enabled = !model.isDefault) {
                    Text("设为默认")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ModelEditorScreen(
    state: ModelEditorState,
    onChange: (ModelEditorState) -> Unit,
    onProtocolChange: (ApiProtocol) -> Unit,
    onApplyPreset: (ModelPreset) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    var apiKeyVisible by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    },
                    title = { Text(if (state.isEditing) "编辑模型" else "添加模型") },
                )
            },
            bottomBar = {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("取消")
                        }
                        Button(
                            onClick = onSave,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("保存")
                        }
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "快速选择",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ModelPresets.defaults.forEach { preset ->
                        AssistChip(
                            onClick = { onApplyPreset(preset) },
                            label = { Text(preset.label) },
                        )
                    }
                }
                HorizontalDivider()
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ApiProtocol.entries.forEach { protocol ->
                        FilterChip(
                            selected = state.protocol == protocol,
                            onClick = { onProtocolChange(protocol) },
                            label = { Text(protocol.label) },
                        )
                    }
                }
                OutlinedTextField(
                    value = state.displayName,
                    onValueChange = { onChange(state.copy(displayName = it)) },
                    label = { Text("显示名称") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.baseUrl,
                    onValueChange = { onChange(state.copy(baseUrl = it)) },
                    label = { Text("Base URL") },
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { clipboard.setText(AnnotatedString(state.baseUrl)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制 Base URL")
                            }
                            if (state.baseUrl.isNotBlank()) {
                                IconButton(onClick = { onChange(state.copy(baseUrl = "")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清空 Base URL")
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.modelName,
                    onValueChange = { onChange(state.copy(modelName = it)) },
                    label = { Text("模型名称") },
                    singleLine = true,
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { clipboard.setText(AnnotatedString(state.modelName)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "复制模型名称")
                            }
                            if (state.modelName.isNotBlank()) {
                                IconButton(onClick = { onChange(state.copy(modelName = "")) }) {
                                    Icon(Icons.Default.Close, contentDescription = "清空模型名称")
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = { onChange(state.copy(apiKey = it)) },
                    label = { Text(if (state.isEditing) "API Key（留空不修改）" else "API Key") },
                    singleLine = true,
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                    ),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(
                                imageVector = if (apiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (apiKeyVisible) "隐藏 API Key" else "显示 API Key",
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.streamEnabled,
                        onCheckedChange = { onChange(state.copy(streamEnabled = it)) },
                    )
                    Text("流式输出")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = state.isDefault,
                        onCheckedChange = { onChange(state.copy(isDefault = it)) },
                    )
                    Text("设为默认模型")
                }
                Text(
                    text = protocolHint(state.protocol),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RenameConversationDialog(
    state: RenameDialogState,
    onChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重命名对话") },
        text = {
            OutlinedTextField(
                value = state.title,
                onValueChange = onChange,
                label = { Text("标题") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = onSave, shape = RoundedCornerShape(8.dp)) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

@Composable
private fun ConfirmActionDialog(
    state: ConfirmDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.title) },
        text = { Text(state.message) },
        confirmButton = {
            Button(onClick = onConfirm, shape = RoundedCornerShape(8.dp)) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}

private fun protocolHint(protocol: ApiProtocol): String =
    when (protocol) {
        ApiProtocol.OPENAI_COMPATIBLE ->
            "DeepSeek、OpenAI、小米 MiMo 和大多数中转服务都可以选这一类。MiMo 示例 Base URL：https://api.xiaomimimo.com/v1。"
        ApiProtocol.CLAUDE ->
            "Claude 官方接口通常使用 https://api.anthropic.com/v1。"
        ApiProtocol.GEMINI ->
            "Gemini 官方接口通常使用 https://generativelanguage.googleapis.com/v1beta。"
    }

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun exportConversationMarkdown(messages: List<MessageEntity>): String {
    if (messages.isEmpty()) return ""
    return buildString {
        appendLine("# AI Chat 对话")
        appendLine()
        messages.forEach { message ->
            val role = MessageRole.fromStored(message.role)
            appendLine("## ${role.label} · ${formatTime(message.createdAt)}")
            appendLine()
            appendLine(message.content.ifBlank { message.errorMessage.orEmpty() })
            appendLine()
        }
    }.trimEnd()
}
