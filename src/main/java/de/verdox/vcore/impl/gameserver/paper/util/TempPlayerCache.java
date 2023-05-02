package de.verdox.vcore.impl.gameserver.paper.util;

import de.verdox.vcore.impl.gameserver.paper.listener.VCorePaperListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class TempPlayerCache<T> extends VCorePaperListener {
    private final Map<UUID, T> cache = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> removalTasks = new ConcurrentHashMap<>();
    private JavaPlugin javaPlugin;

    public TempPlayerCache(JavaPlugin javaPlugin) {
        super(javaPlugin);
        this.javaPlugin = javaPlugin;
    }

    @EventHandler
    void onJoin(PlayerJoinEvent e) {

    }

    @EventHandler
    void onQuit(PlayerQuitEvent e) {
        removeData(e.getPlayer());
    }

    @EventHandler
    void onKick(PlayerKickEvent e) {
        removeData(e.getPlayer());
    }

    public boolean hasData(UUID uuid) {
        return cache.containsKey(uuid);
    }

    public void removeData(UUID player) {
        cache.remove(player);
        var former = removalTasks.remove(player);
        if (former != null)
            former.cancel();
    }

    public boolean setData(UUID uuid, T data) {
        if (hasData(uuid))
            return false;
        cache.put(uuid, data);
        return true;
    }

    public boolean replaceData(UUID uuid, T data) {
        cache.put(uuid, data);
        return true;
    }

    public boolean setDataForTime(UUID uuid, T data, long time, TimeUnit timeUnit, @Nullable Consumer<Player> onCleanup) {
        if (hasData(uuid))
            return false;
        if (removalTasks.containsKey(uuid)) {
            var formerTask = removalTasks.remove(uuid);
            if (formerTask != null)
                formerTask.cancel();
        }
        setData(uuid, data);
        removalTasks.put(uuid,
                Bukkit.getScheduler()
                      .runTaskLaterAsynchronously(javaPlugin, () -> {
                          removeData(uuid);
                          if (isPlayerOnline(uuid) && onCleanup != null)
                              onCleanup.accept(Bukkit.getPlayer(uuid));
                      }, 20L * timeUnit.toSeconds(time)));
        return true;
    }

    public boolean setDataForTime(UUID uuid, T data, long time, TimeUnit timeUnit) {
        return setDataForTime(uuid, data, time, timeUnit, null);
    }

    @Nullable
    public T getData(UUID playerUUID) {
        return cache.getOrDefault(playerUUID, null);
    }

    public void ifPresent(UUID playerUUID, Consumer<T> consumer) {
        var data = cache.getOrDefault(playerUUID, null);
        if (data != null)
            consumer.accept(data);
    }

    public boolean hasData(Player player) {
        return hasData(player.getUniqueId());
    }

    public void removeData(Player player) {
        removeData(player.getUniqueId());
    }

    public boolean setData(Player player, T data) {
        return setData(player.getUniqueId(), data);
    }

    public boolean replaceData(Player player, T data) {
        return replaceData(player.getUniqueId(), data);
    }

    public boolean setDataForTime(Player player, T data, long time, TimeUnit timeUnit, @Nullable Consumer<Player> onCleanup) {
        return setDataForTime(player.getUniqueId(), data, time, timeUnit, onCleanup);
    }

    public boolean setDataForTime(Player player, T data, long time, TimeUnit timeUnit) {
        return setDataForTime(player.getUniqueId(), data, time, timeUnit, null);
    }

    public void ifPresent(Player player, Consumer<T> consumer) {
        ifPresent(player.getUniqueId(), consumer);
    }

    @Nullable
    public T getData(Player player) {
        return getData(player.getUniqueId());
    }

    private boolean isPlayerOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    public Set<UUID> getPlayersWithData() {
        return cache.keySet();
    }
}

