package me.FurH.Core.perm;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author FurmigaHumana
 */
public class CoreVault implements ICorePermissions {
    
    private Permission permission;
    private Chat chat;
    
    public CoreVault() {

        RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (chatProvider != null) {
            chat = chatProvider.getProvider();
        }
    }

    @Override
    public boolean has(CommandSender sender, String node) {
        return permission.has(sender, node);
    }
    
    @Override
    public String getPlayerPrefix(Player player) {
        return chat.getPlayerPrefix(player);
    }
    
    @Override
    public String getPlayerSuffix(Player player) {
        return chat.getPlayerSuffix(player);
    }

    @Override
    public String getGroupPrefix(Player player) {
        return chat.getGroupPrefix(player.getWorld(), permission.getPrimaryGroup(player));
    }

    @Override
    public String getGroupSuffix(Player player) {
        return chat.getGroupSuffix(player.getWorld(), permission.getPrimaryGroup(player));
    }
}
