package net.minecraftforge.client;

import java.util.function.Function;
import net.minecraft.client.gui.screens.Screen;

public class ConfigScreenHandler {
    public static class ConfigScreenFactory {
        public ConfigScreenFactory(Function<Screen, Screen> screenFunction) {}
    }
}
