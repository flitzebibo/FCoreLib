package me.FurH.Core;

import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.internals.InternalManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class CoreMainListener implements Listener {
    
    private boolean inbound;
    private boolean outbound;
    
    public CoreMainListener(boolean inbound, boolean outbound) {
        this.inbound = inbound;
        this.outbound = outbound;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {
        
        try {
            if (inbound) {
                InternalManager.getEntityPlayer(e.getPlayer(), false).setInboundQueue();
            }
        } catch (CoreException ex) {
            ex.printStackTrace();
        }
        
        try {
            if (outbound) {
                InternalManager.getEntityPlayer(e.getPlayer(), false).setOutboundQueue();
            }
        } catch (CoreException ex) {
            ex.printStackTrace();
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent e) {
        InternalManager.removeEntityPlayer(e.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerQuit(PlayerKickEvent e) {
        InternalManager.removeEntityPlayer(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!e.isCancelled()) {
            try {
                
                if (!(e.getWhoClicked() instanceof Player)) {
                    return;
                }
                
                Player p = (Player) e.getWhoClicked();
                
                if (InternalManager.getEntityPlayer(p, true).isInventoryHidden()) {
                    e.setCancelled(true);
                }
                
            } catch (CoreException ex) {
                ex.printStackTrace();
            }
        }
    }
}