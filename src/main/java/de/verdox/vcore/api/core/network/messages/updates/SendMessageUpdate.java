package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SendMessageUpdate extends Update {

    public UUID playerUUID;
    public PlayerMessageType messageType;
    public String message;

    public SendMessageUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public UpdateCompletion onInstructionReceive(MessagingService messagingService) {
        var platform = VCoreNetwork.getInstance().getPlatform();

        if (platform instanceof GameServerPlatform gameServerPlatform)
            return UpdateCompletion.fromBoolean(gameServerPlatform
                    .sendMessage(playerUUID, message, messageType)
                    .join());

        return UpdateCompletion.NOT_DONE;
    }
}
