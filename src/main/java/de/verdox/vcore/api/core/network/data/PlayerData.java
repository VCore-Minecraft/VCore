package de.verdox.vcore.api.core.network.data;

import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.verdox.vpipeline.api.pipeline.datatypes.IPipelineData;

public interface PlayerData extends IPipelineData {
    void setNbtData(ReadWriteNBT nbtData);
    ReadWriteNBT getNbtData();
}
