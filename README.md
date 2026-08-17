# Verity MiMo

给 **Verity JE（1.20.1）** 增加小米 MiMo 语音（TTS 合成 + ASR 语音识别）的两个附加 mod（纯 Mixin 方案，**不改动原 mod**）。

> 两个分支对应两种接入方式，二选一。获取 MiMo API key：https://platform.xiaomimimo.com/#/console/api-keys

## 分支

| 分支 | 依赖 | TTS 接入 | ASR 接入 | 设置入口 | 特点 |
|---|---|---|---|---|---|
| [`addon`](https://github.com/Enderman112/verity-mimo/tree/addon) | Verity + Smiley's Better Voice | BetterVoice 播放管线 | Verity | addon 自己的配置界面 | 带文字流同步、口型（BetterVisuals）、打断管理 |
| [`direct`](https://github.com/Enderman112/verity-mimo/tree/direct) | 仅 Verity | verity 自己的 `AiAPI.playTTS` | Verity | **Verity 配置菜单**里的 "Xiaomi MiMo" 分类 | 最简、不装 BetterVoice，带 3D 空间音效 |

### 怎么选

- 想要"角色扮演感"完整（嘴型、聊天逐字同步、说话打断管理）→ **addon**
- 想要最简、少装一个 mod → **direct**

两个分支里都包含完整源码、构建脚本和编译好的 jar。

## 用法（以 direct 为例）

1. 把 `Verity-MiMo-Direct.jar` 放进 mods（需 Forge 47.x、YACL、Verity 6.0.0-beta.5+）。
2. 打开 Verity 配置界面 → **Xiaomi MiMo** 分类：填 MiMo API Key，打开 `Enable MiMo TTS` / `Enable MiMo ASR`，选音色（默认 `苏打` 中文男声）、ASR 语言（`zh`）。
3. 保存即可。MiMo 接管 TTS 和 ASR。

addon 分支的用法见该分支 README。

## 构建

任一分支内执行：

```bash
./build.sh     # 需要 JDK 17+，编译期依赖在 build/lib/
```

## 说明

- 编译产物 / jar 不含任何原 mod 代码，仅本仓库代码。
- 音色列表：中文男 `苏打` / `白桦`，中文女 `冰糖` / `茉莉`，英文 `Mia` / `Chloe` / `Milo` / `Dean`，或 `mimo_default`。
- MiMo 按量付费 base URL 默认 `https://api.xiaomimimo.com/v1`；Token Plan 用户填 `https://token-plan-cn.xiaomimimo.com/v1`。
