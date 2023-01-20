package de.verdox.vcore.api.core;

import org.jetbrains.annotations.NotNull;

public class PlatformDependentImplementationBuilder<T> {
    private @NotNull T plugin;

    public PlatformDependentImplementationBuilder setPlugin(@NotNull T plugin) {
        this.plugin = plugin;
        return this;
    }

    public PlatformDependentImplementation createPlatformDependentImplementation() {
        return new PlatformDependentImplementation(plugin);
    }
}