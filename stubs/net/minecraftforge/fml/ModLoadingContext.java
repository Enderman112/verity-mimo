package net.minecraftforge.fml;

import java.util.function.Supplier;
import net.minecraftforge.fml.config.IConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

public class ModLoadingContext {
    public static ModLoadingContext get() { return null; }
    public void registerConfig(ModConfig.Type type, IConfigSpec spec) {}
    public void registerConfig(ModConfig.Type type, IConfigSpec spec, String fileName) {}
    public void registerExtensionPoint(Class<?> type, Supplier<?> supplier) {}
}
