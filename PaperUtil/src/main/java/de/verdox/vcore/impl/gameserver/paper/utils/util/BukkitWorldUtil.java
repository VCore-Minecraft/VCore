/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util;


import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;

import java.util.HashSet;
import java.util.Set;

public class BukkitWorldUtil {

    public static boolean blockLocationEqual(Location loc1, Location loc2) {
        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

/*    public static boolean isRegionLoaded(WorldRegion worldRegion) {
        World world = Bukkit.getWorld(worldRegion.worldName);
        if (world == null)
            return false;
        for (WorldChunk chunk : worldRegion.getChunks()) {
            if (world.isChunkLoaded(chunk.x, chunk.z))
                return true;
        }
        return false;
    }

    public static boolean isRegionLoaded(String worldName, WorldRegion worldRegion) {
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return false;
        for (WorldChunk chunk : worldRegion.getChunks()) {
            if (world.isChunkLoaded(chunk.x, chunk.z))
                return true;
        }
        return false;
    }*/

    //TODO: Funktioniert noch nicht
    public static Block findStem(Block block) {
        if (hasBlockStem(block, BlockFace.NORTH))
            return block.getRelative(BlockFace.NORTH);
        else if (hasBlockStem(block, BlockFace.SOUTH))
            return block.getRelative(BlockFace.SOUTH);
        else if (hasBlockStem(block, BlockFace.EAST))
            return block.getRelative(BlockFace.EAST);
        else if (hasBlockStem(block, BlockFace.WEST))
            return block.getRelative(BlockFace.WEST);
        return block;
    }

    public static Material getWoolByColor(DyeColor dyeColor) {
        return Material.valueOf(dyeColor.name() + "_WOOL");
    }

/*    public static WorldChunk toWorldChunk(Chunk chunk) {
        return new WorldChunk(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static WorldChunk toWorldChunk(Location location) {
        return new WorldChunk(location.getWorld().getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static GameLocation toGameLocation(Chunk chunk) {
        return new GameLocation(chunk.getWorld().getName(), chunk.getX(), 70, chunk.getZ());
    }

    public static GameLocation toGameLocation(Location location) {
        return new GameLocation(location
                .getWorld()
                .getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(), location.getPitch());
    }*/

    private static boolean hasBlockStem(Block block, BlockFace stemBlockFaceLocation) {
        if (block == null)
            return false;
        if (stemBlockFaceLocation == null)
            return false;
        Block stem = block.getRelative(stemBlockFaceLocation);
        if (!stem.getType().equals(Material.ATTACHED_MELON_STEM) && !stem
                .getType()
                .equals(Material.ATTACHED_PUMPKIN_STEM))
            return false;
        Directional directional = (Directional) stem.getBlockData();
        BlockFace stemFacingTo = directional.getFacing();
        return stemFacingTo.getOppositeFace().equals(stemBlockFaceLocation);
    }

    public static Set<Chest> findConnectedChest(Chest chest) {
        Set<Chest> set = new HashSet<>();
        set.add(chest);
        org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) chest.getBlockData();
        BlockFace blockFace = chestData.getFacing();

        Location otherChestLocation = chest.getLocation().clone();
        switch (chestData.getType()) {
            default:
                return set;
            // Look at the Left side of the block for an other chest Block
            case LEFT: {
                if (blockFace.equals(BlockFace.NORTH)) {
                    otherChestLocation.add(1, 0, 0);
                    break;
                } else if (blockFace.equals(BlockFace.EAST)) {
                    otherChestLocation.add(0, 0, 1);
                    break;
                }
                if (blockFace.equals(BlockFace.SOUTH)) {
                    otherChestLocation.add(-1, 0, 0);
                    break;
                } else if (blockFace.equals(BlockFace.WEST)) {
                    otherChestLocation.add(0, 0, -1);
                    break;
                }
            }
            // Look at the Right side of the block for an other chest Block
            case RIGHT: {
                if (blockFace.equals(BlockFace.NORTH)) {
                    otherChestLocation.add(-1, 0, 0);
                    break;
                } else if (blockFace.equals(BlockFace.EAST)) {
                    otherChestLocation.add(0, 0, -1);
                    break;
                }
                if (blockFace.equals(BlockFace.SOUTH)) {
                    otherChestLocation.add(1, 0, 0);
                    break;
                } else if (blockFace.equals(BlockFace.WEST)) {
                    otherChestLocation.add(0, 0, 1);
                    break;
                }
            }
        }

        set.add((Chest) otherChestLocation.getBlock().getState(false));
        return set;
    }
}
