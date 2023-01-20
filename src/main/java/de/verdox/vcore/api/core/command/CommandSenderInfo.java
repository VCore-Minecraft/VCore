package de.verdox.vcore.api.core.command;

import de.verdox.vcore.api.core.PlatformDependentImplementation;
import de.verdox.vcore.api.core.network.data.VCorePlayer;
import de.verdox.vpipeline.api.pipeline.datatypes.IPipelineData;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;

/**
 * @param <T> Platform plugin
 * @param <S> Command Sender implemented by platform
 * @param <P> Player class implemented by platform
 */
public abstract class CommandSenderInfo<T, S, P> extends PlatformDependentImplementation<T> {
    protected final S sender;

    public CommandSenderInfo(T plugin, S sender) {
        super(plugin);
        this.sender = sender;
    }

    public abstract P getAsPlayer();

    public abstract DataReference<VCorePlayer> getAsVCorePlayer();

    public abstract S getAsSender();

    public <I extends IPipelineData> DataReference<I> getPlayerData(Class<? extends I> type) {
        return getAsVCorePlayer().pipeline().createDataReference(type, getAsVCorePlayer().uuid());
    }
}
