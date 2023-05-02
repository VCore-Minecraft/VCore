/*
 * Copyright (c) 2022. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.event.block.chestevents;

import de.verdox.vcore.impl.gameserver.paper.utils.event.VCoreEvent;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;

public class ChestSplitEvent extends VCoreEvent implements Cancellable {

    private final Chest brokenChest;
    private final DoubleChest doubleChest;
    private final Player splitter;
    private final Chest otherChest;
    private boolean cancelled;

    public ChestSplitEvent(@NotNull Player splitter, @NotNull Chest otherChest, @NotNull Chest brokenChest, @NotNull DoubleChest doubleChest) {
        this.splitter = splitter;
        this.otherChest = otherChest;
        this.brokenChest = brokenChest;
        this.doubleChest = doubleChest;
    }

    public Player getSplitter() {
        return splitter;
    }

    public DoubleChest getDoubleChest() {
        return doubleChest;
    }

    /**
     * @return Chest that will be broken by a player in this Event
     */
    public Chest getBrokenChest() {
        return brokenChest;
    }

    /**
     * @return The Chest that is connected to the chest that is broken
     */
    public Chest getOtherChest() {
        return otherChest;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
