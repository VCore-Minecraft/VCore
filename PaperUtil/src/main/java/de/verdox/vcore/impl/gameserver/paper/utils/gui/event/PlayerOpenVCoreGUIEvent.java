/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui.event;

import de.verdox.vcore.impl.gameserver.paper.utils.event.VCoreHybridEvent;
import de.verdox.vcore.impl.gameserver.paper.utils.gui.VCoreGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;


public class PlayerOpenVCoreGUIEvent extends VCoreHybridEvent implements Cancellable {
    private final Player player;
    private final VCoreGUI<?> vCoreGUI;
    private boolean cancelled;

    public PlayerOpenVCoreGUIEvent(@NotNull Player player, VCoreGUI<?> vCoreGUI) {
        this.player = player;
        this.vCoreGUI = vCoreGUI;
    }

    public Player getPlayer() {
        return player;
    }

    public VCoreGUI<?> getvCoreGUI() {
        return vCoreGUI;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
