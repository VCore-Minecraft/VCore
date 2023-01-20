package de.verdox.vcore.api.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @param <T> Platform plugin
 */
public class PlatformDependentImplementation<T> {
    protected final T plugin;

    public PlatformDependentImplementation(@NotNull T plugin) {
        this.plugin = plugin;
        Objects.requireNonNull(plugin);
    }

    public T getPlugin() {
        return plugin;
    }
}
