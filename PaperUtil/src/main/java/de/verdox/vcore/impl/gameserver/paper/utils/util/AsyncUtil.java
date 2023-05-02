package de.verdox.vcore.impl.gameserver.paper.utils.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AsyncUtil {
    public static CompletableFuture<Player> getPlayerFromMainThreadSafe(JavaPlugin javaPlugin, UUID player) {
        var future = new CompletableFuture<Player>();
        Bukkit.getScheduler().runTask(javaPlugin, () -> future.complete(Bukkit.getPlayer(player)));
        return future;
    }

    public static Player waitForPlayerFromMainThread(JavaPlugin javaPlugin, UUID player) {
        var future = new CompletableFuture<Player>();
        Bukkit.getScheduler().runTask(javaPlugin, () -> future.complete(Bukkit.getPlayer(player)));
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
