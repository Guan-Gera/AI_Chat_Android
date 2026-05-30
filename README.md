# AI Chat

AI Chat 是一个自用优先的原生 Android 文本对话 App。当前版本重点优化手机使用体验，支持本地历史记录、多模型配置、流式输出、上下文记忆，以及 OpenAI 兼容、Claude、Gemini 三类接口。

## 当前功能

- 文本聊天：发送消息、流式显示、停止生成、重新生成、复制、删除。
- 本地历史：会话列表、新建会话、搜索、重命名、删除、清空历史。
- 多模型配置：手动填写显示名称、协议类型、Base URL、API Key、模型名称，也支持常见平台预设。
- 接口协议：OpenAI 兼容、Claude、Gemini。
- 加密密钥：API Key 使用 Android Keystore 加密保存。
- 上下文记忆：默认每次发送最近 20 条消息，可在设置里调整。
- 阅读体验：AI 回复支持基础 Markdown、标题、列表和代码块显示。
- 手机体验：Claude 风格聊天页、键盘弹出适配、底部模型选择、全屏模型编辑、消息更多菜单、防误删确认。
- 打包方式：支持 GitHub Actions 云端构建 Debug APK。
- 主题：跟随系统、浅色、深色。

## 版本记录

### 0.4.0

- 聊天页改为更接近 Claude 手机端的阅读型布局。
- 修复键盘弹出后输入框下方出现大块空白的问题。
- 输入框改为底部圆角胶囊样式，发送/停止按钮更清晰。
- AI 回复改为正文式展示，减少灰色气泡带来的拥挤感。
- 流式输出期间降低数据库刷新、滚动和 Markdown 渲染频率，减少卡顿和抖动。
- 更新浅色/深色主题为更温和的中性色。

### 0.3.0

- 历史抽屉增加搜索框，手机上更容易找旧对话。
- 顶部模型切换改为底部弹窗，更适合单手操作。
- 模型编辑改为全屏表单，减少键盘遮挡。
- 消息操作收进更多菜单，聊天列表更紧凑。
- 输入框增加一键清空和更大的长文本输入高度。
- 删除会话、删除模型、清空历史记录前增加确认弹窗。
- 增加 GitHub Actions 自动构建 `app-debug.apk`。

### 0.2.0

- 添加 OpenAI、DeepSeek、小米 MiMo、Claude、Gemini 模型预设。
- AI 回复支持基础 Markdown、列表、标题和代码块显示。
- 模型编辑流程更适合新手，减少手动查 Base URL 和模型名的步骤。
- 聊天页支持一键复制当前对话为 Markdown 文本。

### 0.1.0

- 完成文本聊天、本地历史、多模型配置、流式输出和加密 API Key。

## 推荐模型配置示例

| 平台 | 协议类型 | Base URL | 模型名称示例 |
| --- | --- | --- | --- |
| OpenAI | OpenAI兼容 | `https://api.openai.com/v1` | `gpt-4.1` |
| DeepSeek | OpenAI兼容 | `https://api.deepseek.com` | `deepseek-chat` |
| 小米 MiMo | OpenAI兼容 | `https://api.xiaomimimo.com/v1` | `mimo-v2.5-pro` |
| Claude | Claude | `https://api.anthropic.com/v1` | `claude-3-5-sonnet-latest` |
| Gemini | Gemini | `https://generativelanguage.googleapis.com/v1beta` | `gemini-2.5-pro` |
| 自定义 AI | 按服务选择 | 服务商提供 | 服务商提供 |

## 部署到安卓手机

完整教程见 [docs/DEPLOY_ANDROID.md](docs/DEPLOY_ANDROID.md)。

如果你不想在 Windows 上安装 Android Studio，可以把项目上传到 GitHub，然后使用仓库里的 GitHub Actions 自动生成 APK。只用 GitHub 网页上传时，请按 [docs/GITHUB_WEB_UPLOAD.md](docs/GITHUB_WEB_UPLOAD.md) 操作，避免隐藏目录和版本目录传错位置。
