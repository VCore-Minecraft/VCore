package de.verdox.vcore.api.core.listener;

/**
 * @param <T> Platform plugin
 */
public abstract class VCoreListener<T> {
    private final T platformPlugin;

    public VCoreListener(T platformPlugin) {
        this.platformPlugin = platformPlugin;
        registerListener(platformPlugin);
    }

    public T getPlatformPlugin() {
        return platformPlugin;
    }

    protected abstract void registerListener(T platformPlugin);
    protected abstract void unRegisterListener(T platformPlugin);
}
