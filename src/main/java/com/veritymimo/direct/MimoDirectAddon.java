package com.veritymimo.direct;

import com.veritymimo.direct.config.MimoDirectConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

@Mod(value = "verity_mimo_direct")
public class MimoDirectAddon {
    public MimoDirectAddon() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec) MimoDirectConfig.SPEC, "verity_mimo_direct-common.toml");
    }
}
