package com.veritymimo.direct.mixin;

import com.veritymimo.direct.config.MimoDirectConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.DoubleSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.StringControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "varmite.verity.VerityConfigUI", remap = false)
public abstract class VerityConfigUIMixin {
    @Redirect(
        method = "createYACLScreen",
        at = @At(value = "INVOKE", target = "Ldev/isxander/yacl3/api/YetAnotherConfigLib$Builder;category(Ldev/isxander/yacl3/api/ConfigCategory;)Ldev/isxander/yacl3/api/YetAnotherConfigLib$Builder;"),
        remap = false, require = 0)
    private static YetAnotherConfigLib.Builder mimo$appendCategory(YetAnotherConfigLib.Builder builder, ConfigCategory category) {
        try {
            YetAnotherConfigLib.Builder b = builder.category(category);
            String name = category.name().getString();
            if ("Advanced".equals(name)) {
                b = b.category(VerityConfigUIMixin.mimoCategory());
            }
            return b;
        }
        catch (Throwable t) {
            t.printStackTrace();
            return builder.category(category);
        }
    }

    private static ConfigCategory mimoCategory() {
        ConfigCategory.Builder b = ConfigCategory.createBuilder().name(Component.m_237113_("Xiaomi MiMo"));
        b = b.option(Option.<String>createBuilder().name(Component.m_237113_("MiMo API Key")).description(OptionDescription.of(Component.m_237113_("API key from https://platform.xiaomimimo.com/#/console/api-keys"))).binding("", MimoDirectConfig.MIMO_API_KEY, arg_0 -> MimoDirectConfig.MIMO_API_KEY.set(arg_0)).controller(StringControllerBuilder::create).build());
        b = b.option(Option.<Boolean>createBuilder().name(Component.m_237113_("Enable MiMo TTS")).description(OptionDescription.of(Component.m_237113_("Fallback activation. Normal way: pick MIMO in Verity's Text To Speech Provider."))).binding(false, MimoDirectConfig.ENABLE_MIMO_TTS, arg_0 -> MimoDirectConfig.ENABLE_MIMO_TTS.set(arg_0)).controller(TickBoxControllerBuilder::create).build());
        b = b.option(Option.<Boolean>createBuilder().name(Component.m_237113_("Enable MiMo ASR")).description(OptionDescription.of(Component.m_237113_("Fallback activation. Normal way: pick MIMO in Verity's Recognition Provider."))).binding(false, MimoDirectConfig.ENABLE_MIMO_ASR, arg_0 -> MimoDirectConfig.ENABLE_MIMO_ASR.set(arg_0)).controller(TickBoxControllerBuilder::create).build());
        b = b.option(Option.<String>createBuilder().name(Component.m_237113_("MiMo TTS Voice")).description(OptionDescription.of(Component.m_237113_("Chinese male: \u82cf\u6253 (Soda) / \u767d\u6866 (Baihua). Chinese female: \u51b0\u7cd6 (Bingtang) / \u8309\u8389 (Moliy). English: Mia, Chloe, Milo, Dean."))).binding("\u82cf\u6253", MimoDirectConfig.MIMO_TTS_VOICE, arg_0 -> MimoDirectConfig.MIMO_TTS_VOICE.set(arg_0)).controller(StringControllerBuilder::create).build());
        b = b.option(Option.<Double>createBuilder().name(Component.m_237113_("MiMo TTS Speed")).description(OptionDescription.of(Component.m_237113_("Speech speed 0.5x\u20132.0x"))).binding(1.0, MimoDirectConfig.MIMO_TTS_SPEED, arg_0 -> MimoDirectConfig.MIMO_TTS_SPEED.set(arg_0)).controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.5, 2.0).step(0.05)).build());
        b = b.option(Option.<String>createBuilder().name(Component.m_237113_("MiMo TTS Style (optional)")).description(OptionDescription.of(Component.m_237113_("Natural-language speaking style. Empty = auto from Verity's expression."))).binding("", MimoDirectConfig.MIMO_TTS_STYLE, arg_0 -> MimoDirectConfig.MIMO_TTS_STYLE.set(arg_0)).controller(StringControllerBuilder::create).build());
        b = b.option(Option.<String>createBuilder().name(Component.m_237113_("MiMo ASR Language")).description(OptionDescription.of(Component.m_237113_("zh / en / auto"))).binding("zh", MimoDirectConfig.MIMO_STT_LANGUAGE, arg_0 -> MimoDirectConfig.MIMO_STT_LANGUAGE.set(arg_0)).controller(StringControllerBuilder::create).build());
        b = b.option(Option.<String>createBuilder().name(Component.m_237113_("MiMo Base URL (optional)")).description(OptionDescription.of(Component.m_237113_("Empty = https://api.xiaomimimo.com/v1. Token Plan: https://token-plan-cn.xiaomimimo.com/v1"))).binding("", MimoDirectConfig.MIMO_BASE_URL, arg_0 -> MimoDirectConfig.MIMO_BASE_URL.set(arg_0)).controller(StringControllerBuilder::create).build());
        return b.build();
    }
}
