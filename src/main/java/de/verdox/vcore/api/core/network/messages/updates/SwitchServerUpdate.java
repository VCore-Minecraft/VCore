package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.platform.ProxyPlatform;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vpipeline.api.NetworkLogger;
import de.verdox.vpipeline.api.messaging.MessagingService;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SwitchServerUpdate extends Update {

    public UUID playerUUID;
    public String serverName;

    public SwitchServerUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public UpdateCompletion onInstructionReceive(MessagingService messagingService) {
        var platform = VCoreNetwork.getInstance().getPlatform();

        if (!(platform instanceof ProxyPlatform proxyPlatform))
            return UpdateCompletion.NOT_DONE;
        if (playerUUID == null) {
            NetworkLogger.getLogger().warning("Received SwitchServer Instruction but playerUUID was null");
            return UpdateCompletion.CANCELLED;
        }
        if (serverName == null) {
            NetworkLogger.getLogger().warning("Received SwitchServer Instruction but serverName was null");
            return UpdateCompletion.CANCELLED;
        }

        var vCoreServer = VCoreNetwork.getInstance().getServer(serverName);
        var worked = proxyPlatform.sendToServer(playerUUID, vCoreServer);

        return UpdateCompletion.fromBoolean(worked.orTimeout(10, TimeUnit.SECONDS).join());
    }
}
