package de.verdox.vcore.api.core.network.platform;

import de.verdox.vcore.api.core.network.VCoreServer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProxyPlatform extends Platform {
    CompletableFuture<Boolean> sendToServer(@NotNull UUID playerUUID, @NotNull VCoreServer vCoreServer);
    CompletableFuture<VCoreServer> getServer(@NotNull UUID playerUUID);
}
