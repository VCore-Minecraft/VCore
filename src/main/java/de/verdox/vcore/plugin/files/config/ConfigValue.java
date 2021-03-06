/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.plugin.files.config;

/**
 * @version 1.0
 * @Author: Lukas Jonsson (Verdox)
 * @date 21.08.2021 21:56
 */
public class ConfigValue<T> {
    protected final VCoreYAMLConfig config;
    protected final String path;
    protected final T defaultValue;

    public ConfigValue(VCoreYAMLConfig config, String path, T defaultValue) {
        this.config = config;
        this.path = path;
        this.defaultValue = defaultValue;
    }

    public void addDefault() {
        config.getConfig().addDefault(path, defaultValue);
    }

    public T readValue() {
        if (!config.getConfig().isSet(path)) {
            setValue(defaultValue);
            return defaultValue;
        }
        return (T) config.getConfig().get(path);
    }

    public void setValue(T value) {
        config.getConfig().set(path, value);
        config.save();
    }
}