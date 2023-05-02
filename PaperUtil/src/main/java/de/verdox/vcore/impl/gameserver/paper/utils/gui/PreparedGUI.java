/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui;


public interface PreparedGUI<T> extends CustomGUI {
    VCoreGUI.Builder<T> getBuilder();
}
