package de.verdox.vcore.impl.gameserver.paper;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.PlayerGameMode;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vcore.api.core.network.platform.types.TeleportCause;
import de.verdox.vcore.impl.gameserver.paper.listener.QueryListener;
import de.verdox.vcore.impl.gameserver.paper.listener.VCorePaperListener;
import de.verdox.vpipeline.api.NetworkLogger;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ProxyServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.*;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static de.verdox.vcore.impl.gameserver.paper.util.PaperUtil.*;

public class PaperMCPlatform implements GameServerPlatform {
    private final VCorePaper vCorePaper;

    PaperMCPlatform(VCorePaper vCorePaper) {
        this.vCorePaper = vCorePaper;
    }

    @Override
    public CompletableFuture<Boolean> setPlayerHealth(@NotNull UUID playerUUID, double health) {
        return performServerActionThreadSafe(() -> {
            var player = Bukkit.getPlayer(playerUUID);
            if (player == null)
                return false;
            player.setHealth(health);
            NetworkLogger.debug("setPlayerHealth [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> setPlayerFood(@NotNull UUID playerUUID, int food) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return false;
            Bukkit.getPlayer(playerUUID).setFoodLevel(food);
            NetworkLogger.debug("setPlayerFood [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> teleportPlayer(@NotNull UUID playerUUID, @NotNull GameLocation gameLocation, TeleportCause teleportCause) {
        return performAsyncTaskThreadSafe(() -> {
            var loc = toBukkitLocation(gameLocation);
            if (Bukkit.getPlayer(playerUUID) == null || loc.getWorld() == null)
                return CompletableFuture.completedFuture(false);
            return Bukkit.getPlayer(playerUUID)
                         .teleportAsync(toBukkitLocation(gameLocation), PlayerTeleportEvent.TeleportCause.valueOf(teleportCause.name()))
                         .whenComplete((aBoolean, throwable) -> {
                             NetworkLogger.debug("teleportPlayer [" + playerUUID + "]");
                         });
        });
    }

    @Override
    public CompletableFuture<Boolean> setGameMode(@NotNull UUID playerUUID, @NotNull PlayerGameMode playerGameMode) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return false;
            Bukkit.getPlayer(playerUUID).setGameMode(toBukkitGameMode(playerGameMode));
            NetworkLogger.debug("setGameMode [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> sendMessage(@NotNull UUID playerUUID, @NotNull String message, @NotNull PlayerMessageType playerMessageType) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return false;
            var player = Bukkit.getPlayer(playerUUID);
            if (playerMessageType.equals(PlayerMessageType.CHAT))
                player.sendMessage(Component.text(message));
            else if (playerMessageType.equals(PlayerMessageType.ACTION_BAR))
                player.sendActionBar(Component.text(message));
            NetworkLogger.debug("sendMessage [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<Void> broadcastMessage(@NotNull String message, @NotNull PlayerMessageType playerMessageType) {
        return performAsyncTaskThreadSafe(() -> {
            var array = Bukkit.getOnlinePlayers().stream()
                              .map(player -> sendMessage(player.getUniqueId(), message, playerMessageType))
                              .toList().toArray(new CompletableFuture[0]);
            NetworkLogger.debug("broadcastMessage");
            return CompletableFuture.allOf(array);
        });
    }

    @Override
    public CompletableFuture<Boolean> kickPlayer(@NotNull UUID playerUUID, @NotNull String kickMessage) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return false;
            Bukkit.getPlayer(playerUUID).kick(Component.text(kickMessage));
            NetworkLogger.debug("kickPlayer [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<Boolean> killPlayer(@NotNull UUID playerUUID) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return false;
            Bukkit.getPlayer(playerUUID).setHealth(0);
            NetworkLogger.debug("killPlayer [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<Void> waitForPlayerReady(@NotNull UUID playerUUID) {
        var future = new CompletableFuture<Void>();

        Bukkit.getScheduler().runTask(vCorePaper, () -> {

            if (Bukkit.getPlayer(playerUUID) != null) {
                future.complete(null);
                return;
            }

            new VCorePaperListener(vCorePaper) {
                @EventHandler
                public void onJoin(PlayerJoinEvent e) {
                    if (e.getPlayer().getUniqueId().equals(playerUUID)) {
                        future.complete(null);
                        HandlerList.unregisterAll(this);
                    }
                }

                @EventHandler
                public void onQuit(PlayerQuitEvent e) {
                    if (e.getPlayer().getUniqueId().equals(playerUUID))
                        HandlerList.unregisterAll(this);
                }

                @EventHandler
                public void onKick(PlayerKickEvent e) {
                    if (e.getPlayer().getUniqueId().equals(playerUUID))
                        HandlerList.unregisterAll(this);
                }
            };
        });

        return future.orTimeout(10, TimeUnit.SECONDS);
    }

    @Override
    public CompletableFuture<Boolean> clearInventory(@NotNull UUID playerUUID) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return false;
            Bukkit.getPlayer(playerUUID).getInventory().clear();
            NetworkLogger.debug("clearInventory [" + playerUUID + "]");
            return true;
        });
    }

    @Override
    public CompletableFuture<GameLocation> getLocation(@NotNull UUID playerUUID) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return null;
            return fromBukkitLocation(Bukkit
                    .getPlayer(playerUUID)
                    .getLocation());
        });
    }

    @Override
    public CompletableFuture<List<String>> getWorlds() {
        return performServerActionThreadSafe(() -> Bukkit
                .getWorlds()
                .stream()
                .map(WorldInfo::getName)
                .collect(Collectors.toList()));
    }

    @Override
    public <R> CompletableFuture<R> performServerActionThreadSafe(Supplier<R> taskToRun) {
        var future = new CompletableFuture<R>();
        Bukkit.getScheduler().runTaskLater(vCorePaper, () -> {
            try {
                var result = taskToRun.get();
                future.complete(result);
            } catch (Throwable e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        }, 1L);
        return future;
    }

    @Override
    public <R> CompletableFuture<R> performAsyncTaskThreadSafe(Supplier<CompletableFuture<R>> taskToRun) {
        var future = new CompletableFuture<R>();
        Bukkit.getScheduler()
              .runTaskLater(vCorePaper, () -> taskToRun.get().whenComplete((r, throwable) -> future.complete(r)), 5L);
        return future;
    }

    @Override
    public CompletableFuture<Double> getTPS() {
        return performServerActionThreadSafe(() -> Bukkit.getTPS()[2]);
    }

    @Override
    public CompletableFuture<Boolean> isPlayerOnline(@NotNull UUID playerUUID) {
        return performServerActionThreadSafe(() -> Bukkit.getPlayer(playerUUID) != null);
    }

    @Override
    public CompletableFuture<InetSocketAddress> getPlayerAddress(@NotNull UUID playerUUID) {
        return performServerActionThreadSafe(() -> {
            if (Bukkit.getPlayer(playerUUID) == null)
                return null;
            return Bukkit.getPlayer(playerUUID).getAddress();
        });
    }

    @Override
    public boolean isConnectedToProxyNetwork() {
        if (!VCorePaper.isTestEnv)
            return Bukkit.spigot().getSpigotConfig().getBoolean("settings.bungeecord");
        else return false;
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }
}
