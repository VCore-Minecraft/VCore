package de.verdox.vcore.api.core.network.data;

import de.verdox.vcore.api.core.network.VCoreServer;
import de.verdox.vcore.api.core.network.platform.types.*;
import de.verdox.vpipeline.api.pipeline.datatypes.IPipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;

import java.util.concurrent.CompletableFuture;

public interface VCorePlayer extends IPipelineData {

    VCoreServer getCurrentServer();

    GameLocation getGameLocation();

    ServerLocation getServerLocation();

    String getName();

    CompletableFuture<Boolean> teleport(GameLocation gameLocation, TeleportCause teleportCause);

    default CompletableFuture<Boolean> teleport(double x, double y, double z, TeleportCause teleportCause) {
        return teleport(new GameLocation(getGameLocation().worldName(), x, y, z, getGameLocation().yaw(), getGameLocation().pitch()), teleportCause);
    }

    CompletableFuture<Boolean> teleport(ServerLocation serverLocation, TeleportCause teleportCause);

    CompletableFuture<Boolean> teleportTo(DataReference<VCorePlayer> target, TeleportCause teleportCause);

    CompletableFuture<Boolean> switchServer(VCoreServer vCoreServer);

    CompletableFuture<Boolean> sendMessage(String message, PlayerMessageType playerMessageType);

    CompletableFuture<Boolean> setHealth(double health);

    CompletableFuture<Boolean> setFood(int food);

    CompletableFuture<Boolean> clearInventory();

    CompletableFuture<Boolean> kickPlayer();

    CompletableFuture<Boolean> killPlayer();

    CompletableFuture<Boolean> setGameMode(PlayerGameMode playerGameMode);
}
