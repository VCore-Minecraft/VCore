package de.verdox.vcore.impl.gameserver.paper;

import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.PlayerGameMode;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static de.verdox.vcore.impl.gameserver.paper.util.PaperUtil.*;

public class PaperMCPlatform implements GameServerPlatform {
    private final VCorePaper vCorePaper;

    PaperMCPlatform(VCorePaper vCorePaper) {
        this.vCorePaper = vCorePaper;
    }

    @Override
    public CompletableFuture<Boolean> setPlayerHealth(@NotNull UUID playerUUID, double health) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit.getPlayer(playerUUID).setHealth(health));
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> setPlayerFood(@NotNull UUID playerUUID, int food) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit.getPlayer(playerUUID).setFoodLevel(food));
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> teleportPlayer(@NotNull UUID playerUUID, @NotNull GameLocation gameLocation) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit
                    .getPlayer(playerUUID)
                    .teleportAsync(toBukkitLocation(gameLocation)));
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> setGameMode(@NotNull UUID playerUUID, @NotNull PlayerGameMode playerGameMode) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit
                    .getPlayer(playerUUID)
                    .setGameMode(toBukkitGameMode(playerGameMode)));
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> sendMessage(@NotNull UUID playerUUID, @NotNull String message, @NotNull PlayerMessageType playerMessageType) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> {
                var player = Bukkit.getPlayer(playerUUID);
                if (playerMessageType.equals(PlayerMessageType.CHAT))
                    player.sendMessage(Component.text(message));
                else if (playerMessageType.equals(PlayerMessageType.ACTION_BAR))
                    player.sendActionBar(Component.text(message));
            });
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> broadcastMessage(@NotNull String message, @NotNull PlayerMessageType playerMessageType) {
        performServerActionThreadSafe(() -> Bukkit
                .getOnlinePlayers()
                .forEach(player -> sendMessage(player.getUniqueId(), message, playerMessageType)));
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> kickPlayer(@NotNull UUID playerUUID, @NotNull String kickMessage) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit.getPlayer(playerUUID).kick());
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> killPlayer(@NotNull UUID playerUUID) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit.getPlayer(playerUUID).setHealth(0));
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<Boolean> clearInventory(@NotNull UUID playerUUID) {
        return ifPlayerOnlineCallback(playerUUID, () -> {
            performServerActionThreadSafe(() -> Bukkit.getPlayer(playerUUID).getInventory().clear());
            return true;
        }, false);
    }

    @Override
    public CompletableFuture<GameLocation> getLocation(@NotNull UUID playerUUID) {
        return ifPlayerOnlineCallback(playerUUID, () -> fromBukkitLocation(Bukkit
                .getPlayer(playerUUID)
                .getLocation()), null);
    }

    @Override
    public CompletableFuture<List<String>> getWorlds() {
        return CompletableFuture.completedFuture(Bukkit
                .getWorlds()
                .stream()
                .map(WorldInfo::getName)
                .collect(Collectors.toList()));
    }

    @Override
    public CompletableFuture<Void> performServerActionThreadSafe(Runnable taskToRun) {
        var future = new CompletableFuture<Void>();
        Bukkit.getScheduler().runTask(vCorePaper, () -> {
            taskToRun.run();
            future.complete(null);
        });
        return future;
    }

    @Override
    public CompletableFuture<Double> getTPS() {
        return CompletableFuture.completedFuture(Bukkit.getTPS()[2]);
    }

    @Override
    public CompletableFuture<Boolean> isPlayerOnline(@NotNull UUID playerUUID) {
        return CompletableFuture.completedFuture(Bukkit.getPlayer(playerUUID) != null);
    }

    @Override
    public CompletableFuture<InetSocketAddress> getPlayerAddress(@NotNull UUID playerUUID) {
        return ifPlayerOnlineCallback(playerUUID, () -> Bukkit.getPlayer(playerUUID).getAddress(), null);
    }

    @Override
    public boolean isConnectedToProxyNetwork() {
        return Bukkit.spigot().getSpigotConfig().getBoolean("settings.bungeecord");
    }

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }
}
