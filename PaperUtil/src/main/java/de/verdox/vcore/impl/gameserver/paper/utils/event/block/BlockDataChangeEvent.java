/*
 * Copyright (c) 2022. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.event.block;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.jetbrains.annotations.NotNull;


public class BlockDataChangeEvent extends BlockEvent {
    private static final HandlerList handlers = new HandlerList();
    private final boolean materialChanged;
    private final BlockData oldBlockState;
    private final BlockData newBlockState;
    private final Cause cause;
    private final Event event;
    private boolean cancelled;

    public BlockDataChangeEvent(@NotNull Block theBlock, BlockData oldBlockState, BlockData newBlockState, @NotNull Cause cause, @NotNull Event event) {
        super(theBlock);
        if (!cause.getEvent().isAssignableFrom(event.getClass()))
            throw new IllegalStateException("Wrong Event type. Only " + cause.getEvent() + " allowed");
        this.cause = cause;
        this.event = event;
        this.materialChanged = !oldBlockState.getMaterial().equals(newBlockState.getMaterial());
        this.oldBlockState = oldBlockState;
        this.newBlockState = newBlockState;
    }

    public Class<? extends Event> getUnderlyingEventClass() {
        return cause.getEvent();
    }

    public <T extends Event> T castEvent(Class<? extends T> typeClass) {
        return typeClass.cast(event);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Cause getCause() {
        return cause;
    }

    public boolean isMaterialChanged() {
        return materialChanged;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public BlockData getOldBlockState() {
        return oldBlockState;
    }

    public BlockData getNewBlockState() {
        return newBlockState;
    }

    public enum Cause {
        BLOCK_BURN(BlockBurnEvent.class),
        PLAYER_BLOCK_BREAK(BlockBreakEvent.class),
        PLAYER_BLOCK_PLACE(BlockPlaceEvent.class),
        BLOCK_RECEIVE_GAME_EVENT(BlockReceiveGameEvent.class),
        PLAYER_EMPTY_BUCKET(PlayerBucketEmptyEvent.class),
        IGNITED(BlockIgniteEvent.class),
        PHYSICS(BlockPhysicsEvent.class),
        PLAYER_FILL_BUCKET(PlayerBucketFillEvent.class),
        PISTON_RETRACT(BlockPistonRetractEvent.class),
        PISTON_EXTEND(BlockPistonExtendEvent.class),
        FLUID_FLOW(BlockFromToEvent.class),
        DRAGON_EGG_TELEPORT(BlockFromToEvent.class),
        BLOCK_EXPLODE(BlockExplodeEvent.class),
        ENTITY_CHANGE_BLOCK(EntityChangeBlockEvent.class),
        TRIGGERED_DESTRUCTION(BlockDestroyEvent.class),
        GROW(BlockGrowEvent.class),
        STRUCTURE_GROW(StructureGrowEvent.class),
        PLAYER_FERTILIZE_BLOCK(BlockFertilizeEvent.class),
        FADE(BlockFadeEvent.class),
        LEAVES_DECAY(LeavesDecayEvent.class),
        MOISTURE_CHANGE(MoistureChangeEvent.class),
        FLUID_LEVEL_CHANGE(FluidLevelChangeEvent.class),
        SIGN_CHANGE(SignChangeEvent.class),
        SPONGE_ABSORB(SpongeAbsorbEvent.class),
        ;
        private final Class<? extends Event> event;

        Cause(Class<? extends Event> event) {
            this.event = event;
        }

        public Class<? extends Event> getEvent() {
            return event;
        }
    }
}
