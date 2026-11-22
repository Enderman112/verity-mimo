# Verity MiMo Direct

一个独立的 Forge 附加 mod（单 jar），把小米 MiMo 的 **TTS 和 ASR 直接挂到 Verity 本体**上，**不依赖 / 不使用 Smiley's Better Voice**。原 mod 文件零修改。TTS 与 ASR 的后端可以各自独立切换为 **DeepSeek**（`/v1/tools/tts`、`/v1/tools/asr`）。

## 与 "Verity MiMo Addon" 的区别

| | Verity MiMo Addon | Verity MiMo Direct |
|---|---|---|
| 依赖 | Verity + Smiley's Better Voice + Cloth Config | 仅 Verity（+ YACL） |
| TTS 接入点 | `FishAudioManager.handleSpeech`（Better Voice 的内部入口） | `TTSHandler.playTTS` |
| ASR 接入点 | `TTSHandler.transcribeAudio` | `TTSHandler.transcribeAudio`（相同） |
| 设置入口 | addon 自己的 Cloth Config 界面 | **Verity 自己的配置菜单**（"Xiaomi MiMo" 分类） |
| 音频播放 | 本 mod 自己播放（SourceDataLine） | 本 mod 自己播放（SourceDataLine） |
| 3D 空间音效 | Verity 的 `TTSHandler.apply3DEffect` | Verity 的 `TTSHandler.apply3DEffect`（相同） |

> 注意：**不要和 Verity MiMo Addon 同时安装**，两者都会接管 Verity 的 TTS。

## 原理（纯 Mixin，原 mod 不动）

- `TTSHandler.playTTS`（HEAD，cancellable）：开启 MiMo TTS 且 Verity 的 `USE_TTS` 开启时，取消 Verity 原生 TTS，转由本 mod 的 `MimoTtsService` 合成并播放。
- `TTSHandler.transcribeAudio`（HEAD，cancellable）：开启 MiMo ASR 时，转由本 mod 的 `MimoSttService` 转写；结果再经过 Verity 6.0.0 新增的 `TTSHandler.sanitizeTranscript`（反射调用，旧 beta 版本没有该方法时自动跳过），避免 MiMo 文本绕过 Verity 自己的 "Verity/verdi/birdie" 纠错。
- `VerityConfigUI.createYACLScreen`（Redirect `Builder.category(...)`）：在 Verity 配置菜单末尾追加 "Xiaomi MiMo" 分类（key、音色、语速、风格、语言、base URL）。
  - 显式写了两个重载的描述符：`createYACLScreen(Screen)` 与 beta.9 起新增的 `createYACLScreen(Screen, boolean)`。**Mixin 在方法名不带描述符时只匹配第一个同名方法**（`Quantifier.DEFAULT` 的匹配上限被钳到 1），而新版第一个重载只是委托、内部没有 `category()` 调用——所以必须写全描述符；旧描述符保持 `require = 0`，新描述符命中真正的 UI 方法。
- 打断与 3D 音效通过 `TTSHandler.cancelCurrentSpeech` / `TTSHandler.apply3DEffect`（反射调用，兼容不同版本）。
- 播放层抽成 `tts/TtsPlayback`，两个后端共用：它跟踪当前 `SourceDataLine`，Verity 连续下发多句时新的一句会先停掉上一句，避免两段语音叠在一起（Verity 客户端不会先调 `interruptSpeech`）。
- 后端分派：`tts/MimoTtsService` 与 `stt/MimoSttService` 在入口按 `ttsBackend` / `sttBackend` 配置跳到 `DeepSeekTtsService` / `DeepSeekSttService`，默认仍是 MiMo（老配置不受影响）。DeepSeek TTS 直接返回音频字节（不是 base64），ASR 是 `multipart/form-data` 上传、语言自动识别；DeepSeek 没有语速参数，`mimoTtsSpeed` 只对 MiMo 生效。

> 激活方式只有上面 "Xiaomi MiMo" 分类里的两个开关。Verity 的 TTS/STT Provider 枚举里没有 MIMO，**不需要**（也不能）在那里选 MiMo。

## 构建

```bash
./build.sh          # 产出 Verity-MiMo-Direct.jar
```

需要 JDK 17+；编译期依赖在 `build/lib/`（gson / yacl / mixin / annotations / guava），运行期由游戏提供。

## 安装

1. `Verity-MiMo-Direct.jar` 放进 mods（需 Forge 47.x、YACL 3.6+、Verity 6.0.0；6.0.0-beta.7 及以上也可）。
2. 打开 Verity 配置界面 → 新增的 **Xiaomi MiMo** 分类填 `MiMo API Key`、选音色（默认 `苏打` 中文男声）。
3. 在同一分类里打开 `Enable MiMo TTS` / `Enable MiMo ASR`；TTS 还需要 Verity 自己的 `Use Text To Speech` 保持开启。
4. 获得 MiMo key：https://platform.xiaomimimo.com/#/console/api-keys
5. 想用 DeepSeek：把 `TTS Backend` / `ASR Backend` 改成 `DEEPSEEK`，填 `DeepSeek API Key`（https://platform.deepseek.com/api_keys），音色在 `DeepSeek TTS Voice` 里选（`STELLA` 女声 / `TIDE` 男声）。DeepSeek 与 MiMo 的 key 各自独立，可以混用（例如 MiMo ASR + DeepSeek TTS）。

## 版本

- **1.5.1** —— 与 Addon 版同步版本号（Addon 1.5.1 收紧了对 Verity / Smiley's Better Voice 的版本依赖并把 ASR 接上 `sanitizeTranscript`；Direct 版本来就只依赖 Verity 6.0.0，代码无改动）。
- **1.5.0** —— TTS/ASR 后端可切换：新增 `ttsBackend` / `sttBackend`（`MIMO` 或 `DEEPSEEK`）与 `deepseekApiKey` / `deepseekBaseUrl` / `deepseekTtsVoice`；新增 `DeepSeekTtsService`（`POST /v1/tools/tts`，直接返回 wav 字节）与 `DeepSeekSttService`（`POST /v1/tools/asr`，multipart 上传、语言自动识别）；播放层抽为 `TtsPlayback` 供两个后端共用。默认后端仍是 MiMo。
- 1.4.1 —— 适配 Verity 6.0.0；ASR 结果补走 Verity 的 `sanitizeTranscript`；hook 只保留 `TTSHandler` 并把 `require` 提高到 1（版本再改时直接报错而不是静默失效）；连续语音不再叠音；复用 HttpClient；修正配置界面里 "去 Provider 里选 MIMO" 的过时说明、空文本仍触发合成的边界、语速滑块在 0.95–1.05 之外才生效的问题。
- 1.3.0 —— 适配 Verity 6.0.0-beta.9；修复 beta.9 上 "Xiaomi MiMo" 分类不显示的问题（`createYACLScreen` 新增重载导致 Redirect 命中委托方法）。
- 1.2.1 —— 支持 Verity 6.0.0-beta.7（TTS/ASR 从 `AiAPI` 迁移到 `TTSHandler`）。
