# Verity MiMo Addon

一个独立的 Forge 附加 mod（单 jar），给 **Verity 6.0.0-beta.9** 和 **Smiley's Better Voice 6.0.0-beta.9** 增加小米 MiMo 的 TTS 与 ASR，**不改动这两个 mod 本身**。TTS 与 ASR 的后端可以各自独立切换为 **DeepSeek**（`/v1/tools/tts`、`/v1/tools/asr`）。

## 原理

- 纯 Mixin 方案，两个原 mod 保持原样，版权安全。
- Mixin 钩子：
  - TTS：`FishAudioManager.handleSpeech`（HEAD，cancellable）——Better Voice beta.9 自己的 `TtsHandlerMixin` 把 `TTSHandler.playTTS` 委托给它，并以它的返回值决定是否取消 Verity 原生 TTS。启用 MiMo 时我们返回 `true` 并转走本 mod 的 `MimoTtsService`，由 Better Voice 负责取消原生 TTS。
  - ASR：`TTSHandler.transcribeAudio`（HEAD，cancellable）——启用 MiMo 时转走本 mod 的 `MimoSttService`。
- 音频播放由本 mod 自己完成（`SourceDataLine` 播放 + Verity 的 `apply3DEffect` 3D 音效 + 读 `cancelCurrentSpeech` 处理打断），因为 Better Voice beta.9 已不再对外暴露播放管线。播放层抽成 `tts/TtsPlayback`，MiMo 与 DeepSeek 两个后端共用。
- 后端分派：`tts/MimoTtsService` 与 `stt/MimoSttService` 在入口按 `ttsBackend` / `sttBackend` 跳到 `DeepSeekTtsService` / `DeepSeekSttService`，默认仍是 MiMo。DeepSeek TTS 直接返回音频字节，ASR 是 multipart 上传、语言自动识别，且没有语速参数。

## 构建

```bash
./build.sh          # 产出 Verity-MiMo-Addon.jar
```

需要 JDK 17+。编译期依赖在 `build/lib/`（gson / cloth-config / mixin / annotations / guava），运行期由游戏提供。

## 安装

1. 把 `Verity-MiMo-Addon.jar` 放进 mods 目录，需要：
   - Forge 47.x + Minecraft 1.20.1
   - Verity 6.0.0-beta.9
   - Smiley's Better Voice 6.0.0-beta.9（可选；不装的话请改用 Verity-MiMo-Direct）
   - Cloth Config **11.1.136 或更高**（Better Voice beta.9 强制要求）
2. 配置 `config/verity_mimo-common.toml`（或在 Mods 列表里打开本 mod 的 Config 界面）：
   - `mimoApiKey`：MiMo API key
   - `enableMimoTts` / `enableMimoAsr`：开关
   - `mimoVoiceMode`、`mimoTtsVoice`、`mimoTtsSpeed`、`mimoTtsStyle`、`mimoSttLanguage`
   - `ttsBackend` / `sttBackend`：`MIMO`（默认）或 `DEEPSEEK`
   - `deepseekApiKey`、`deepseekBaseUrl`（留空即 `https://api.deepseek.com`）、`deepseekTtsVoice`（`STELLA` / `TIDE`）
3. **不要和 Verity-MiMo-Direct 同时安装**：两者都会接管 Verity 的 TTS，同时启用行为不可预测。

## 版本

- **1.5.0** —— TTS/ASR 后端可切换：新增 `ttsBackend` / `sttBackend`（`MIMO` 或 `DEEPSEEK`）与 `deepseekApiKey` / `deepseekBaseUrl` / `deepseekTtsVoice`；新增 `DeepSeekTtsService`（`POST /v1/tools/tts`）与 `DeepSeekSttService`（`POST /v1/tools/asr`，multipart、语言自动识别）；播放层抽为 `TtsPlayback` 共用。默认后端仍是 MiMo。
- 1.3.0 —— 适配 Verity 6.0.0-beta.9 + Smiley's Better Voice 6.0.0-beta.9。Better Voice 换了 modId（`verity_cartesia` → `smileys_better_voice`）、包名（`com.gabe.veritycartesia` → `com.toast5.smileysbettervoice`）和接入点，旧 API 已全部移除。
- 1.2.1 —— 适配 Verity 6.0.0-beta.7/8 + Smiley's Better Voice 6.0.0（`com.gabe.veritycartesia`）。
