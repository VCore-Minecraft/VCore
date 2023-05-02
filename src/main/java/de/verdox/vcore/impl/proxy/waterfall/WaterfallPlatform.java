package de.verdox.vcore.impl.proxy.waterfall;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.VCoreServer;
import de.verdox.vcore.api.core.network.platform.ProxyPlatform;
import de.verdox.vcore.impl.proxy.listener.VCoreWaterfallListener;
import de.verdox.vpipeline.api.NetworkLogger;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class WaterfallPlatform implements ProxyPlatform {

    private final VCoreWaterfall vCoreWaterfall;

    WaterfallPlatform(VCoreWaterfall vCoreWaterfall) {
        this.vCoreWaterfall = vCoreWaterfall;
    }

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
        return performServerActionThreadSafe(() -> {

            if (ProxyServer.getInstance().getPlayer(playerUUID) == null)
                return false;
            var serverName = vCoreServer.getName().join();
            var proxiedPlayer = ProxyServer.getInstance().getPlayer(playerUUID);
            if (proxiedPlayer.getServer().getInfo().getName().equals(serverName))
                return true;

            var server = ProxyServer.getInstance().getServerInfo(serverName);
            var completed = new CompletableFuture<Boolean>();
            proxiedPlayer.connect(server, (result, error) -> {
                if (error == null)
                    completed.complete(result);
                else
                    completed.complete(false);
            });
            return completed.join();
        });
    }

    @Override
    public CompletableFuture<VCoreServer> getServer(@NotNull UUID playerUUID) {
        return performServerActionThreadSafe(() -> {
            if (ProxyServer.getInstance().getPlayer(playerUUID) == null)
                return null;
            var serverName = ProxyServer.getInstance().getPlayer(playerUUID).getServer().getInfo().getName();
            return VCoreNetwork.getInstance().getServer(serverName);
        });
    }

    @Override
    public CompletableFuture<Boolean> kickPlayer(@NotNull UUID playerUUID, @NotNull String kickMessage) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            ProxyServer.getInstance().getPlayer(playerUUID).disconnect(kickMessage);
            NetworkLogger.info("Kicked player: " + playerUUID);
            return true;
        }, false);
    }

    @Override
    public <R> CompletableFuture<R> performServerActionThreadSafe(Supplier<R> taskToRun) {
        return CompletableFuture.completedFuture(taskToRun.get());
    }

    @Override
    public CompletableFuture<Void> waitForPlayerReady(@NotNull UUID playerUUID) {
        var future = new CompletableFuture<Void>();

        ProxyServer.getInstance().getScheduler().schedule(vCoreWaterfall, () -> {

            if (Bukkit.getPlayer(playerUUID) != null) {
                future.complete(null);
                return;
            }

            new VCoreWaterfallListener(vCoreWaterfall) {
                @EventHandler
                public void onJoin(PostLoginEvent e) {
                    if (e.getPlayer().getUniqueId().equals(playerUUID)) {
                        future.complete(null);
                        ProxyServer.getInstance().getPluginManager().unregisterListener(this);
                    }
                }

                @EventHandler
                public void onQuit(PlayerDisconnectEvent e) {
                    if (e.getPlayer().getUniqueId().equals(playerUUID))
                        ProxyServer.getInstance().getPluginManager().unregisterListener(this);
                }
            };
        }, 0, TimeUnit.MILLISECONDS);

        return future.orTimeout(10, TimeUnit.SECONDS);
    }

    @Override
    public <R> CompletableFuture<R> performAsyncTaskThreadSafe(Supplier<CompletableFuture<R>> taskToRun) {
        return CompletableFuture.completedFuture(taskToRun.get().join());
    }
}
