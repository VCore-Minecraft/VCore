/*
 * Copyright (c) 2022. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.event.block.chestevents;


import de.verdox.vcore.impl.gameserver.paper.utils.event.VCoreEvent;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChestMergeEvent extends VCoreEvent {
    private final Player merger;
    private final Chest chestConnectedTo;
    private final Chest newChest;
    private final DoubleChest doubleChest;

    public ChestMergeEvent(@NotNull Player merger, @NotNull Chest chestConnectedTo, @NotNull Chest newChest, @NotNull DoubleChest doubleChest) {
        this.merger = merger;
        this.chestConnectedTo = chestConnectedTo;
        this.newChest = newChest;
        this.doubleChest = doubleChest;
    }

    public DoubleChest getDoubleChest() {
        return doubleChest;
    }

    public Player getMerger() {
        return merger;
    }

    public Chest getChestConnectedTo() {
        return chestConnectedTo;
    }

    public Chest getNewChest() {
        return newChest;
    }
}
