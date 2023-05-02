/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util;

import org.bukkit.block.Biome;


public class VanillaUtil {

    public static int getBiomeID_1_17(Biome biome) {
        return getBiomeID_1_16(biome);
    }

    public static int getBiomeID_1_16(Biome biome) {
        return switch (biome) {
            case BADLANDS -> 37;
            case BEACH -> 16;
            case DEEP_OCEAN -> 24;
            case RIVER -> 7;
            //case BADLANDS_PLATEAU -> 39;
            case SWAMP -> 6;
            case TAIGA -> 5;
            case BAMBOO_JUNGLE -> 168;
            //case BAMBOO_JUNGLE_HILLS -> 169;
            case BASALT_DELTAS -> 173;
            case SOUL_SAND_VALLEY -> 170;
            case THE_END -> 9;
            case END_HIGHLANDS -> 42;
            case END_BARRENS -> 43;
            case END_MIDLANDS -> 41;
            case THE_VOID -> 127;
            case BIRCH_FOREST -> 27;
            //case BIRCH_FOREST_HILLS -> 28;
            case COLD_OCEAN -> 46;
            case CRIMSON_FOREST -> 171;
            case DARK_FOREST -> 29;
            //case DARK_FOREST_HILLS -> 157;
            case DEEP_COLD_OCEAN -> 49;
            case DEEP_FROZEN_OCEAN -> 50;
            case DEEP_LUKEWARM_OCEAN -> 48;
            //case DEEP_WARM_OCEAN -> 47;
            case DESERT -> 2;
            //case DESERT_HILLS -> 17;
            //case DESERT_LAKES -> 130;
            case ERODED_BADLANDS -> 165;
            case FLOWER_FOREST -> 132;
            case FOREST -> 4;
            case FROZEN_OCEAN -> 10;
            case FROZEN_RIVER -> 11;
            //case GIANT_SPRUCE_TAIGA -> 160;
            //case GIANT_SPRUCE_TAIGA_HILLS -> 161;
            //case GIANT_TREE_TAIGA -> 32;
            //case GIANT_TREE_TAIGA_HILLS -> 33;
            //case GRAVELLY_MOUNTAINS -> 131;
            //case MODIFIED_GRAVELLY_MOUNTAINS -> 162;
            case ICE_SPIKES -> 140;
            case JUNGLE -> 21;
            //case JUNGLE_EDGE -> 23;
            //case JUNGLE_HILLS -> 22;
            case LUKEWARM_OCEAN -> 45;
            //case MODIFIED_BADLANDS_PLATEAU -> 167;
            //case MODIFIED_JUNGLE -> 149;
            //case MODIFIED_JUNGLE_EDGE -> 151;
            //case WOODED_BADLANDS_PLATEAU -> 38;
            //case MODIFIED_WOODED_BADLANDS_PLATEAU -> 166;
            case PLAINS -> 1;
            //case MOUNTAIN_EDGE -> 20;
            //case MOUNTAINS -> 3;
            //case MUSHROOM_FIELD_SHORE -> 15;
            case MUSHROOM_FIELDS -> 14;
            case NETHER_WASTES -> 8;
            case SAVANNA -> 35;
            case SAVANNA_PLATEAU -> 36;
            //case SHATTERED_SAVANNA -> 163;
            //case SHATTERED_SAVANNA_PLATEAU -> 164;
            case SMALL_END_ISLANDS -> 40;
            case SNOWY_BEACH -> 26;
            //case SNOWY_MOUNTAINS -> 13;
            case SNOWY_TAIGA -> 30;
            //case SNOWY_TAIGA_HILLS -> 31;
            //case SNOWY_TAIGA_MOUNTAINS -> 158;
            //case SNOWY_TUNDRA -> 12;
            //case STONE_SHORE -> 25;
            case WARM_OCEAN -> 44;
            case SUNFLOWER_PLAINS -> 129;
            //case SWAMP_HILLS -> 134;
            //case TAIGA_HILLS -> 19;
            //case TAIGA_MOUNTAINS -> 133;
            //case TALL_BIRCH_FOREST -> 155;
            //case TALL_BIRCH_HILLS -> 156;
            case WARPED_FOREST -> 172;
            //case WOODED_HILLS -> 18;
            //case WOODED_MOUNTAINS -> 34;
            default -> 0;
        };
    }
}
