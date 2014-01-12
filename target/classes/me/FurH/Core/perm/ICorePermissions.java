package me.FurH.Core.perm;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public interface ICorePermissions {

    /**
     * Check if the player has the given permission
     * 
     * @param sender the player
     * @param node the permission node
     * @return true if the player has the permission, false otherwise
     */
    public boolean has(CommandSender sender, String node);
    
    /**
     * Get the player chat prefix
     * 
     * @param player the player to get the prefix
     * @return the player prefix
     */
    public String getPlayerPrefix(Player player);
    
    /**
     * Get the player chat suffix
     * 
     * @param player the player to get the suffix
     * @return the player suffix
     */
    public String getPlayerSuffix(Player player);
    
    /**
     * Get the group chat prefix
     * 
     * @param player the player to get the group to get the prefix
     * @return the group prefix
     */
    public String getGroupPrefix(Player player);
    
    /**
     * Get the group chat suffix
     * 
     * @param player the player to get the group to get the suffix
     * @return the group suffix
     */
    public String getGroupSuffix(Player player);
        
}
