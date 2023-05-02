package de.verdox.vcore.impl.gameserver.paper.utils.event;

import de.verdox.vcore.impl.gameserver.paper.utils.event.block.chestevents.ChestMergeEvent;
import de.verdox.vcore.impl.gameserver.paper.utils.event.block.chestevents.ChestSplitEvent;
import de.verdox.vcore.impl.gameserver.paper.utils.event.player.PlayerMilkCowEvent;
import de.verdox.vcore.impl.gameserver.paper.utils.event.player.PlayerPreOpenContainerEvent;
import de.verdox.vcore.impl.gameserver.paper.utils.util.BukkitServerUtil;
import de.verdox.vcore.impl.gameserver.paper.utils.util.BukkitWorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

public class CustomEventListener implements Listener {
    private final JavaPlugin javaPlugin;

    public CustomEventListener(JavaPlugin javaPlugin){
        this.javaPlugin = javaPlugin;
    }

    @EventHandler
    public void onMilk(PlayerInteractEntityEvent e) {
        if (e instanceof PlayerMilkCowEvent)
            return;
        Player player = e.getPlayer();
        if (!(e.getRightClicked() instanceof Cow))
            return;
        Cow cow = (Cow) e.getRightClicked();
        PlayerMilkCowEvent playerMilkCowEvent = null;
        if (player.getInventory().getItemInMainHand().getType().equals(Material.BUCKET)) {
            playerMilkCowEvent = new PlayerMilkCowEvent(player, cow, EquipmentSlot.HAND);
        } else if (player.getInventory().getItemInOffHand().getType().equals(Material.BUCKET)) {
            playerMilkCowEvent = new PlayerMilkCowEvent(player, cow, EquipmentSlot.OFF_HAND);
        }
        if (playerMilkCowEvent != null) {
            Bukkit.getPluginManager().callEvent(playerMilkCowEvent);
            if (playerMilkCowEvent.isCancelled())
                e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void callChestMergeEvent(BlockPlaceEvent e) {
        if (!e.getBlock().getType().equals(Material.CHEST))
            return;
        Chest chest = (Chest) e.getBlock().getState();
        Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
            if (chest.getBlockInventory().getHolder() instanceof DoubleChest) {
                DoubleChest doubleChest = (DoubleChest) chest.getBlockInventory().getHolder();
                Chest chestConnectedTo = BukkitWorldUtil.findConnectedChest(chest).stream().filter(chest1 -> !chest1.equals(chest)).findAny().orElseThrow(() -> new IllegalStateException("Could not find connected chest. Might be an API Bug"));
                Bukkit.getPluginManager().callEvent(new ChestMergeEvent(e.getPlayer(), chest, chestConnectedTo, doubleChest));
            }
        }, 5);
    }

    @EventHandler(ignoreCancelled = true)
    public void callPreOpenContainerEvent(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)
                || e.getClickedBlock() == null
                || !(e.getClickedBlock().getState() instanceof Container))
            return;
        //plugin.consoleMessage("&8[&eEvent&7-&eDebug&8] &7Calling &b" + PlayerPreOpenContainerEvent.class.getSimpleName(), true);
        e.setCancelled(!BukkitServerUtil.callCancellable(new PlayerPreOpenContainerEvent(e.getPlayer(), (Container) e.getClickedBlock().getState())));
    }

    @EventHandler(ignoreCancelled = true)
    public void callChestSplitEvent(BlockBreakEvent e) {
        if (!e.getBlock().getType().equals(Material.CHEST))
            return;
        Chest chest = (Chest) e.getBlock().getState();
        if (!(chest.getBlockInventory().getHolder() instanceof DoubleChest))
            return;
        DoubleChest doubleChest = (DoubleChest) chest.getBlockInventory().getHolder();
        Set<Chest> foundChests = BukkitWorldUtil.findConnectedChest(chest);
        Chest otherChest = foundChests.stream().filter(chest1 -> !chest1.equals(chest)).findAny().orElseThrow(() -> new IllegalStateException("Could not find connected chest. Might be an API Bug"));
        //plugin.consoleMessage("&8[&eEvent&7-&eDebug&8] &7Calling &b" + ChestSplitEvent.class.getSimpleName(), true);
        e.setCancelled(!BukkitServerUtil.callCancellable(new ChestSplitEvent(e.getPlayer(), otherChest, chest, doubleChest)));
    }
}
