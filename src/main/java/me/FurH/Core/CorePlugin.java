package me.FurH.Core;

import me.FurH.Core.database.CoreSQLDatabase;
import me.FurH.Core.gc.MemoryMonitor;
import me.FurH.Core.perm.CorePermissions;
import me.FurH.Core.perm.ICorePermissions;
import me.FurH.Core.util.Communicator;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author FurmigaHumana
 */
public abstract class CorePlugin extends JavaPlugin {

    private static CorePlugin           handler;
    
    public CoreSQLDatabase              coredatabase;
    public  static Thread               main_thread;
    private static ICorePermissions     permissions;
    private Communicator                communicator;
    private static MemoryMonitor        monitor;
    public static long start            = 0;

    /**
     * Initializes a new CorePlugin Objects
     * 
     * @param tag the default chat tag to be used
     */
    public CorePlugin(String tag) {
        setup(tag);
    }

    /**
     * Initializes a new CorePlugin Objects
     * 
     * @param tag the default chat tag to be used
     * @param inbound whatever the plugin should or should not hook the player inboud queue
     * @param outbound whatever the plugin should or should not hook the player outbound queue 
     */    
    private void setup(String tag) {

        if (handler == null) {
            handler = this;
        }

        this.communicator = new Communicator(handler, tag);

        if (start == 0) {
            start = System.currentTimeMillis();
        }
        
        if (main_thread == null) {
            main_thread = Thread.currentThread();
        }
        
        if (permissions == null) {
            permissions = CorePermissions.getPermissionsBridge(this);
        }

        if (monitor == null) {
            monitor = new MemoryMonitor();
        }

    }

    /**
     * Check if the player has an permission
     *
     * @param sender the player to check
     * @param node the permission node
     * @return true if the player has permission, otherwise false
     */
    public static boolean hasPermS(CommandSender sender, String node) {

        if (permissions == null) {
            return sender.hasPermission(node);
        }

        return permissions.has(sender, node);
    }
    
    /**
     * Check if the player has an permission
     *
     * @param sender the player to check
     * @param node the permission node
     * @return true if the player has permission, otherwise false
     */
    public boolean hasPerm(CommandSender sender, String node) {
        return hasPermS(sender, node);
    }
    
    /**
     * Send a message to the command sender or to a player
     * 
     * @param sender the command sender or the player
     * @param message the message to display
     * @param objects the message objects
     */
    public void msg(CommandSender sender, String message, Object...objects) {
        communicator.msg(sender, message, objects);
    }
    
    /**
     * Log the plugin enabled message with ms count
     * @param took the total ms count
     */
    public void logEnable(long took) {
        log("[TAG] {0} v{1} loaded in {2} ms! Using edition by kangarko!", getDescription().getName(), getDescription().getVersion(), took);
    }
    
    /**
     * Log the default plugin enabled message
     */
    public void logEnable() {
        log("[TAG] {0} v{1} loaded!", getDescription().getName(), getDescription().getVersion());
    }
    
    /**
     * Log the plugin disabled message with ms count
     * @param took the total ms count
     */
    public void logDisable(long took) {
        log("[TAG] {0} v{1} disabled in {2} ms!", getDescription().getName(), getDescription().getVersion(), took);
    }
    
    /**
     * Log the default plugin disabled message
     */
    public void logDisable() {
        log("[TAG] {0} v{1} disabled!", getDescription().getName(), getDescription().getVersion());
    }
    
    /**
     * Log a message as info
     * 
     * @param message the message to display
     * @param objects the message objects
     */
    public void log(String message, Object...objects) {
        communicator.log(message, objects);
    }
    
    /**
     * Handle an exception
     * 
     * @param ex the exception
     */
    public void error(Throwable ex) {
        communicator.error(ex);
    }
    
    /**
     * Handle and exception
     * 
     * @param ex the exception
     * @param message the message to display in console
     * @param objects the message objects
     */
    public void error(Throwable ex, String message, Object...objects) {
        communicator.error(ex, message, objects);
    }

    /**
     * Get the CorePlugin default Communicator
     *
     * @return the communicator
     */
    public Communicator getCommunicator() {
        return communicator;
    }
    
    /**
     * Get the ICorePermissions bridge
     *
     * @return the ICorePermissions objects, might be null.
     */
    public static ICorePermissions getPermissions() {
        return permissions;
    }
    
    public static MemoryMonitor getMemoryMonitor() {
        return monitor;
    }

    public static CorePlugin getHandler() {
        return handler;
    }
}