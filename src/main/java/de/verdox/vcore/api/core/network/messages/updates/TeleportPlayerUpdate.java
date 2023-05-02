package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.GameLocation;
import de.verdox.vcore.api.core.network.platform.types.TeleportCause;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeleportPlayerUpdate extends Update {

    public UUID playerUUID;
    public GameLocation gameLocation;
    public TeleportCause teleportCause;
    public TeleportPlayerUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public UpdateCompletion onInstructionReceive(MessagingService messagingService) {
        var platform = VCoreNetwork.getInstance().getPlatform();

        if (!(platform instanceof GameServerPlatform gameServerPlatform))
            return UpdateCompletion.NOT_DONE;

        var result = gameServerPlatform.teleportPlayer(playerUUID, gameLocation, teleportCause)
                                       .orTimeout(10, TimeUnit.SECONDS).join();

        return UpdateCompletion.fromBoolean(result);
    }
}
