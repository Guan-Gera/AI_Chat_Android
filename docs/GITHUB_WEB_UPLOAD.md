# GitHub 网页重新上传教程

这份教程适合完全不用 Git 命令、只用 GitHub 网页上传项目。

## 目标

把本地 `D:\Codex-APP\AI Chat_Android` 里的干净项目重新上传到 GitHub，让 Actions 生成 `app-debug.apk`。

## 先清空 GitHub 仓库文件

建议保留仓库，不要删除仓库本身。

在 GitHub 仓库页面逐个删除这些内容：

```text
.github
app
docs
gradle
README.md
build.gradle.kts
gradle.properties
settings.gradle.kts
```

如果仓库根目录有这个文件，也删除：

```text
libs.versions.toml
```

注意：根目录不应该有 `libs.versions.toml`。正确位置是：

```text
gradle/libs.versions.toml
```

## 必须上传的根目录内容

清空后，从本地项目根目录上传这些内容：

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

本地项目根目录是：

```text
D:\Codex-APP\AI Chat_Android
```

## 最容易漏传的两个文件

这两个文件最关键：

```text
.github/workflows/android-debug-apk.yml
gradle/libs.versions.toml
```

上传后你要在 GitHub 仓库里确认：

1. 根目录能看到 `.github` 文件夹。
2. 点进 `.github` -> `workflows`，里面有 `android-debug-apk.yml`。
3. 点进 `gradle` 文件夹，里面有 `libs.versions.toml`。
4. 根目录没有 `libs.versions.toml`。

## 正确的 `gradle/libs.versions.toml` 插件区

点开 `gradle/libs.versions.toml`，拉到底部，应该看到：

```toml
[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

不应该出现：

```toml
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

## 正确的 `app/build.gradle.kts` 插件区

点开 `app/build.gradle.kts`，顶部应该是：

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}
```

不应该出现：

```kotlin
alias(libs.plugins.kotlin.android)
```

## 触发 Actions 打包

上传完成后：

1. 打开 GitHub 仓库页面。
2. 点击顶部 `Actions`。
3. 左侧选择 `Build Android Debug APK`。
4. 如果已经自动开始跑，可以直接等待。
5. 如果没有自动跑，点击右侧 `Run workflow`。
6. 分支选择 `main`。
7. 点击绿色 `Run workflow`。

## 下载 APK

如果构建成功：

1. 点击绿色成功的 workflow run。
2. 进入详情页。
3. 页面底部找到 `Artifacts`。
4. 下载 `AIChat-debug-apk`。
5. 解压后得到：

```text
app-debug.apk
```

把 `app-debug.apk` 发到手机上安装。

## 如果失败

不要点旧失败任务的 `Re-run jobs`。

正确处理方式：

1. 点最新失败的 workflow run。
2. 点左侧或中间的 `build-debug-apk`。
3. 展开红色步骤 `Build Debug APK`。
4. 截图包含 `FAILURE`、`What went wrong`、`Caused by` 的部分。
5. 把截图发给我。
