package varmite.verity;

import net.minecraftforge.common.ForgeConfigSpec;
import varmite.verity.types.STTProvider;
import varmite.verity.types.TTSProvider;

public class VerityConfig {
    public static ForgeConfigSpec.EnumValue<TTSProvider> TTS_PROVIDER;
    public static ForgeConfigSpec.EnumValue<STTProvider> STT_PROVIDER;
    public static ForgeConfigSpec.BooleanValue USE_TTS;
}
