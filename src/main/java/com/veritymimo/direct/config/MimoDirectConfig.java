package com.veritymimo.direct.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MimoDirectConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MIMO_TTS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MIMO_ASR;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_BASE_URL;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_TTS_VOICE;
    public static final ForgeConfigSpec.DoubleValue MIMO_TTS_SPEED;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_TTS_STYLE;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_STT_LANGUAGE;

    private MimoDirectConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLE_MIMO_TTS = builder.comment("Enable Xiaomi MiMo TTS. Also enable by picking MIMO in Verity's Text To Speech Provider.").define("enableMimoTts", false);
        ENABLE_MIMO_ASR = builder.comment("Enable Xiaomi MiMo Speech Recognition. Also enable by picking MIMO in Verity's Recognition Provider.").define("enableMimoAsr", false);
        MIMO_API_KEY = builder.comment("Xiaomi MiMo API key (https://platform.xiaomimimo.com/#/console/api-keys). Format: sk-...").define("mimoApiKey", "");
        MIMO_BASE_URL = builder.comment("MiMo OpenAI-compatible base URL. Leave empty for https://api.xiaomimimo.com/v1 (Token Plan: https://token-plan-cn.xiaomimimo.com/v1).").define("mimoBaseUrl", "");
        MIMO_TTS_VOICE = builder.comment("MiMo TTS preset voice. Chinese male: \u82cf\u6253 (Soda) / \u767d\u6866 (Baihua). Chinese female: \u51b0\u7cd6 (Bingtang) / \u8309\u8389 (Moliy). English: Mia, Chloe, Milo, Dean. Or mimo_default.").define("mimoTtsVoice", "\u82cf\u6253");
        MIMO_TTS_SPEED = builder.comment("MiMo TTS speech speed (0.5 to 2.0).").defineInRange("mimoTtsSpeed", 1.0, 0.5, 2.0);
        MIMO_TTS_STYLE = builder.comment("Optional natural-language speaking style for MiMo TTS. Leave empty to auto-generate from Verity's expression.").define("mimoTtsStyle", "");
        MIMO_STT_LANGUAGE = builder.comment("MiMo ASR language: zh (Chinese), en (English) or auto.").define("mimoSttLanguage", "zh");
        SPEC = builder.build();
    }
}
