package net.minecraftforge.common;

import java.util.function.Supplier;
import net.minecraftforge.fml.config.IConfigSpec;

public class ForgeConfigSpec implements IConfigSpec<ForgeConfigSpec> {
    public static class Builder {
        public <T> ConfigValue<T> define(String path, T defaultValue) { return null; }
        public BooleanValue define(String path, boolean defaultValue) { return null; }
        public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) { return null; }
        public DoubleValue defineInRange(String path, double defaultValue, double min, double max) { return null; }
        public IntValue defineInRange(String path, int defaultValue, int min, int max) { return null; }
        public Builder comment(String comment) { return this; }
        public Builder comment(String... comment) { return this; }
        public Builder push(String path) { return this; }
        public Builder pop() { return this; }
        public ForgeConfigSpec build() { return null; }
    }

    public static class ConfigValue<T> implements Supplier<T> {
        public T get() { return null; }
        public void set(T value) {}
        public void save() {}
    }

    public static class BooleanValue extends ConfigValue<Boolean> {}

    public static class IntValue extends ConfigValue<Integer> {}

    public static class LongValue extends ConfigValue<Long> {}

    public static class DoubleValue extends ConfigValue<Double> {}

    public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {}

    public void save() {}
}
