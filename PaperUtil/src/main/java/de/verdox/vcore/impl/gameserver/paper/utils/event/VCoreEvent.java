/*
 * Copyright (c) 2022. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class VCoreEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public VCoreEvent() {
        super(false);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
