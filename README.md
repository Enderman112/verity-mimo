# Verity MiMo Addon

一个独立的 Forge 附加 mod（单 jar），给 **Verity 6.0.0-beta.5** 和 **Smiley's Better Voice 6.0.0** 增加小米 MiMo 的 TTS 与 ASR，**不改动这两个 mod 本身**。

## 原理

- 纯 Mixin 方案，两个原 mod 保持原样，版权安全。
- 通过 `EnumHelper.addEnum` 给原 mod 的 `TtsProvider` / `STTProvider` 枚举动态增加 `MIMO` 选项（失败会自动降级，不影响游戏）。
- Mixin 钩子：
  - TTS：`BetterVoiceTtsService.play`（HEAD，cancellable）——选中 MIMO 时转走本 mod 的 `MimoTtsService`
  - ASR：`AiAPI.transcribeAudio`（HEAD，cancellable）——选中 MIMO 时转走本 mod 的 `MimoSttService`

## 构建

```bash
./build.sh          # 产出 Verity-MiMo-Addon.jar
```

需要 JDK 17+。编译期依赖在 `build/lib/`（gson / cloth-config / mixin / annotations / guava），运行期由游戏提供。

## 安装

1. 把 `Verity-MiMo-Addon.jar` 放进 mods 目录（需已装 Forge 47.x、Cloth Config、Verity、Smiley's Better Voice）。
2. 首次进入后配置 `config/verity_mimo-common.toml`（或在 Mods 列表里打开本 mod 的 Config 界面）：
   - `mimoApiKey`：MiMo API key（https://platform.xiaomimimo.com/#/console/api-keys）
   - `mimoVoiceMode`：`MIMO_PRESET`（默认，用下面的预置音色）或 `VERITY_CLONE`（**克隆 Verity 游戏内的 intro 语音**，走 `mimo-v2.5-tts-voiceclone`，参考音频已内置于 mod）
   - `mimoTtsVoice`：预置音色，默认 `苏打`（中文男声）；可用 `白桦`、`冰糖`、`茉莉`、`Mia`、`Chloe`、`Milo`、`Dean`（仅 MIMO_PRESET 模式生效）
   - `mimoSttLanguage`：`zh` / `en` / `auto`
3. 二选一启用（原生方式：在 Verity/Smiley's Better Voice 配置里把对应 Provider 选成 **MIMO**；或兜底：打开本 mod 的 `enableMimoTts` / `enableMimoAsr` 开关）。
