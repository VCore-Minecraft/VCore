package de.verdox.vcore.impl.proxy.listener;

import de.verdox.vcore.api.core.listener.VCoreListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public class VCoreWaterfallListener extends VCoreListener<Plugin> implements Listener {
    public VCoreWaterfallListener(Plugin platformPlugin) {
        super(platformPlugin);
    }

    @Override
    protected void registerListener(Plugin platformPlugin) {
        ProxyServer.getInstance().getPluginManager().registerListener(platformPlugin, this);
    }

    @Override
    protected void unRegisterListener(Plugin platformPlugin) {
        ProxyServer.getInstance().getPluginManager().unregisterListener(this);
    }
}
