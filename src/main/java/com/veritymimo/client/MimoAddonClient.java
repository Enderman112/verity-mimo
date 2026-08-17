package com.veritymimo.client;

import com.veritymimo.config.MimoAddonConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = "verity_mimo", bus = Mod.EventBusSubscriber.Bus.MOD, value = {Dist.CLIENT})
public final class MimoAddonClient {
    private MimoAddonClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(MimoAddonClient::createConfigScreen));
    }

    public static Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.m_237113_("Verity MiMo Addon"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.m_237113_("MiMo"));
        general.addEntry(entryBuilder.startBooleanToggle(Component.m_237113_("Enable MiMo TTS"), MimoAddonConfig.ENABLE_MIMO_TTS.get().booleanValue()).setDefaultValue(true).setTooltip(Component.m_237113_("When on and Smiley's Better Voice is in use, MiMo replaces the Cartesia/Fish provider for Verity's voice.")).setSaveConsumer(arg_0 -> ((ForgeConfigSpec.BooleanValue) MimoAddonConfig.ENABLE_MIMO_TTS).set(arg_0)).build());
        general.addEntry(entryBuilder.startBooleanToggle(Component.m_237113_("Enable MiMo ASR"), MimoAddonConfig.ENABLE_MIMO_ASR.get().booleanValue()).setDefaultValue(true).setTooltip(Component.m_237113_("When on, Verity's speech recognition uses MiMo (recognizes Chinese/English) instead of the local/Groq engine.")).setSaveConsumer(arg_0 -> ((ForgeConfigSpec.BooleanValue) MimoAddonConfig.ENABLE_MIMO_ASR).set(arg_0)).build());
        general.addEntry(entryBuilder.startStrField(Component.m_237113_("MiMo API Key"), MimoAddonConfig.MIMO_API_KEY.get()).setDefaultValue("").setTooltip(Component.m_237113_("API key from https://platform.xiaomimimo.com/#/console/api-keys (sk-...)")).setSaveConsumer(arg_0 -> MimoAddonConfig.MIMO_API_KEY.set(arg_0)).build());
        general.addEntry(entryBuilder.startStrField(Component.m_237113_("MiMo Base URL (optional)"), MimoAddonConfig.MIMO_BASE_URL.get()).setDefaultValue("").setTooltip(Component.m_237113_("Leave empty for https://api.xiaomimimo.com/v1. Token Plan users: https://token-plan-cn.xiaomimimo.com/v1")).setSaveConsumer(arg_0 -> MimoAddonConfig.MIMO_BASE_URL.set(arg_0)).build());
        general.addEntry(entryBuilder.startStrField(Component.m_237113_("MiMo TTS Voice"), MimoAddonConfig.MIMO_TTS_VOICE.get()).setDefaultValue("\u82cf\u6253").setTooltip(Component.m_237113_("Chinese male: \u82cf\u6253 (Soda) / \u767d\u6866 (Baihua). Chinese female: \u51b0\u7cd6 (Bingtang) / \u8309\u8389 (Moliy). English: Mia, Chloe, Milo, Dean.")).setSaveConsumer(arg_0 -> MimoAddonConfig.MIMO_TTS_VOICE.set(arg_0)).build());
        general.addEntry(entryBuilder.startDoubleField(Component.m_237113_("MiMo TTS Speed"), MimoAddonConfig.MIMO_TTS_SPEED.get().doubleValue()).setDefaultValue(1.0).setMin(0.5).setMax(2.0).setTooltip(Component.m_237113_("Speech speed 0.5x\u20132.0x (passed to MiMo as a style hint)")).setSaveConsumer(arg_0 -> ((ForgeConfigSpec.DoubleValue) MimoAddonConfig.MIMO_TTS_SPEED).set(arg_0)).build());
        general.addEntry(entryBuilder.startStrField(Component.m_237113_("MiMo TTS Style (optional)"), MimoAddonConfig.MIMO_TTS_STYLE.get()).setDefaultValue("").setTooltip(Component.m_237113_("Natural-language speaking style. Leave empty to auto-generate from Verity's emotion.")).setSaveConsumer(arg_0 -> MimoAddonConfig.MIMO_TTS_STYLE.set(arg_0)).build());
        general.addEntry(entryBuilder.startStrField(Component.m_237113_("MiMo ASR Language"), MimoAddonConfig.MIMO_STT_LANGUAGE.get()).setDefaultValue("zh").setTooltip(Component.m_237113_("zh (Chinese), en (English) or auto")).setSaveConsumer(arg_0 -> MimoAddonConfig.MIMO_STT_LANGUAGE.set(arg_0)).build());
        general.addEntry(entryBuilder.startTextDescription(Component.m_237113_("After enabling MiMo providers in this addon, open Verity's config (Speech Recognition \u2192 Recognition Provider) and Smiley's Better Voice's config (TTS Provider) and pick \"MIMO\". Settings entered here apply to both.")).build());
        builder.setSavingRunnable(() -> MimoAddonConfig.SPEC.save());
        return builder.build();
    }
}
