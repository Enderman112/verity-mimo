# Verity MiMo Direct

一个独立的 Forge 附加 mod（单 jar），把小米 MiMo 的 **TTS 和 ASR 直接挂到 Verity 本体**上，**不依赖 / 不使用 Smiley's Better Voice**。原 mod 文件零修改。

## 与 "Verity MiMo Addon" 的区别

| | Verity MiMo Addon | Verity MiMo Direct |
|---|---|---|
| 依赖 | Verity + Smiley's Better Voice beta.9 + Cloth Config 11.1.136+ | 仅 Verity（+ YACL） |
| TTS 接入点 | `FishAudioManager.handleSpeech`（Better Voice 的内部入口） | `TTSHandler.playTTS` |
| ASR 接入点 | `TTSHandler.transcribeAudio` | `TTSHandler.transcribeAudio`（相同） |
| 设置入口 | addon 自己的 Cloth Config 界面 | **Verity 自己的配置菜单**（"Xiaomi MiMo" 分类） |
| 音频播放 | 本 mod 自己播放（SourceDataLine） | 本 mod 自己播放（SourceDataLine） |
| 3D 空间音效 | Verity 的 `TTSHandler.apply3DEffect` | Verity 的 `TTSHandler.apply3DEffect`（相同） |

> 注意：**不要和 Verity MiMo Addon 同时安装**，两者都会接管 Verity 的 TTS。

## 原理（纯 Mixin，原 mod 不动）

- `TTSHandler.playTTS`（HEAD，cancellable）：开启 MiMo 时转走本 mod 的 `MimoTtsService`。
- `TTSHandler.transcribeAudio`（HEAD，cancellable）：开启 MiMo 时转走本 mod 的 `MimoSttService`。
- `VerityConfigUI.createYACLScreen`（Redirect `Builder.category(...)`）：在 Verity 配置菜单末尾追加 "Xiaomi MiMo" 分类（key、音色、语速、风格、语言、base URL）。
  - 显式写了两个重载的描述符：Verity beta.8 的 `createYACLScreen(Screen)` 与 beta.9 新增的 `createYACLScreen(Screen, boolean)`。**Mixin 在方法名不带描述符时只匹配第一个同名方法**（`Quantifier.DEFAULT` 的匹配上限被钳到 1），而 beta.9 的第一个重载只是委托、内部没有 `category()` 调用——所以必须写全描述符，否则注入点数为 0 且 `require = 0` 会静默跳过。
- 打断与 3D 音效通过 `TTSHandler.cancelCurrentSpeech` / `TTSHandler.apply3DEffect`（反射调用，兼容不同版本）。

## 构建

```bash
./build.sh          # 产出 Verity-MiMo-Direct.jar
```

## 安装

1. `Verity-MiMo-Direct.jar` 放进 mods（需 Forge 47.x、YACL 3.6+、Verity 6.0.0-beta.9）。
2. 打开 Verity 配置界面 → 新增的 **Xiaomi MiMo** 分类填 `MiMo API Key`、选音色（默认 `苏打` 中文男声）。
3. 在同一分类里打开 `Enable MiMo TTS` / `Enable MiMo ASR`。
4. 获得 MiMo key：https://platform.xiaomimimo.com/#/console/api-keys

## 版本

- **1.3.0** —— 适配 Verity 6.0.0-beta.9；修复 beta.9 上 "Xiaomi MiMo" 分类不显示的问题（`createYACLScreen` 新增重载导致 Redirect 命中委托方法）。
- 1.2.1 —— 支持 Verity 6.0.0-beta.7（TTS/ASR 从 `AiAPI` 迁移到 `TTSHandler`）。
