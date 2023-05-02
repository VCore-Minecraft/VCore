package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class KickPlayerUpdate extends Update {

    public UUID playerUUID;
    public String kickMessage;

    public KickPlayerUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public UpdateCompletion onInstructionReceive(MessagingService messagingService) {
        var platform = VCoreNetwork.getInstance().getPlatform();
        return UpdateCompletion.fromBoolean(platform.kickPlayer(playerUUID, kickMessage).join());
    }
}
