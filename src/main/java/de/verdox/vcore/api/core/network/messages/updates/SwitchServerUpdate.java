package de.verdox.vcore.api.core.network.messages.updates;

import de.verdox.vcore.api.core.network.platform.ProxyPlatform;
import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vpipeline.api.messaging.instruction.TransmittedData;
import de.verdox.vpipeline.api.messaging.instruction.types.Update;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class SwitchServerUpdate extends Update {
    public SwitchServerUpdate(@NotNull UUID uuid) {
        super(uuid);
    }

    @Override
    public List<Class<?>> instructionDataTypes() {
        return List.of(UUID.class, String.class);
    }

    @Override
    protected @NotNull UpdateCompletion executeUpdate(TransmittedData transmittedData) {
        var target = transmittedData.getObject(0, UUID.class);
        var server = transmittedData.getObject(1, String.class);

        var platform = VCoreNetwork.getInstance().getPlatform();

        if (!(platform instanceof ProxyPlatform proxyPlatform))
            return UpdateCompletion.NOT_DONE;
        
        var vCoreServer = VCoreNetwork.getInstance().getServer(server);
        var worked = proxyPlatform.sendToServer(target, vCoreServer);
        return UpdateCompletion.fromBoolean(worked.join());
    }
}
