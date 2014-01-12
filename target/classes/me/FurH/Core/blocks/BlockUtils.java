package me.FurH.Core.blocks;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Attachable;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class BlockUtils {
    
    /**
     * Removes all blocks of the given type inside the defined radius of the centered block
     *
     * @param b the centered block
     * @param radius the radius
     * @param material the block type to remove
     */
    public static void removeTypeAround(Block b, int radius, Material material) {
        
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    Location center = new Location(b.getWorld(), b.getX() + x, b.getY() + y, b.getZ() + z);
                    if (center.distanceSquared(b.getLocation()) > radius) {
                        continue;
                    }

                    if (center.getBlock().getType() == material) {
                        center.getBlock().setType(Material.AIR);
                    }
                }
            }
        }
        
    }
    
    /**
     * Get all blocks attached to another one
     *
     * @param block the block to check for attachements
     * @return a list with all attached blocks, might be empty but never null.
     */
    public static List<Block> getAttachedBlock(Block block) {
        List<Block> blocks = new ArrayList<Block>();

        BlockFace[] faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST };
        for (BlockFace face : faces) {
            Block relative = block.getRelative(face);

            if (relative.getState().getData() instanceof Attachable) {
                Attachable a = (Attachable) relative.getState().getData();
                Block attached = relative.getRelative(a.getAttachedFace());

                if (attached.getLocation().equals(block.getLocation())) {
                    blocks.add(relative);
                }

            } else
            if (face.equals(BlockFace.UP) && relative.getType() != Material.AIR && !relative.getType().isSolid()) {
                blocks.add(relative);
            }
        }
        
        return blocks;
    }
}
