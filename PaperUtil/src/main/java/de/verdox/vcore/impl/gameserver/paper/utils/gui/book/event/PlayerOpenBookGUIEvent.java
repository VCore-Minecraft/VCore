/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui.book.event;


import de.verdox.vcore.impl.gameserver.paper.utils.gui.book.BookGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.NotNull;


public class PlayerOpenBookGUIEvent extends BookGUIEvent implements Cancellable {

    private boolean cancelled;

    public PlayerOpenBookGUIEvent(@NotNull Player player, @NotNull BookGUI bookGUI) {
        super(player, bookGUI);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void setCancelled(boolean cancel) {

    }
}
