package me.FurH.Core.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.internals.InternalManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class PlayerUtils {

    /**
     * Get the players ping average
     * 
     * @return the players ping average
     * @throws CoreException  
     */
    public static int getPingAverage() throws CoreException {
        List<Integer> pings = new ArrayList<Integer>();

        for (Player p : Bukkit.getOnlinePlayers()) {

            int ping = getPing(p);
            
            if (ping > 0) {
                pings.add(ping);
            }
        }

        return getAverage(pings.toArray(new Integer[] { }));
    }
    
    /**
     * Get the average number of all the givem values
     * 
     * @param values the integer array with all the values
     * @return the average
     */
    public static int getAverage(Integer[] values) {
        int sum = 0;
        
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }
        
        return (int) (((double) sum) / values.length);
    }

    public static double getAverage(Double[] values) {
        double sum = 0;

        for (int i = 0; i < values.length; i++) {
            sum += values[i];
        }

        return (sum / ((double) values.length));
    }

    /**
     * Get the player ping
     * 
     * @param p the player
     * @return the player ping
     * @throws CoreException  
     */
    public static int getPing(Player p) throws CoreException {
        return InternalManager.getEntityPlayer(p, true).ping();
    }

    /**
     * Get if the player is online
     * 
     * @param player the player
     * @return true if there is already a player with the same name online, false otherwise.
     */
    public static boolean isOnline(Player player) {

        for (Player p : player.getServer().getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(player.getName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get if the player has invalid characters in his nickname
     * 
     * @param name the player name
     * @return true if the player name is valid, false otherwise.
     */
    public static boolean isValidName(String name) {
        return name.replaceAll("[^ a-zA-Z0-9_]", "").equals(name);
    }

    /**
     * Teleports the player to an safe location
     * 
     * @param p the player to teleport
     */
    public static void toSafeLocation(Player p) {
        Location loc = p.getLocation().subtract(0, 1, 0);
        Block block = loc.getBlock();

        if (isSafeBlock(block)) {
            return;
        }

        int stack = 256;
        while (stack > 0 && !block.getType().isSolid()) {
            block = block.getRelative(BlockFace.DOWN);
            stack--;
        }

        Block[] blocks = new Block[]{
            block,
            block.getRelative(BlockFace.NORTH),
            block.getRelative(BlockFace.SOUTH),
            block.getRelative(BlockFace.WEST),
            block.getRelative(BlockFace.EAST)
        };

        for (Block process : blocks) {

            stack = 2;
            while (stack > 0 && !isSafeBlock(process)) {

                if (isFloorBlock(process.getRelative(BlockFace.DOWN))) {
                    process = process.getRelative(BlockFace.UP);
                } else {
                    process = process.getRelative(BlockFace.DOWN);
                }

                stack--;
            }

            if (isSafeBlock(process)) {
                block = process;
                break;
            }
        }

        if (!isSafeBlock(block)) {
            block = getBlockUp(block);
        }

        if (!isSafeBlock(block)) {
            BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};
            for (BlockFace face : faces) {
                Block relative = block.getRelative(face).getRelative(BlockFace.DOWN);

                stack = 256;
                while (stack > 0 && isUnsafeBlock(relative)) {
                    relative = relative.getRelative(face);
                    stack--;
                }

                if (!isUnsafeBlock(relative)) {
                    block = relative;
                    break;
                }
            }
        }

        if (!isSafeBlock(block)) {
            block = getBlockUp(block);
        }
        
        if (!isSafeBlock(block)) {
            p.teleport(p.getWorld().getSpawnLocation());
            return;
        }

        Location newLoc = block.getRelative(BlockFace.UP).getLocation();

        newLoc.setPitch(p.getLocation().getPitch());
        newLoc.setYaw(p.getLocation().getYaw());

        p.teleport(newLoc);
    }

    private static Block getBlockUp(Block block) {
        int stack = 256;
        while (stack > 0 && !isSafeBlock(block)) {

            if (isFloorBlock(block.getRelative(BlockFace.DOWN))) {
                block = block.getRelative(BlockFace.UP).getRelative(BlockFace.UP);
            } else {
                block = block.getRelative(BlockFace.DOWN);
            }

            stack--;
        }
        return block;
    }

    private static boolean isSafeBlock(Block block) {
        return block.getType() != Material.AIR && !isUnsafeBlock(block)
                && !block.getRelative(BlockFace.UP).getType().isSolid() && !isUnsafeBlock(block.getRelative(BlockFace.UP))
                && !block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType().isSolid()
                && !isUnsafeBlock(block.getRelative(BlockFace.UP).getRelative(BlockFace.UP));
    }

    private static boolean isUnsafeBlock(Block block) {
        return block.getTypeId() == 10 || block.getTypeId() == 11 || block.getTypeId() == 51 || block.getTypeId() == 119;
    }

    private static boolean isFloorBlock(Block block) {
        return block.getType() != Material.AIR;
    }
}