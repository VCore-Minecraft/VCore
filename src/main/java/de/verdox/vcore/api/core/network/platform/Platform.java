package de.verdox.vcore.api.core.network.platform;

import de.verdox.vcore.impl.util.MojangAPI;
import de.verdox.vpipeline.api.NetworkLogger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface Platform {
    CompletableFuture<Boolean> isPlayerOnline(@NotNull UUID playerUUID);

    default CompletableFuture<String> getPlayerName(UUID playerUUID) {
        return MojangAPI.getNameAsync(playerUUID);
    }

    CompletableFuture<InetSocketAddress> getPlayerAddress(@NotNull UUID playerUUID);

    boolean isConnectedToProxyNetwork();

    CompletableFuture<Boolean> kickPlayer(@NotNull UUID playerUUID, @NotNull String kickMessage);

    <R> CompletableFuture<R> performServerActionThreadSafe(Supplier<R> taskToRun);


    /**
     * Used whenever a player joined a server, but you want to be clear he is loaded on this server correctly.
     * This should only be used in environments that are not in sync with the minecraft main thread.
     * <p>
     * For example if you are executing logic inside an update you may want to wait for the player to be ready because you know you sent
     * a switch server update before.
     *
     * @param playerUUID The player
     * @return A future that completes when the player is ready
     */
    CompletableFuture<Void> waitForPlayerReady(@NotNull UUID playerUUID);

    default <R> R performBlockingMainThreadGetter(Supplier<R> taskToRun, R defaultValue) {
        return performServerActionThreadSafe(taskToRun).completeOnTimeout(defaultValue, 10, TimeUnit.SECONDS).join();
    }

    <R> CompletableFuture<R> performAsyncTaskThreadSafe(Supplier<CompletableFuture<R>> taskToRun);

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

        return future.orTimeout(10, TimeUnit.SECONDS);
    }
}
