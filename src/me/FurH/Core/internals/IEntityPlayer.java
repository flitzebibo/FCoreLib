package me.FurH.Core.internals;

import me.FurH.Core.exceptions.CoreException;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public interface IEntityPlayer {
    
    /**
     * Set the Player of this IEntityPlayer object
     * 
     * @param player the player to be set
     * @return the IEntityPlayer for the given Player
     */
    public IEntityPlayer setEntityPlayer(Player player);
    
    /**
     * Get the Player NetworkManager Object
     * 
     * @throws CoreException 
     */
    public void setInboundQueue() throws CoreException;
        
    /**
     * Get the Player network ping
     * 
     * @return the player ping in milliseconds
     */
    public int ping();
    
    /**
     * Send the EntityPlayer a custom payload
     *
     * @param packet the custom payload
     */
    public void sendPacket(PacketCustomPayload packet);
    
    /**
     * Hides the player inventory
     * 
     * @ATTENTION: You must cancel inventory click event in way to prevent the inventory from revealing itself
     */
    public void hideInventory();
    
    /**
     * Revels the player inventory
     */
    public void unHideInventory();
}
