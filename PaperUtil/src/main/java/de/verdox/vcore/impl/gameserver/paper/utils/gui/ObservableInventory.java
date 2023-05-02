/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.gui;


import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class ObservableInventory implements CustomGUI {

    private final ObservableInventoryListener listener;
    private final JavaPlugin plugin;
    private final Inventory inventory;
    private final Player player;
    private final boolean preventClick;
    private final Consumer<List<ItemStack>> onClose;
    private boolean open;

    public ObservableInventory(JavaPlugin plugin, Player player, Inventory inventory, boolean preventClick, Consumer<List<ItemStack>> onClose) {
        this.plugin = plugin;
        this.inventory = inventory;
        this.player = player;
        this.preventClick = preventClick;
        this.onClose = onClose;
        this.listener = new ObservableInventoryListener();
    }

    public ObservableInventory(JavaPlugin plugin, Player player, Inventory inventory, Consumer<List<ItemStack>> onClose) {
        this(plugin, player, inventory, false, onClose);
    }

    @Override
    public void openInventory() {
        Bukkit.getPluginManager().registerEvents(this.listener, this.plugin);
        Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(inventory));
        this.open = true;
    }

    private void closeInventory() {
        if (open) {
            open = false;
            HandlerList.unregisterAll(this.listener);
            if (onClose != null)
                onClose.accept(Arrays.stream(inventory.getContents()).filter(Objects::nonNull).collect(Collectors.toList()));
        }
    }

    private class ObservableInventoryListener implements Listener {
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (open && e.getInventory().equals(inventory)) {
                closeInventory();
            }
        }

        @EventHandler
        public void onClick(InventoryClickEvent e) {
            if (open && e.getClickedInventory() != null && e.getClickedInventory().equals(inventory) && preventClick)
                e.setCancelled(true);
        }

        @EventHandler
        public void onDrag(InventoryDragEvent e) {
            if (open && e.getInventory().equals(inventory) && preventClick)
                e.setCancelled(true);
        }
    }
}
