package de.verdox.vcore.impl.core.network;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.verdox.vcore.api.core.network.data.PlayerData;
import de.verdox.vpipeline.api.pipeline.annotations.DataStorageIdentifier;
import de.verdox.vpipeline.api.pipeline.annotations.PipelineDataProperties;
import de.verdox.vpipeline.api.pipeline.core.Pipeline;
import de.verdox.vpipeline.api.pipeline.datatypes.PipelineData;
import de.verdox.vpipeline.api.pipeline.enums.DataContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@DataStorageIdentifier(identifier = "PlayerData")
@PipelineDataProperties(dataContext = DataContext.CACHE_ONLY)
public class PlayerDataImpl extends PipelineData implements PlayerData {
    private ReadWriteNBT nbtData;

    public PlayerDataImpl(@NotNull Pipeline pipeline, @NotNull UUID objectUUID) {
        super(pipeline, objectUUID);
    }

    public void setNbtData(ReadWriteNBT nbtData) {
        this.nbtData = nbtData;
    }

    public ReadWriteNBT getNbtData() {
        return nbtData;
    }
}
