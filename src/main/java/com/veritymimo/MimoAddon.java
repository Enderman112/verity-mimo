package com.veritymimo;

import com.veritymimo.config.MimoAddonConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

@Mod(value = "verity_mimo")
public class MimoAddon {
    public MimoAddon() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, (IConfigSpec) MimoAddonConfig.SPEC, "verity_mimo-common.toml");
    }
}
