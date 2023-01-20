package de.verdox.vcore.api.core.network.platform;

import de.verdox.vcore.impl.util.MojangAPI;
import de.verdox.vpipeline.api.NetworkLogger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface Platform {
    CompletableFuture<Boolean> isPlayerOnline(@NotNull UUID playerUUID);

    default CompletableFuture<String> getPlayerName(UUID playerUUID) {
        return MojangAPI.getNameAsync(playerUUID);
    }

    CompletableFuture<InetSocketAddress> getPlayerAddress(@NotNull UUID playerUUID);

    boolean isConnectedToProxyNetwork();

    CompletableFuture<Boolean> kickPlayer(@NotNull UUID playerUUID, @NotNull String kickMessage);

    void shutdown();

    default <T> CompletableFuture<T> ifPlayerOnlineCallback(UUID playerUUID, Supplier<T> supplier, T ifNotOnlineValue) {
        var future = new CompletableFuture<T>();

        isPlayerOnline(playerUUID).whenComplete((playerIsOnline, throwable) -> {
            try {
                if (!playerIsOnline)
                    future.complete(ifNotOnlineValue);
                else
                    future.complete(supplier.get());
            } catch (Throwable e) {
                NetworkLogger.warning("Exception thrown in playerOnlineCallback");
                e.printStackTrace();
                future.complete(ifNotOnlineValue);
            }
        });

        return future;
    }
}
