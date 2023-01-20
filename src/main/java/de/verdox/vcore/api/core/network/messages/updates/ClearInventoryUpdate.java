package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.GameServerPlatform;
import de.verdox.vpipeline.api.messaging.instruction.TransmittedData;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ClearInventoryUpdate extends Update {
    public ClearInventoryUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public List<Class<?>> instructionDataTypes() {
        return List.of(UUID.class);
    }


    @Override
    protected @NotNull UpdateCompletion executeUpdate(TransmittedData transmittedData) {
        var targetUUID = transmittedData.getObject(0, UUID.class);

        var platform = VCoreNetwork.getInstance().getPlatform();

        if (!(platform instanceof GameServerPlatform gameServerPlatform))
            return UpdateCompletion.NOT_DONE;
        return UpdateCompletion.fromBoolean(gameServerPlatform.clearInventory(targetUUID).join());
    }
}
