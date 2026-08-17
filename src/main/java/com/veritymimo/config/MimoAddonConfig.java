package com.veritymimo.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class MimoAddonConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MIMO_TTS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_MIMO_ASR;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_API_KEY;
    public static final ForgeConfigSpec.EnumValue<MimoPlan> MIMO_PLAN;
    public static final ForgeConfigSpec.EnumValue<MimoCluster> MIMO_CLUSTER;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_BASE_URL;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_TTS_VOICE;
    public static final ForgeConfigSpec.DoubleValue MIMO_TTS_SPEED;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_TTS_STYLE;
    public static final ForgeConfigSpec.ConfigValue<String> MIMO_STT_LANGUAGE;

    public enum MimoPlan {
        PAYG("\u6309\u91cf\u4ed8\u8d39"),
        TOKEN_PLAN("Token Plan");

        private final String displayName;

        MimoPlan(String displayName) {
            this.displayName = displayName;
        }

        public String displayName() {
            return this.displayName;
        }
    }

    public enum MimoCluster {
        CN("\u4e2d\u56fd", "cn"),
        SINGAPORE("\u65b0\u52a0\u5761", "sgp"),
        AMSTERDAM("\u6b27\u6d32", "ams");

        private final String displayName;
        private final String code;

        MimoCluster(String displayName, String code) {
            this.displayName = displayName;
            this.code = code;
        }

        public String displayName() {
            return this.displayName;
        }

        public String code() {
            return this.code;
        }
    }

    private MimoAddonConfig() {
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        ENABLE_MIMO_TTS = builder.comment("Enable Xiaomi MiMo TTS. When on and Smiley's Better Voice is used, MiMo replaces the Cartesia/Fish provider.").define("enableMimoTts", true);
        ENABLE_MIMO_ASR = builder.comment("Enable Xiaomi MiMo Speech Recognition (Chinese/English). When on, Verity's STT uses MiMo instead of the local/Groq engine.").define("enableMimoAsr", true);
        MIMO_API_KEY = builder.comment("Xiaomi MiMo API key. Pay-as-you-go keys start with sk-, Token Plan keys with tp- (https://platform.xiaomimimo.com/#/console/api-keys or Token Plan page).").define("mimoApiKey", "");
        MIMO_PLAN = builder.comment("MiMo account type: PAYG (\u6309\u91cf\u4ed8\u8d39, sk- keys) or TOKEN_PLAN (tp- keys).").defineEnum("mimoPlan", MimoPlan.PAYG);
        MIMO_CLUSTER = builder.comment("Token Plan server cluster (only used when plan = TOKEN_PLAN): CN (\u4e2d\u56fd), SINGAPORE (\u65b0\u52a0\u5761) or AMSTERDAM (\u6b27\u6d32).").defineEnum("mimoCluster", MimoCluster.CN);
        MIMO_BASE_URL = builder.comment("Optional custom MiMo base URL (OpenAI-compatible). Leave empty to auto-resolve from plan + cluster.").define("mimoBaseUrl", "");
        MIMO_TTS_VOICE = builder.comment("MiMo TTS preset voice. Chinese male: \u82cf\u6253 (Soda) / \u767d\u6866 (Baihua). Chinese female: \u51b0\u7cd6 (Bingtang) / \u8309\u8389 (Moliy). English: Mia, Chloe, Milo, Dean. Or mimo_default.").define("mimoTtsVoice", "\u82cf\u6253");
        MIMO_TTS_SPEED = builder.comment("MiMo TTS speech speed (0.5 to 2.0). Passed as a natural-language style hint.").defineInRange("mimoTtsSpeed", 1.0, 0.5, 2.0);
        MIMO_TTS_STYLE = builder.comment("Optional natural-language speaking style for MiMo TTS, e.g. \u201c\u5e74\u8f7b\u5973\u6027\uff0c\u6e29\u67d4\u6025\u4eba\u7684\u8bed\u6c14\u201d. Leave empty to auto-generate from Verity's emotion.").define("mimoTtsStyle", "");
        MIMO_STT_LANGUAGE = builder.comment("MiMo ASR language: zh (Chinese), en (English) or auto.").define("mimoSttLanguage", "zh");
        SPEC = builder.build();
    }

    public static String resolveApiUrl() {
        String custom = MIMO_BASE_URL.get();
        if (custom != null && !custom.isBlank()) {
            String trimmed = custom.trim();
            while (trimmed.endsWith("/")) {
                trimmed = trimmed.substring(0, trimmed.length() - 1);
            }
            return trimmed;
        }
        if (MIMO_PLAN.get() == MimoPlan.TOKEN_PLAN) {
            return "https://token-plan-" + MIMO_CLUSTER.get().code() + ".xiaomimimo.com/v1";
        }
        return "https://api.xiaomimimo.com/v1";
    }
}
