package de.verdox.vcore.impl.gameserver.paper.listener;

import de.verdox.vcore.api.core.listener.PlayerDataLoader;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vcore.impl.gameserver.paper.util.PaperUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerListener extends VCorePaperListener implements PlayerDataLoader {

    public PlayerListener(JavaPlugin platformPlugin) {
        super(platformPlugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerJoin(PlayerJoinEvent e) {
        ServerLocation serverLocation = new ServerLocation(VCoreNetwork
                .getInstance()
                .getAsServer(), PaperUtil.fromBukkitLocation(e
                .getPlayer()
                .getLocation()));

        onJoin(e.getPlayer().getUniqueId(), e.getPlayer().getName(), serverLocation);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerQuit(PlayerQuitEvent e) {
        var removeFromCache = (VCoreNetwork
                .getInstance()
                .getPlatform() instanceof GameServerPlatform gameServerPlatform) && !gameServerPlatform.isConnectedToProxyNetwork();
        onQuit(e.getPlayer().getUniqueId(), e.getPlayer().getName(), removeFromCache);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerKick(PlayerKickEvent e) {
        var removeFromCache = (VCoreNetwork
                .getInstance()
                .getPlatform() instanceof GameServerPlatform gameServerPlatform) && !gameServerPlatform.isConnectedToProxyNetwork();
        onQuit(e.getPlayer().getUniqueId(), e.getPlayer().getName(), removeFromCache);
    }
}
