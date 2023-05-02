/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;


public class BukkitServerUtil {
    public static boolean callCancellable(Cancellable cancellable) {
        if (!(cancellable instanceof Event))
            throw new IllegalStateException(cancellable + " not instance of Event");
        Bukkit.getPluginManager().callEvent((Event) cancellable);
        return !cancellable.isCancelled();
    }
}
