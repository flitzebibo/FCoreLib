package me.FurH.Core.perm;

import me.FurH.Core.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 *
 * @author FurmigaHumana
 */
public class CorePermissions {
    
    private static ICorePermissions permissions;

    /**
     * Get the permissions bridge, it will setup the bridge if none is found
     * 
     * @param core the plugin calling this method
     * @return the permissions interface
     */
    public static ICorePermissions getPermissionsBridge(CorePlugin core) {
        
        if (permissions != null) {
            return permissions;
        }
        
        PluginManager pm = Bukkit.getPluginManager();

        Plugin plugin = pm.getPlugin("Vault");
        if (plugin != null && plugin.isEnabled()) {
            permissions = new CoreVault();
            core.log("[TAG] Vault hooked as permission plugin!");
        }

        return permissions;
    }
}