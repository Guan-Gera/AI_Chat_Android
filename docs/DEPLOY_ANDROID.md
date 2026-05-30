# AI Chat 安卓手机部署教程

这份教程按 Windows 电脑 + 魅族 20 Classic + Android 16 编写。

## 最省事方式：GitHub 云端打包 APK

这种方式不需要你在 Windows 上安装 Android Studio、Java、Gradle 或 Android SDK。你只需要把项目上传到 GitHub，然后下载云端生成的 APK。

如果你只用 GitHub 网页上传项目，先看更详细的网页上传清单：[GITHUB_WEB_UPLOAD.md](GITHUB_WEB_UPLOAD.md)。

### 1. 创建 GitHub 仓库

1. 打开 `https://github.com`。
2. 登录账号。
3. 点击右上角 `+`，选择 `New repository`。
4. 仓库名可以写 `AI-Chat-Android`。
5. 选择 `Private` 或 `Public` 都可以。
6. 创建仓库。

### 2. 上传项目

如果你会用 Git，可以把 `D:\Codex-APP\AI Chat_Android` 推送到刚创建的仓库。

如果你暂时不会 Git，可以在 GitHub 仓库页面点击 `Add file` -> `Upload files`，把项目文件上传上去。注意要保留 `.github/workflows/android-debug-apk.yml` 这个文件。

### 3. 运行自动打包

1. 打开 GitHub 仓库页面。
2. 点击顶部 `Actions`。
3. 选择 `Build Android Debug APK`。
4. 点击 `Run workflow`。
5. 等待构建完成。

如果你之前已经有失败的运行记录，不要只点旧页面右上角的 `Re-run jobs`。那个按钮会重跑旧提交里的旧 workflow，可能继续失败。正确做法是先上传最新项目文件，尤其是 `.github/workflows/android-debug-apk.yml`，再从最新代码触发新的 workflow。

构建成功后，在页面底部的 `Artifacts` 里会出现：

```text
AIChat-debug-apk
```

下载并解压，里面有：

```text
app-debug.apk
```

### 4. 安装到魅族 20 Classic

1. 把 `app-debug.apk` 发送到手机，比如微信文件传输、数据线、网盘都可以。
2. 在手机文件管理器里点击 APK。
3. 如果提示禁止安装未知来源应用，按提示允许当前文件管理器安装未知来源应用。
4. 安装完成后，桌面会出现 `AI Chat`。

Debug APK 适合你自己使用。以后如果要长期分享给朋友，建议再做正式签名版 Release APK。

---

## 1. 安装 Android Studio

1. 打开 Android Studio 官网：`https://developer.android.com/studio`
2. 下载 Windows 版本并安装。
3. 第一次启动时选择 Standard 安装。
4. 安装完成后，确认 Android Studio 自带的 JDK 可用。

## 2. 安装 Android SDK 36

1. 打开 Android Studio。
2. 进入 `Settings`。
3. 打开 `Languages & Frameworks` -> `Android SDK`。
4. 在 `SDK Platforms` 勾选 `Android API 36`。
5. 在 `SDK Tools` 勾选：
   - Android SDK Build-Tools
   - Android SDK Platform-Tools
   - Android SDK Command-line Tools
6. 点击 `Apply`，等待下载完成。

## 3. 打开项目

1. 在 Android Studio 首页点击 `Open`。
2. 选择本项目目录：`D:\Codex-APP\AI Chat_Android`
3. 等待 Gradle Sync。
4. 如果提示选择 Gradle JDK，选择 Android Studio 自带的 `Embedded JDK` 或 `jbr-17`。
5. 如果提示下载 Gradle 或 Android Gradle Plugin，允许下载。

如果 Android Studio 提示缺少 Gradle Wrapper，可以先用 Android Studio 的修复提示生成 Wrapper。也可以在安装本机 Gradle 后，在项目根目录运行：

```powershell
gradle wrapper --gradle-version 9.4.1
```

之后重新打开项目或点击 `Sync Project with Gradle Files`。

## 4. 设置魅族 20 Classic

1. 打开手机 `设置`。
2. 进入 `关于手机`。
3. 连续点击 `版本号`，直到提示已进入开发者模式。
4. 返回设置，进入 `开发者选项`。
5. 打开 `USB 调试`。
6. 用 USB 数据线连接电脑。
7. 手机弹出调试授权时，选择允许。

## 5. 运行到手机

1. Android Studio 顶部设备列表选择你的魅族 20 Classic。
2. 点击绿色运行按钮。
3. 等待安装完成。
4. 手机上打开 `AI Chat`。

第一次进入 App 后，需要到设置里添加模型：

你可以先点 `快速选择` 里的 `小米 MiMo`，App 会自动填好协议、Base URL 和模型名称，然后你只需要填写自己的 API Key。

```text
显示名称：小米 MiMo
协议类型：OpenAI兼容
Base URL：https://api.xiaomimimo.com/v1
模型名称：mimo-v2.5-pro
API Key：填写你自己的 MiMo Key
流式输出：开启
设为默认模型：开启
```

DeepSeek、OpenAI 和大多数第三方中转服务也选择 `OpenAI兼容`。

## 6. 构建 Debug APK

在 Android Studio 里：

1. 点击顶部菜单 `Build`。
2. 选择 `Build Bundle(s) / APK(s)`。
3. 选择 `Build APK(s)`。
4. 构建完成后点击右下角提示里的 `locate`。
5. Debug APK 通常位于：

```text
app\build\outputs\apk\debug\app-debug.apk
```

## 7. 手动安装 APK

如果你没有用 Android Studio 直接运行，也可以把 `app-debug.apk` 复制到手机，然后在手机文件管理器里点击安装。

如果提示禁止安装未知来源应用：

1. 按手机提示进入权限页面。
2. 允许当前文件管理器安装未知来源应用。
3. 返回重新安装 APK。

## 8. 常见问题

### Gradle Sync 失败

优先检查：

- Android Studio 是否能联网。
- 是否安装了 Android API 36。
- Gradle JDK 是否选择了 JDK 17。
- 重新点击 `File` -> `Sync Project with Gradle Files`。

### 手机不出现在设备列表

优先检查：

- USB 数据线是否支持数据传输。
- 手机是否开启 USB 调试。
- 手机是否点了允许调试。
- Android SDK Platform-Tools 是否安装。

### API 请求失败

优先检查：

- Base URL 是否正确。
- API Key 是否完整。
- 模型名称是否和平台文档一致。
- 当前网络是否能访问对应平台。
- Claude 要选择 `Claude` 协议，Gemini 要选择 `Gemini` 协议。

### 聊天时模型忘记前文

进入设置，把 `每次发送最近多少条消息` 调大。默认是 20，最大可设置到 200。
