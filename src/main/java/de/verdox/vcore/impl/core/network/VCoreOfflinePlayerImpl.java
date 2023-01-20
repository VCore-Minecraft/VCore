package de.verdox.vcore.impl.core.network;

import de.verdox.vcore.api.core.network.data.VCoreOfflinePlayer;
import de.verdox.vpipeline.api.pipeline.annotations.DataStorageIdentifier;
import de.verdox.vpipeline.api.pipeline.annotations.PipelineDataProperties;
import de.verdox.vpipeline.api.pipeline.core.Pipeline;
import de.verdox.vpipeline.api.pipeline.datatypes.PipelineData;
import de.verdox.vpipeline.api.pipeline.enums.DataContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@DataStorageIdentifier(identifier = "VCoreOfflinePlayer")
@PipelineDataProperties(dataContext = DataContext.STORAGE_ONLY)
public class VCoreOfflinePlayerImpl extends PipelineData implements VCoreOfflinePlayer {
    public VCoreOfflinePlayerImpl(@NotNull Pipeline pipeline, @NotNull UUID objectUUID) {
        super(pipeline, objectUUID);
    }
}
