/*
 * Copyright (c) 2021. Lukas Jonsson
 */

package de.verdox.vcore.impl.gameserver.paper.utils.util;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;

public class GeometryUtil {

    public static double dCos(double degrees) {
        int dInt = (int) degrees;
        if (degrees == (double) dInt && dInt % 90 == 0) {
            dInt %= 360;
            if (dInt < 0) {
                dInt += 360;
            }

            switch (dInt) {
                case 0:
                    return 1.0D;
                case 90:
                    return 0.0D;
                case 180:
                    return -1.0D;
                case 270:
                    return 0.0D;
            }
        }

        return Math.cos(Math.toRadians(degrees));
    }

    public static double dSin(double degrees) {
        int dInt = (int) degrees;
        if (degrees == (double) dInt && dInt % 90 == 0) {
            dInt %= 360;
            if (dInt < 0) {
                dInt += 360;
            }

            switch (dInt) {
                case 0:
                    return 0.0D;
                case 90:
                    return 1.0D;
                case 180:
                    return 0.0D;
                case 270:
                    return -1.0D;
            }
        }

        return Math.sin(Math.toRadians(degrees));
    }

    public static Location rotateAround(Location point, Location rotateAround, double rotation) {
        if (rotation == 0)
            return point;
        rotation %= 360;
        float part = (float) ((rotation / 360.0f) * 2);

        double angle = part * Math.PI;

        double centerX = rotateAround.getX();
        double centerZ = rotateAround.getZ();

        double rotatedX = -Math.sin(angle) * (point.getZ() - centerZ) + Math.cos(angle) * (point.getX() - centerX) + centerX;
        double rotatedZ = Math.sin(angle) * (point.getX() - centerX) + Math.cos(angle) * (point.getZ() - centerZ) + centerZ;

        double newRotatedX = Math.round(rotatedX);
        double newRotatedZ = Math.round(rotatedZ);

        return new Location(point.getWorld(), newRotatedX, point.getY(), newRotatedZ);
    }

    public static Location rotatePointAround(Location point, Location rotateAround, double rotation) {

        // Point will be rotated around rotateAround

        float part = 0;
        if (rotation == 0) {
            return point;
        }
        if (rotation < 0)
            throw new IllegalArgumentException("Rotation can't be negative");
        if (rotation >= 360)
            rotation -= 360;
        switch ((int) rotation) {
            case 0:
                return point;
            case 90:
                part = 0.5f;
                break;
            case 180:
                part = 1f;
                break;
            case 270:
                part = 1.5f;
                break;
        }

        double angle = part * Math.PI;

        double centerX = rotateAround.getX();
        double centerZ = rotateAround.getZ();

        //- Math.sin(angle) * (point.getZ() - centerZ) + Math.cos(angle) * (point.getX() - centerX) + centerX

        double rotatedX = -Math.sin(angle) * (point.getZ() - centerZ) + Math.cos(angle) * (point.getX() - centerX) + centerX;
        double rotatedZ = Math.sin(angle) * (point.getX() - centerX) + Math.cos(angle) * (point.getZ() - centerZ) + centerZ;

        double newRotatedX = Math.round(rotatedX);
        double newRotatedZ = Math.round(rotatedZ);

        return new Location(point.getWorld(), newRotatedX, point.getY(), newRotatedZ);
    }

    public static BlockData rotateBlockData(BlockData blockData, double rotation) {
        if (blockData == null) {
            throw new IllegalArgumentException("blockData can't be null!");
        } else if (rotation < 0.0D) {
            throw new IllegalArgumentException("Rotation can't be negative");
        } else {
            if (rotation >= 360.0D) {
                rotation -= 360.0D;
            }

            if (blockData instanceof Rotatable) {
                Rotatable rotatable = (Rotatable) blockData.clone();
                rotatable.setRotation(rotateBlockface(rotatable.getRotation(), rotation));
                return rotatable;
            } else if (blockData instanceof Orientable) {
                Orientable orientable = (Orientable) blockData.clone();
                orientable.setAxis(rotateAxis(orientable.getAxis(), rotation));
                return orientable;
            } else if (blockData instanceof Directional) {
                Directional directional = (Directional) blockData.clone();
                directional.setFacing(rotateBlockface(directional.getFacing(), rotation));
                return directional;
            } else if (blockData instanceof MultipleFacing) {
                MultipleFacing multipleFacing = (MultipleFacing) blockData.clone();
                multipleFacing = rotateMultipleFacing(multipleFacing, rotation);
                return multipleFacing;
            } else {
                return blockData;
            }
        }
    }

    public static MultipleFacing rotateMultipleFacing(MultipleFacing multipleFacing, double rotation) {
        boolean oldNorth = multipleFacing.hasFace(BlockFace.NORTH);
        boolean oldEast = multipleFacing.hasFace(BlockFace.EAST);
        boolean oldSouth = multipleFacing.hasFace(BlockFace.SOUTH);
        boolean oldWest = multipleFacing.hasFace(BlockFace.WEST);
        switch ((int) rotation) {
            case 90:
                multipleFacing.setFace(BlockFace.NORTH, oldWest);
                multipleFacing.setFace(BlockFace.EAST, oldNorth);
                multipleFacing.setFace(BlockFace.SOUTH, oldEast);
                multipleFacing.setFace(BlockFace.WEST, oldSouth);
                return multipleFacing;
            case 180:
                multipleFacing.setFace(BlockFace.NORTH, oldSouth);
                multipleFacing.setFace(BlockFace.EAST, oldWest);
                multipleFacing.setFace(BlockFace.SOUTH, oldNorth);
                multipleFacing.setFace(BlockFace.WEST, oldEast);
                return multipleFacing;
            case 270:
                multipleFacing.setFace(BlockFace.NORTH, oldEast);
                multipleFacing.setFace(BlockFace.EAST, oldSouth);
                multipleFacing.setFace(BlockFace.SOUTH, oldWest);
                multipleFacing.setFace(BlockFace.WEST, oldNorth);
                return multipleFacing;
            default:
                multipleFacing.setFace(BlockFace.NORTH, oldNorth);
                multipleFacing.setFace(BlockFace.EAST, oldEast);
                multipleFacing.setFace(BlockFace.SOUTH, oldSouth);
                multipleFacing.setFace(BlockFace.WEST, oldWest);
                return multipleFacing;
        }
    }

    public static Axis rotateAxis(Axis axis, double rotation) {
        if (rotation == 270.0D || rotation == 90.0D) {
            if (axis == Axis.X) {
                return Axis.Z;
            }

            if (axis == Axis.Z) {
                return Axis.X;
            }
        }

        return axis;
    }

    public static BlockFace rotateBlockface(BlockFace blockFace, double rotation) {
        BlockFaceDirectionDegree blockFaceDirectionDegree = toBlockFaceDirectionDegree(blockFace);
        double originRotation = blockFaceDirectionDegree.getDegree();
        double newRotation = originRotation + rotation;
        if (newRotation >= 360.0D) {
            newRotation -= 360.0D;
        }

        return getBlockFaceByDegree(newRotation);
    }

    public static BlockFaceDirectionDegree toBlockFaceDirectionDegree(BlockFace blockFace) {
        return BlockFaceDirectionDegree.valueOf(blockFace.name());
    }

    public static BlockFace getBlockFaceByDegree(double degree) {
        BlockFaceDirectionDegree[] var2 = BlockFaceDirectionDegree.values();
        int var3 = var2.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            BlockFaceDirectionDegree blockFaceDirectionDegree = var2[var4];
            if (blockFaceDirectionDegree.getDegree() == degree) {
                return BlockFace.valueOf(blockFaceDirectionDegree.name());
            }
        }

        return BlockFace.NORTH;
    }

    enum BlockFaceDirectionDegree {
        NORTH(0.0D),
        NORTH_NORTH_EAST(22.5D),
        NORTH_EAST(45.0D),
        EAST_SOUTH_EAST(57.5D),
        EAST(90.0D),
        EAST_NORTH_EAST(112.5D),
        SOUTH_EAST(135.0D),
        SOUTH_SOUTH_EAST(157.5D),
        SOUTH(180.0D),
        SOUTH_SOUTH_WEST(202.5D),
        SOUTH_WEST(225.0D),
        WEST_SOUTH_WEST(247.5D),
        WEST(270.0D),
        WEST_NORTH_WEST(292.5D),
        NORTH_WEST(315.0D),
        NORTH_NORTH_WEST(337.5D),
        UP(0.0D),
        DOWN(0.0D);

        private final double degree;

        private BlockFaceDirectionDegree(double degree) {
            this.degree = degree;
        }

        public double getDegree() {
            return this.degree;
        }
    }

}
