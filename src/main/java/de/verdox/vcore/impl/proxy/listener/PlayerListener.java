package de.verdox.vcore.impl.proxy.listener;

import de.verdox.vcore.api.core.listener.PlayerDataLoader;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class PlayerListener extends VCoreWaterfallListener implements PlayerDataLoader {
    public PlayerListener(Plugin platformPlugin) {
        super(platformPlugin);
    }

    @EventHandler
    public void onQuit(PlayerDisconnectEvent e) {
        onQuit(e.getPlayer().getUniqueId(), e.getPlayer().getName(), true);
    }

    @EventHandler
    public void onJoin(PostLoginEvent e) {
        onQuit(e.getPlayer().getUniqueId(), e.getPlayer().getName(), true);
    }
}
