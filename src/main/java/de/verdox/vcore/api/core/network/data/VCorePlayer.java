package de.verdox.vcore.api.core.network.data;

import de.verdox.vcore.api.core.network.VCoreServer;
import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.PlayerGameMode;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vpipeline.api.pipeline.datatypes.IPipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;

public interface VCorePlayer extends IPipelineData {

    VCoreServer getCurrentServer();

    GameLocation getGameLocation();

    ServerLocation getServerLocation();

    String getName();

    void teleport(GameLocation gameLocation);

    default void teleport(double x, double y, double z) {
        teleport(new GameLocation(getGameLocation().worldName(), x, y, z, getGameLocation().yaw(), getGameLocation().pitch()));
    }

    void teleport(ServerLocation serverLocation);

    void teleportTo(DataReference<VCorePlayer> target);

    void sendMessage(String message, PlayerMessageType playerMessageType);

    void setHealth(double health);

    void setFood(int food);

    void clearInventory();

    void kickPlayer();

    void killPlayer();

    void setGameMode(PlayerGameMode playerGameMode);
}
