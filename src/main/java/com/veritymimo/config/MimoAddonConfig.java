package com.veritymimo.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MimoAddonConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MIMO_TTS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MIMO_ASR;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_API_KEY;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_BASE_URL;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_TTS_VOICE;
    public static final ForgeConfigSpec.DoubleValue MIMO_TTS_SPEED;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_TTS_STYLE;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_STT_LANGUAGE;

    private MimoAddonConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLE_MIMO_TTS = builder.comment("Enable Xiaomi MiMo TTS. When on and Smiley's Better Voice is used, MiMo replaces the Cartesia/Fish provider.").define("enableMimoTts", true);
        ENABLE_MIMO_ASR = builder.comment("Enable Xiaomi MiMo Speech Recognition (Chinese/English). When on, Verity's STT uses MiMo instead of the local/Groq engine.").define("enableMimoAsr", true);
        MIMO_API_KEY = builder.comment("Xiaomi MiMo API key (https://platform.xiaomimimo.com/#/console/api-keys). Format: sk-...").define("mimoApiKey", "");
        MIMO_BASE_URL = builder.comment("MiMo OpenAI-compatible base URL. Leave empty for the default https://api.xiaomimimo.com/v1 (Token Plan users: https://token-plan-cn.xiaomimimo.com/v1).").define("mimoBaseUrl", "");
        MIMO_TTS_VOICE = builder.comment("MiMo TTS preset voice. Chinese male: \u82cf\u6253 (Soda) or \u767d\u6866 (Baihua). Chinese female: \u51b0\u7cd6 (Bingtang) or \u8309\u8389 (Moliy). English: Mia, Chloe, Milo, Dean. Or mimo_default.").define("mimoTtsVoice", "\u82cf\u6253");
        MIMO_TTS_SPEED = builder.comment("MiMo TTS speech speed (0.5 to 2.0). Passed as a natural-language style hint.").defineInRange("mimoTtsSpeed", 1.0, 0.5, 2.0);
        MIMO_TTS_STYLE = builder.comment("Optional natural-language speaking style for MiMo TTS, e.g. \u201c\u5e74\u8f7b\u5973\u6027\uff0c\u6e29\u67d4\u6025\u4eba\u7684\u8bed\u6c14\u201d. Leave empty to auto-generate from Verity's emotion.").define("mimoTtsStyle", "");
        MIMO_STT_LANGUAGE = builder.comment("MiMo ASR language: zh (Chinese), en (English) or auto.").define("mimoSttLanguage", "zh");
        SPEC = builder.build();
    }
}
