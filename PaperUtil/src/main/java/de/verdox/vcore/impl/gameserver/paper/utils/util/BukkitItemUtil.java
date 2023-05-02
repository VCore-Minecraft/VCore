/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.ThreadLocalRandom;


public class BukkitItemUtil {

    public static ItemStack createStackWithName(Material material, Component displayName) {
        var stack = new ItemStack(material);
        stack.editMeta(meta -> meta.displayName(displayName));
        return stack;
    }

    public static ItemStack createStackWithName(Material material, String displayName) {
        return createStackWithName(material, Component.text(displayName));
    }

    public int applyDamageNaturally(@Nullable ItemStack stack, @NotNull Material material) {
        return applyDamageNaturally(stack, material, null);
    }

    /**
     * Applys damage to the tool as if it has broken the given material
     *
     * @param stack    ItemStack to apply damage on
     * @param material Material the Item would break
     * @return damage that was dealt
     */
    public static int applyDamageNaturally(@Nullable ItemStack stack, @NotNull Material material, @Nullable Player player) {


        if (stack == null)
            return 0;
        ItemMeta meta = stack.getItemMeta();
        if (!(meta instanceof Damageable))
            return 0;
        Damageable damageable = (Damageable) meta;
        int damageDealt = 0;
        if (material.getHardness() == 0)
            return damageDealt;
        else if (!isMaterialTool(stack.getType()))
            return damageDealt;
            // Breaking Blocks with swords applies damage value 2
        else if (stack.getType().name().contains("SWORD"))
            damageDealt = 2;
        else if (stack.getType().equals(Material.FISHING_ROD)
                || stack.getType().equals(Material.FLINT_AND_STEEL))
            damageDealt = 0;
        else
            damageDealt = 1;
        int durabilityLevel = meta.getEnchantLevel(Enchantment.DURABILITY);
        if (durabilityLevel > 0) {
            // Chance of item to get damage
            float chanceOfDurabilityLoss = 100 / (float) (durabilityLevel + 1);
            if (ThreadLocalRandom.current().nextFloat(0, 1) > chanceOfDurabilityLoss)
                damageDealt = 0;
        }
        if (player != null) {
            PlayerItemDamageEvent playerItemDamageEvent = new PlayerItemDamageEvent(player, stack, damageDealt);
            Bukkit.getPluginManager().callEvent(playerItemDamageEvent);
            if (playerItemDamageEvent.isCancelled())
                return 0;
        }

        damageable.setDamage((damageable.getDamage() + damageDealt));
        stack.setItemMeta((ItemMeta) damageable);
        if (damageable.getDamage() > stack.getType().getMaxDurability()) {
            if (player != null)
                player.getInventory().removeItem(stack);
        }
        return damageDealt;
    }

    /**
     * Checks if a Material is listed as tool in minecraft client
     *
     * @param material Material to be checked
     * @return true if it is a tool
     */
    public static boolean isMaterialTool(@NotNull Material material) {
        if (material.name().contains("SWORD"))
            return true;
        else if (material.name().contains("PICKAXE"))
            return true;
        else if (material.name().contains("AXE"))
            return true;
        else if (material.name().contains("SHOVEL"))
            return true;
        else if (material.name().contains("HOE"))
            return true;
        else if (material.equals(Material.SHEARS))
            return true;
        else if (material.equals(Material.FISHING_ROD))
            return true;
        else return material.equals(Material.FLINT_AND_STEEL);
    }

    public static Material getSeedMaterial(@NotNull Material plantItem) {
        switch (plantItem) {
            case WHEAT:
                return Material.WHEAT_SEEDS;
            case BEETROOT:
                return Material.BEETROOT_SEEDS;
            case POTATO:
                return Material.POTATO;
            case CARROT:
                return Material.CARROT;
            case SUGAR_CANE:
                return Material.SUGAR_CANE;
            case SWEET_BERRY_BUSH:
                return Material.SWEET_BERRIES;
            case MELON_STEM:
                return Material.MELON_SEEDS;
            case PUMPKIN_STEM:
                return Material.PUMPKIN_SEEDS;
            case COCOA:
                return Material.COCOA_BEANS;
        }
        return plantItem;
    }

}
