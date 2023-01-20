package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vpipeline.api.messaging.instruction.TransmittedData;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class KickPlayerUpdate extends Update {
    public KickPlayerUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public List<Class<?>> instructionDataTypes() {
        return List.of(UUID.class, String.class);
    }


    @Override
    protected @NotNull UpdateCompletion executeUpdate(TransmittedData transmittedData) {
        var targetUUID = transmittedData.getObject(0, UUID.class);
        var kickMsg = transmittedData.getObject(1, String.class);

        var platform = VCoreNetwork.getInstance().getPlatform();
        return UpdateCompletion.fromBoolean(platform.kickPlayer(targetUUID, kickMsg).join());
    }
}
