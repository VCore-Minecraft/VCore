package de.verdox.vcore.api.core.listener;

import de.verdox.vcore.api.core.network.VCoreNetwork;
import de.verdox.vcore.api.core.network.platform.types.PlayerMessageType;
import de.verdox.vcore.api.core.network.platform.types.ServerLocation;
import de.verdox.vcore.impl.core.network.VCorePlayerImpl;
import de.verdox.vpipeline.api.pipeline.datatypes.customtypes.DataReference;
import de.verdox.vpipeline.api.pipeline.enums.PreloadStrategy;
import de.verdox.vpipeline.api.util.AnnotationResolver;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataLoader {

    default CompletableFuture<DataReference<VCorePlayerImpl>> onJoin(UUID playerUUID, String playerName, @Nullable ServerLocation serverLocation) {
        return VCoreNetwork
                .getInstance()
                .addPlayerToCache(playerUUID, playerName, serverLocation)
                .whenComplete((vCorePlayerDataReference, throwable) -> {
                    loadNeededPlayerData(playerUUID);
                });
    }

    default void onQuit(UUID playerUUID, String playerName, boolean removeFromCache) {
        //clearCacheFromUnusedPlayerData(playerUUID);
        if (removeFromCache) {
            VCoreNetwork
                    .getInstance()
                    .removePlayerFromCache(playerUUID);
            VCoreNetwork.getInstance().broadCastMessage(PlayerMessageType.CHAT, "§c- " + playerName);
        }
    }

    default void loadNeededPlayerData(UUID playerUUID) {
        VCoreNetwork
                .getInstance()
                .getPlayerDataClasses()
                .stream()
                .filter(aClass -> AnnotationResolver
                        .getDataProperties(aClass)
                        .preloadStrategy()
                        .equals(PreloadStrategy.LOAD_ON_NEED))
                .forEach(aClass -> VCoreNetwork
                        .getInstance()
                        .getPipeline()
                        .loadOrCreate(aClass, playerUUID));
    }

    default void clearCacheFromUnusedPlayerData(UUID playerUUID) {
        VCoreNetwork
                .getInstance()
                .getPlayerDataClasses()
                .stream()
                .filter(aClass -> AnnotationResolver
                        .getDataProperties(aClass)
                        .preloadStrategy()
                        .equals(PreloadStrategy.LOAD_ON_NEED))
                .forEach(aClass -> {
                    VCoreNetwork
                            .getInstance()
                            .getPipeline()
                            .getLocalCache().remove(aClass, playerUUID);
                });
    }

}
