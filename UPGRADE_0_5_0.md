# AI Chat 0.5.0 升级教程

这份教程适合你现在的情况：不用 Git 命令，只用 GitHub 网页上传文件，然后让 GitHub Actions 自动生成 APK。

## 这次升级包含什么

- 修复键盘弹出后看不到输入框的问题。
- 保留 0.4.0 的 Claude 风格手机界面。
- 用户消息新增“编辑并重发”。
- AI 回复新增“继续生成”。
- 单条消息新增“分享”。
- 模型编辑页新增“测试模型连接”。

## 推荐上传方式

这次不需要清空整个仓库。建议只更新这些内容：

```text
app
README.md
docs/UPGRADE_0_5_0.md
```

如果你想省心，也可以重新上传整个项目，但一定要保留正确目录结构：

```text
.github
app
docs
gradle
.gitignore
README.md
build.gradle.kts
gradle.properties
settings.gradle.kts
```

## GitHub 网页操作步骤

1. 打开你的 GitHub 仓库 `AI_Chat_Android`。
2. 进入 `Code` 页面。
3. 建议先删除仓库里的旧 `app` 文件夹。
4. 点击 `Add file`。
5. 点击 `Upload files`。
6. 把本地这个文件夹拖进去：

```text
D:\Codex-APP\AI Chat_Android\app
```

7. 再上传本地 `README.md`。
8. 进入仓库里的 `docs` 文件夹。
9. 上传本地这个文件：

```text
D:\Codex-APP\AI Chat_Android\docs\UPGRADE_0_5_0.md
```

10. 页面底部点击绿色 `Commit changes`。

## 上传后确认

在 GitHub 仓库里确认这些位置存在：

```text
app/build.gradle.kts
app/src/main/java/com/aichat/app/ui/AIChatApp.kt
app/src/main/java/com/aichat/app/ui/ChatViewModel.kt
app/src/main/java/com/aichat/app/ui/AppUiState.kt
app/src/main/java/com/aichat/app/data/repository/ChatRepository.kt
docs/UPGRADE_0_5_0.md
```

再打开 `app/build.gradle.kts`，确认里面是：

```kotlin
versionCode = 5
versionName = "0.5.0"
```

## 生成 APK

上传完成后，GitHub Actions 一般会自动开始运行。

如果没有自动运行：

1. 点击仓库顶部 `Actions`。
2. 左侧点击 `Build Android Debug APK`。
3. 右侧点击 `Run workflow`。
4. 分支选择 `main`。
5. 点击绿色 `Run workflow`。

成功后：

1. 点开绿色成功的任务。
2. 页面底部找到 `Artifacts`。
3. 下载 `AIChat-debug-apk`。
4. 解压后安装里面的 `app-debug.apk`。

## 手机上重点测试

安装后优先测试这几项：

1. 打开聊天页，点输入框，键盘弹出后输入框应该在键盘上方。
2. 输入几句长文字，输入框不应该消失，也不应该留下大片空白。
3. 长按或点消息右侧更多菜单，测试“编辑并重发”。
4. 点 AI 消息更多菜单，测试“继续生成”和“分享”。
5. 进入设置，打开模型编辑页，点击“测试模型连接”。

## 如果 Actions 失败

不要点旧任务的 `Re-run jobs`。

请点最新失败的任务，展开红色的 `Build Debug APK`，截图包含这些文字附近的内容：

```text
FAILURE
What went wrong
Caused by
```

然后把截图发给我。
