# Verity MiMo Direct

一个独立的 Forge 附加 mod（单 jar），把小米 MiMo 的 **TTS 和 ASR 直接挂到 Verity 本体**上，**不依赖 / 不使用 Smiley's Better Voice**。原 mod 文件零修改。

## 与 "Verity MiMo Addon" 的区别

| | Verity MiMo Addon | Verity MiMo Direct |
|---|---|---|
| 依赖 | Verity + Smiley's Better Voice | 仅 Verity（不用 BetterVoice） |
| TTS 走 | BetterVoice 的分发逻辑 | Verity 自己的 `AiAPI.playTTS` |
| ASR 走 | `AiAPI.transcribeAudio` | `AiAPI.transcribeAudio`（相同） |
| 设置入口 | addon 自己的配置界面 | **Verity 自己的配置菜单**（新增 "Xiaomi MiMo" 分类） |
| 3D 空间音效 | BetterVoice 的 WavPlayer | Verity 的 `AiAPI.apply3DEffect` |

> 注意：这个方案要求**不要装 BetterVoice**（或把它关掉），否则它的 mixin 会抢 TTS。

## 原理（纯 Mixin，原 mod 不动）

- `EnumHelper.addEnum` 给 Verity 的 `TTSProvider` / `STTProvider` 动态加 `MIMO`（Verity 菜单里的 Provider 下拉框会出现 MIMO）。
- `AiAPI.playTTS`（HEAD）：选 MIMO 时转走本 mod 的 `MimoTtsService`。
- `AiAPI.transcribeAudio`（HEAD）：选 MIMO 时转走本 mod 的 `MimoSttService`。
- `VerityConfigUI.createYACLScreen`（Redirect `.category(...)`）：在 Verity 配置菜单末尾追加 "Xiaomi MiMo" 分类（key、音色、语速、风格、语言、base URL）。

## 构建

```bash
./build.sh          # 产出 Verity-MiMo-Direct.jar
```

## 安装

1. `Verity-MiMo-Direct.jar` 放进 mods（需 Forge 47.x、YACL、Verity）。
2. 打开 Verity 配置界面 → 新增的 **Xiaomi MiMo** 分类填 `MiMo API Key`、选音色（默认 `苏打` 中文男声）。
3. 在 **Voice Settings → Text To Speech Provider** 和 **Speech Recognition → Recognition Provider** 里选 **MIMO**。
   （兜底：也可以在 Xiaomi MiMo 分类里开 `Enable MiMo TTS` / `Enable MiMo ASR` 开关。）
4. 获得 MiMo key：https://platform.xiaomimimo.com/#/console/api-keys
