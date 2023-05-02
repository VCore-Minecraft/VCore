/*
 * Copyright (c) 2022. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.event.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class BlockDataChangeListener implements Listener {
    private final JavaPlugin plugin;

    public BlockDataChangeListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean callBlockChangeState(Block block, @NotNull BlockData oldBlockData, @NotNull BlockData newBlockData, @NotNull BlockDataChangeEvent.Cause cause, @NotNull Event event) {
        BlockDataChangeEvent blockDataChangeEvent = new BlockDataChangeEvent(block, oldBlockData, newBlockData, cause, event);
        Bukkit.getPluginManager().callEvent(blockDataChangeEvent);
        return !blockDataChangeEvent.isCancelled();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBurnEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.BLOCK_BURN, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void explodeEvent(BlockExplodeEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.BLOCK_EXPLODE, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockFade(BlockFadeEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getNewState().getBlockData(), BlockDataChangeEvent.Cause.FADE, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockFertilize(BlockFertilizeEvent e) {
        for (BlockState blockState : e.getBlocks()) {
            boolean flag;
            flag = callBlockChangeState(blockState.getBlock(), blockState.getBlock().getBlockData(), blockState.getBlockData(), BlockDataChangeEvent.Cause.PLAYER_FERTILIZE_BLOCK, e);
            if (!flag) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockFertilize(StructureGrowEvent e) {
        for (BlockState blockState : e.getBlocks()) {
            boolean flag;
            flag = callBlockChangeState(blockState.getBlock(), blockState.getBlock().getBlockData(), blockState.getBlockData(), BlockDataChangeEvent.Cause.STRUCTURE_GROW, e);
            if (!flag) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockFromTo(BlockFromToEvent e) {
        if (e.getBlock().getType().equals(Material.DRAGON_EGG)) {
            e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.DRAGON_EGG_TELEPORT, e));
            e.setCancelled(!callBlockChangeState(e.getBlock(), e.getToBlock().getBlockData(), Bukkit.createBlockData(Material.DRAGON_EGG), BlockDataChangeEvent.Cause.DRAGON_EGG_TELEPORT, e));
        } else
            e.setCancelled(!callBlockChangeState(e.getBlock(), e.getToBlock().getBlockData(), e.getBlock().getBlockData(), BlockDataChangeEvent.Cause.FLUID_FLOW, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockGrow(BlockGrowEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getNewState().getBlockData(), BlockDataChangeEvent.Cause.GROW, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockIgnite(BlockIgniteEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getBlock().getBlockData(), BlockDataChangeEvent.Cause.IGNITED, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockPhysics(BlockPhysicsEvent e) {
        e.setCancelled(!callBlockChangeState(e.getSourceBlock(), e.getChangedBlockData(), e.getBlock().getBlockData(), BlockDataChangeEvent.Cause.PHYSICS, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void pistonExtend(BlockPistonExtendEvent e) {

        Block pistonBlockingBlock = e.getBlocks().stream().filter(block -> block.getPistonMoveReaction().equals(PistonMoveReaction.BLOCK)).findAny().orElse(null);
        if (pistonBlockingBlock != null)
            return;

        for (Block block : e.getBlocks()) {
            PistonMoveReaction pistonMoveReaction = block.getPistonMoveReaction();
            boolean flag;

            if (pistonMoveReaction == PistonMoveReaction.BREAK) {
                flag = callBlockChangeState(block, block.getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.PISTON_EXTEND, e);
            } else {
                Block blockBefore = block.getRelative(e.getDirection().getOppositeFace());
                Block blockAfter = block.getRelative(e.getDirection());
                // Piston that pushes
                if (blockBefore.getType().name().contains("PISTON") && !e.getBlocks().contains(blockBefore))
                    flag = callBlockChangeState(block, block.getBlockData(), Bukkit.createBlockData(Material.PISTON_HEAD), BlockDataChangeEvent.Cause.PISTON_EXTEND, e);
                else if (blockBefore.getPistonMoveReaction().equals(PistonMoveReaction.BREAK))
                    flag = callBlockChangeState(block, block.getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.PISTON_EXTEND, e);
                else
                    flag = callBlockChangeState(block, block.getBlockData(), Bukkit.createBlockData(blockBefore.getType()), BlockDataChangeEvent.Cause.PISTON_EXTEND, e);

                if (!e.getBlocks().contains(blockAfter)) {
                    flag = callBlockChangeState(block, blockAfter.getBlockData(), block.getBlockData(), BlockDataChangeEvent.Cause.PISTON_EXTEND, e);
                }
            }
            if (!flag) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void pistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks()) {
            PistonMoveReaction pistonMoveReaction = block.getPistonMoveReaction();

            BlockState airState = e.getBlock().getState(true);
            airState.setType(Material.AIR);

            boolean flag = true;

            if (pistonMoveReaction == PistonMoveReaction.BREAK)
                flag = callBlockChangeState(block, block.getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.PISTON_RETRACT, e);
            else if (pistonMoveReaction == PistonMoveReaction.BLOCK || pistonMoveReaction == PistonMoveReaction.PUSH_ONLY)
                flag = callBlockChangeState(block, block.getBlockData(), block.getBlockData(), BlockDataChangeEvent.Cause.PISTON_RETRACT, e);
            else {
                Block blockAfter = block.getRelative(e.getDirection());
                if (e.getBlocks().contains(blockAfter)) {
                    flag = callBlockChangeState(block, block.getBlockData(), blockAfter.getBlockData(), BlockDataChangeEvent.Cause.PISTON_RETRACT, e);
                } else {
                    boolean flag2 = callBlockChangeState(blockAfter, blockAfter.getBlockData(), block.getBlockData(), BlockDataChangeEvent.Cause.PISTON_RETRACT, e);
                    flag = flag2 && callBlockChangeState(block, block.getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.PISTON_RETRACT, e);
                }
            }
            if (!flag) {
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockPlace(BlockPlaceEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlockReplacedState().getBlockData(), e.getBlockPlaced().getBlockData(), BlockDataChangeEvent.Cause.PLAYER_BLOCK_PLACE, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockReceiveGameEvent(BlockReceiveGameEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getBlock().getBlockData(), BlockDataChangeEvent.Cause.BLOCK_RECEIVE_GAME_EVENT, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockBreak(BlockBreakEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.PLAYER_BLOCK_BREAK, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityChangeBlock(EntityChangeBlockEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getTo().createBlockData(), BlockDataChangeEvent.Cause.ENTITY_CHANGE_BLOCK, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void blockDestroyEvent(BlockDestroyEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getNewState(), BlockDataChangeEvent.Cause.TRIGGERED_DESTRUCTION, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void emptyBucket(PlayerBucketEmptyEvent e) {
        Material fluidMaterial;
        if (e.getBucket().equals(Material.LAVA_BUCKET))
            fluidMaterial = Material.LAVA;
        else
            fluidMaterial = Material.WATER;
        e.setCancelled(!callBlockChangeState(e.getBlock(), Bukkit.createBlockData(Material.AIR), Bukkit.createBlockData(fluidMaterial), BlockDataChangeEvent.Cause.PLAYER_EMPTY_BUCKET, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void emptyBucket(PlayerBucketFillEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.PLAYER_FILL_BUCKET, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void emptyBucket(FluidLevelChangeEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getNewData(), BlockDataChangeEvent.Cause.FLUID_LEVEL_CHANGE, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onLeavesDecay(LeavesDecayEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), Bukkit.createBlockData(Material.AIR), BlockDataChangeEvent.Cause.LEAVES_DECAY, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onMoistureChange(MoistureChangeEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getNewState().getBlockData(), BlockDataChangeEvent.Cause.MOISTURE_CHANGE, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSignChange(SignChangeEvent e) {
        e.setCancelled(!callBlockChangeState(e.getBlock(), e.getBlock().getBlockData(), e.getBlock().getBlockData(), BlockDataChangeEvent.Cause.SIGN_CHANGE, e));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSpongeAbsorb(SpongeAbsorbEvent e) {
        for (BlockState blockState : e.getBlocks()) {
            boolean flag;
            flag = callBlockChangeState(blockState.getBlock(), blockState.getBlock().getBlockData(), blockState.getBlockData(), BlockDataChangeEvent.Cause.SPONGE_ABSORB, e);
            if (!flag) {
                e.setCancelled(true);
                return;
            }
        }
    }
}
