package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SetHealthUpdate extends Update {

    public UUID playerUUID;
    public double health;
    public SetHealthUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public UpdateCompletion onInstructionReceive(MessagingService messagingService) {
        var platform = VCoreNetwork.getInstance().getPlatform();

        if (!(platform instanceof GameServerPlatform gameServerPlatform))
            return UpdateCompletion.NOT_DONE;

        return UpdateCompletion.fromBoolean(gameServerPlatform.setPlayerHealth(playerUUID, health).join());
    }
}
