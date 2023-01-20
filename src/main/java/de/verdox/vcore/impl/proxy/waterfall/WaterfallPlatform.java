package de.verdox.vcore.impl.proxy.waterfall;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.VCoreServer;
import de.verdox.vcore.api.core.network.platform.ProxyPlatform;
import net.md_5.bungee.api.ProxyServer;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WaterfallPlatform implements ProxyPlatform {
    @Override
    public CompletableFuture<Boolean> isPlayerOnline(@NotNull UUID playerUUID) {
        return CompletableFuture.completedFuture(ProxyServer.getInstance().getPlayer(playerUUID) != null);
    }

    @Override
    public CompletableFuture<InetSocketAddress> getPlayerAddress(@NotNull UUID playerUUID) {
        return ifPlayerOnlineCallback(playerUUID, () -> ProxyServer
                .getInstance()
                .getPlayer(playerUUID)
                .getAddress(), null);
    }

    @Override
    public boolean isConnectedToProxyNetwork() {
        return true;
    }

    @Override
    public void shutdown() {
        ProxyServer.getInstance().stop();
    }

    @Override
    public CompletableFuture<Boolean> sendToServer(@NotNull UUID playerUUID, @NotNull VCoreServer vCoreServer) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            var serverName = vCoreServer.getName().join();
            var server = ProxyServer.getInstance().getServerInfo(serverName);
            ProxyServer.getInstance().getPlayer(playerUUID).connect(server);
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<VCoreServer> getServer(@NotNull UUID playerUUID) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            var serverName = ProxyServer.getInstance().getPlayer(playerUUID).getServer().getInfo().getName();
            return VCoreNetwork.getInstance().getServer(serverName);
        }, null);
    }

    @Override
    public CompletableFuture<Boolean> kickPlayer(@NotNull UUID playerUUID, @NotNull String kickMessage) {

        return ifPlayerOnlineCallback(playerUUID, () -> {
            ProxyServer.getInstance().getPlayer(playerUUID).disconnect(kickMessage);
            return true;
        }, false);
    }
}
