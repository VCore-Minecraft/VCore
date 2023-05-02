package de.verdox.vcore.impl.gameserver.paper.utils.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class BukkitEventUtil {

    public static boolean callCancellable(Cancellable cancellable) {
        if (!(cancellable instanceof Event event))
            throw new IllegalArgumentException("Cancellable must be instance of " + Event.class);
        Bukkit.getPluginManager().callEvent(event);
        return !cancellable.isCancelled();
    }
}
