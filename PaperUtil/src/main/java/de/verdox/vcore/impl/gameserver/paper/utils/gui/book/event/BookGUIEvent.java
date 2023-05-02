/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui.book.event;

import de.verdox.vcore.impl.gameserver.paper.utils.event.VCoreHybridEvent;
import de.verdox.vcore.impl.gameserver.paper.utils.gui.book.BookGUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public abstract class BookGUIEvent extends VCoreHybridEvent {
    private final Player player;
    private final BookGUI bookGUI;

    public BookGUIEvent(@NotNull Player player, @NotNull BookGUI bookGUI) {
        this.player = player;
        this.bookGUI = bookGUI;
    }

    public Player getPlayer() {
        return player;
    }

    public BookGUI getBookGUI() {
        return bookGUI;
    }
}
