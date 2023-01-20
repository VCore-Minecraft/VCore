package de.verdox.vcore.api.core.network.platform;

import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.PlayerGameMode;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface GameServerPlatform extends Platform {
    //TODO: CompletableFuture maybe for teleport and stuff

    CompletableFuture<Boolean> setPlayerHealth(@NotNull UUID playerUUID, double health);

    CompletableFuture<Boolean> setPlayerFood(@NotNull UUID playerUUID, int food);

    CompletableFuture<Boolean> teleportPlayer(@NotNull UUID playerUUID, @NotNull GameLocation gameLocation);

    CompletableFuture<Boolean> setGameMode(@NotNull UUID playerUUID, @NotNull PlayerGameMode playerGameMode);

    CompletableFuture<Boolean> sendMessage(@NotNull UUID playerUUID, @NotNull String message, @NotNull PlayerMessageType playerMessageType);

    CompletableFuture<Boolean> broadcastMessage(@NotNull String message, @NotNull PlayerMessageType playerMessageType);

    CompletableFuture<Boolean> killPlayer(@NotNull UUID playerUUID);

    CompletableFuture<Boolean> clearInventory(@NotNull UUID playerUUID);

    CompletableFuture<GameLocation> getLocation(@NotNull UUID playerUUID);

    CompletableFuture<List<String>> getWorlds();

    CompletableFuture<Void> performServerActionThreadSafe(Runnable taskToRun);

    CompletableFuture<Double> getTPS();
}
