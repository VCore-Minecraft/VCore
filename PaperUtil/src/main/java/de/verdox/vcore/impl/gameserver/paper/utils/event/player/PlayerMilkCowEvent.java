/*
 * Copyright (c) 2022. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.event.player;

import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

public class PlayerMilkCowEvent extends PlayerInteractEntityEvent {

    private final Cow cow;
    private boolean cancelled;

    public PlayerMilkCowEvent(@NotNull Player who, @NotNull Entity clickedEntity, @NotNull final EquipmentSlot hand) {
        super(who, clickedEntity);
        this.cow = (Cow) clickedEntity;
    }


    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * @return The Equipment Slot the player has used to milk the cow
     */

    public Cow getCow() {
        return cow;
    }
}
