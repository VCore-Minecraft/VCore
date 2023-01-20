package de.verdox.vcore.impl.gameserver.paper;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vcore.impl.gameserver.paper.util.PaperUtil;
import de.verdox.vpipeline.api.pipeline.enums.PreloadStrategy;
import de.verdox.vpipeline.api.util.AnnotationResolver;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent e) {
        ServerLocation serverLocation = new ServerLocation(VCoreNetwork
                .getInstance()
                .getAsServer(), PaperUtil.fromBukkitLocation(e
                .getPlayer()
                .getLocation()));

        VCoreNetwork
                .getInstance()
                .addPlayerToCache(e
                        .getPlayer()
                        .getUniqueId(), e
                        .getPlayer()
                        .getName(), serverLocation);
        loadNeededPlayerData(e.getPlayer());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent e) {
        clearCacheFromUnusedPlayerData(e.getPlayer());
        if (!(VCoreNetwork
                .getInstance()
                .getPlatform() instanceof GameServerPlatform gameServerPlatform))
            return;
        if (gameServerPlatform.isConnectedToProxyNetwork())
            return;
        VCoreNetwork
                .getInstance()
                .removePlayerFromCache(e
                        .getPlayer()
                        .getUniqueId());
    }

    @EventHandler
    public void playerKick(PlayerKickEvent e) {
        clearCacheFromUnusedPlayerData(e.getPlayer());
        if (!(VCoreNetwork
                .getInstance()
                .getPlatform() instanceof GameServerPlatform gameServerPlatform))
            return;
        if (gameServerPlatform.isConnectedToProxyNetwork())
            return;
        VCoreNetwork
                .getInstance()
                .removePlayerFromCache(e
                        .getPlayer()
                        .getUniqueId());
    }

    private void clearCacheFromUnusedPlayerData(Player player) {
        VCoreNetwork
                .getInstance()
                .getPlayerDataClasses()
                .stream()
                .filter(aClass -> AnnotationResolver
                        .getDataProperties(aClass)
                        .preloadStrategy()
                        .equals(PreloadStrategy.LOAD_ON_NEED))
                .forEach(aClass -> {
                    VCoreNetwork
                            .getInstance()
                            .getPipeline()
                            .saveAndRemoveFromLocalCache(aClass, player.getUniqueId());
                });
    }

    private void loadNeededPlayerData(Player player) {
        VCoreNetwork
                .getInstance()
                .getPlayerDataClasses()
                .stream()
                .filter(aClass -> AnnotationResolver
                        .getDataProperties(aClass)
                        .preloadStrategy()
                        .equals(PreloadStrategy.LOAD_ON_NEED))
                .forEach(aClass -> VCoreNetwork
                        .getInstance()
                        .getPipeline()
                        .loadOrCreate(aClass, player.getUniqueId()));
    }
}
