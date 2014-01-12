package me.FurH.Core.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import me.FurH.Core.CorePlugin;
import me.FurH.Core.database.CoreSQLDatabase;
import me.FurH.Core.database.CoreSQLDatabase.type;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.time.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author FurmigaHumana
 */
public class Communicator {
    
    private boolean communicator_quiet = false;
    private boolean communicator_debug = false;

    private String tag = "&8[&aFCoreLib&8]&7:";
    private CorePlugin plugin;

    /**
     * Set the communicator quiet mode
     * 
     * @param communicator_quiet the quiet mode state, if true, no messages wil be shown to players, only in console.
     */
    public void setQuiet(boolean communicator_quiet) {
        this.communicator_quiet = communicator_quiet;
    }
    
    /**
     *  Set the communicator debug mode
     * 
     * @param communicator_debug the debug mode state, if true, debug messages will be shown, otherwise not.
     */
    public void setDebug(boolean communicator_debug) {
        this.communicator_debug = communicator_debug;
    }

    /**
     * Set the communicator tag
     * 
     * @param tag the string used to replace the [TAG] object
     */
    public void setTag(String tag) {
        this.tag = tag;
    }
    
    /**
     * Creates a new Communicator object
     * 
     * @param plugin the CorePlugin object used to handle this communicator
     * @param tag the default tag
     */
    public Communicator(CorePlugin plugin, String tag) {
        this.plugin = plugin;
        this.tag = tag;
    }

    /**
     * Broadcast a message to all online players
     * 
     * @param message the message to display
     * @param console if true the message will be shown in the console too
     * @param objects the message objects
     */
    public void broadcast(String message, boolean console, Object...objects) {
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            msg(player, message, objects);
        }

        if (console) {
            log(message, objects);
        }
    }
    
    /**
     * Broadcast a message to all players with a permission
     * 
     * @param message the message to display
     * @param permission the permission node to be check
     * @param console if true the message will be shown in the console too
     * @param objects the message objects
     */
    public void broadcast(String message, String permission, boolean console, Object...objects) {

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                msg(player, message, objects);
            }
        }

        if (console) {
            log(message, objects);
        }
    }

    /**
     * Send a message to the command sender or to a player
     * 
     * @param sender the command sender or the player
     * @param message the message to display
     * @param objects the message objects
     */
    public void msg(CommandSender sender, String message, Object...objects) {

        if (message == null || "".equals(message)) {
            return;
        }

        if (sender != null && !communicator_quiet) {
            sender.sendMessage(format(message, objects));
        } else {
            log(message, objects);
        }

    }
    
    /**
     * Handle an exception
     * 
     * @param ex the exception
     */
    public void error(Throwable ex) {

        String message = ex.getMessage();
        if (message == null) {
            message = "error";
        }
        
        error(ex, message);
    }
    
    /**
     * Handle and exception
     * 
     * @param ex the exception
     * @param message the message to display in console
     * @param objects the message objects
     */
    public void error(Throwable ex, String message, Object...objects) {

        if (!(ex instanceof CoreException)) {
            ex = new CoreException(ex, message);
        }
        
        message = format(message, objects);
        log(message, LogType.SEVERE, objects);

        log("[TAG] This error is avaliable at: plugins/{0}/error/error-{1}.txt", LogType.SEVERE, plugin.getDescription().getName(), stack((CoreException) ex));
    }

    /**
     * Log a message as severe
     * 
     * @param message the message to display
     * @param objects the message objects
     */
    public void severe(String message, Object...objects) {
        log(message, LogType.SEVERE, objects);
    }

    /**
     * Log a message as warning
     * 
     * @param message the message to display
     * @param objects the message objects
     */
    public void warning(String message, Object...objects) {
        log(message, LogType.WARNING, objects);
    }

    /**
     * Log a message as debug
     * 
     * @param message the message to display
     * @param objects the message objects
     */
    public void debug(String message, Object...objects) {
        log(message, LogType.DEBUG, objects);
    }

    /**
     * Log a message as info
     * 
     * @param message the message to display
     * @param objects the message objects
     */
    public void log(String message, Object...objects) {
        log(message, LogType.INFO, objects);
    }

    /**
     * Log a message with a specific type
     * 
     * @param message the message to display
     * @param type the type of the log
     * @param objects the message objects
     */
    public void log(String message, LogType type, Object... objects) {
        if (message == null || "".equals(message)) { return; }
        
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

        if (type == LogType.INFO) {
            console.sendMessage(format(message, objects));
        } else
        if (type == LogType.SEVERE) {
            console.sendMessage(format("&4"+message, objects));
        } else
        if (type == LogType.WARNING) {
            console.sendMessage(format("&5"+message, objects));
        } else
        if (type == LogType.DEBUG && communicator_debug) {
            console.sendMessage(format("&3"+message, objects));
        }
    }

    /**
     * Replace the color codes to actually colors
     * 
     * @param message the message to handle
     * @return the message with colors
     */
    public String colors(String message) {
        return message.replaceAll("&([0-9a-fk-or])", "\u00a7$1");
    }

    /**
     * Format the message with the objects and colors
     *
     * @param message the message to handle
     * @param objects the message objects
     * @return the formated message
     */
    public String format(String message, Object...objects) {
        
        if (objects != null && objects.length > 0) {
            message = MessageFormat.format(message, objects);
        }

        if (message.contains("[TAG]")) {
            message = message.replaceAll("\\[TAG\\]", tag);
        }

        return colors(message);
    }
    
    /**
     * Write an CoreException to a file
     * 
     * @param ex the CoreException object
     * @return the name of the file used to write the error
     */
    private String stack(CoreException ex) {
        String format1 = TimeUtils.getSimpleFormatedTimeWithMillis(System.currentTimeMillis());

        File data = new File(plugin.getDataFolder() + File.separator + "error");
        if (!data.exists()) { data.mkdirs(); }

        data = new File(data.getAbsolutePath(), "error-"+format1+".txt");
        if (!data.exists()) {
            try {
                data.createNewFile();
            } catch (IOException e) {
                log("Failed to create new log file, {0} .", e.getMessage());
            }
        }

        try {

            String l = System.getProperty("line.separator");

            String format2 = TimeUtils.getFormatedTime(System.currentTimeMillis());
            FileWriter fw = new FileWriter(data, true);
            BufferedWriter bw = new BufferedWriter(fw);
            Runtime runtime = Runtime.getRuntime();
            
            File root = new File("/");

            int creative = 0;
            int survival = 0;
            int totalp = Bukkit.getOnlinePlayers().length;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode().equals(GameMode.CREATIVE)) {
                    creative++;
                } else {
                    survival++;
                }
            }
            
            CoreSQLDatabase db = plugin.coredatabase;
            
            StackTraceElement[] thread1 = CorePlugin.main_thread.getStackTrace();
            StackTraceElement[] core = ex.getCoreStackTrace();
            StackTraceElement[] error = ex.getStackTrace();
            StackTraceElement[] thread2 = ex.getThreadStackTrace();
            
            bw.write(format2 +l);
            bw.write("	=============================[ ERROR INFORMATION ]============================="+l);
            bw.write("	- Plugin: " + plugin.getDescription().getFullName() +l);
            bw.write("	- Uptime: " + Utils.getServerUptime()+l);
            bw.write("	- Players: "+totalp+" ("+creative+" Creative, "+survival+" Survival)"+l);
            bw.write("	=============================[ HARDWARE SETTINGS ]============================="+l);
            bw.write("		Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " " + System.getProperty("java.vendor.url") +l);
            bw.write("		System: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") +l);
            bw.write("		Processors: " + runtime.availableProcessors() +l);
            bw.write("		Memory: "+l);
            bw.write("			Free: " + Utils.getFormatedBytes(runtime.freeMemory()) +l);
            bw.write("			Total: " + Utils.getFormatedBytes(runtime.totalMemory()) +l);
            bw.write("			Max: " + Utils.getFormatedBytes(runtime.maxMemory()) +l);
            bw.write("		Storage: "+l);
            bw.write("			Total: " + Utils.getFormatedBytes(root.getTotalSpace()) +l);
            bw.write("			Free: " + Utils.getFormatedBytes(root.getFreeSpace()) +l);
            if (db != null) {
                bw.write("	=============================[ SQL INFORMATIONS ]============================="+l);
                bw.write("		Server Type: " + db.type.toString() +l);
                bw.write("		LocalHost: " + db.isLocalHost() +l);
                bw.write("		Queue speed: " + db.queue_speed +l);
                bw.write("		Queue threads: " + db.queue_threads +l);
                bw.write("		Database ping: " + (db.type == type.MySQL ? db.ping() : "<0") +l);
                bw.write("		Database version: " + (db.version) +l);
                bw.write("              Is Update Available: " + db.isUpdateAvailable() + l);
            }
            bw.write("	=============================[ INSTALLED PLUGINS ]============================="+l);
            bw.write("	Plugins:"+l);
            for (Plugin x : Bukkit.getServer().getPluginManager().getPlugins()) {
                bw.write("		- " + x.getDescription().getFullName() +l);
            }
            bw.write("	=============================[  LOADED   WORLDS  ]============================="+l);
            bw.write("	Worlds:"+l);
            for (World w : Bukkit.getServer().getWorlds()) {
                bw.write("		" + w.getName() + ":" +l);
                bw.write("			Envioronment: " + w.getEnvironment().toString() +l);
                bw.write("			Player Count: " + w.getPlayers().size() +l);
                bw.write("			Entity Count: " + w.getEntities().size() +l);
                bw.write("			Loaded Chunks: " + w.getLoadedChunks().length +l);
            }
            bw.write("	=============================[ MAIN  STACKTRACE ]============================="+l);
            for (StackTraceElement element : thread1) {
                bw.write("		- " + element.toString()+l);
            }
            bw.write("	=============================[ CORE  STACKTRACE ]============================="+l);
            bw.write("	- " + ex.getCoreMessage() + " [ " + ex.getCoreMessage().getClass().getSimpleName() + " ]" +l);
            for (StackTraceElement element : core) {
                bw.write("		- " + element.toString()+l);
            }
            bw.write("	=============================[ ERROR STACKTRACE ]============================="+l);
            bw.write("	- " + ex.getMessage() + " [ " + ex.getCause().getClass().getSimpleName() + " ]" +l);
            for (StackTraceElement element : error) {
                bw.write("		- " + element.toString()+l);
            }
            bw.write("	=============================[ EXTRA STACKTRACE ]============================="+l);
            for (StackTraceElement element : thread2) {
                bw.write("		- " + element.toString()+l);
            }
            bw.write("	=============================[ END OF STACKTRACE ]============================="+l);
            bw.write(format2);
            bw.close();
            fw.close();
        } catch (IOException e) {
            log("Failed to write in the log file, {0}", e.getMessage());
        } catch (CoreException ex1) {
            ex1.printStackTrace(); // StackOverFlow
        }
        
        return format1;
    }
    
    /**
     * The available types of log
     */
    public enum LogType { 
        /**
         * logs a normal type of message to the console
         */
        INFO, 
        /**
         * logs a warning message
         */
        WARNING, 
        /**
         * logs a severe message
         */
        SEVERE, 
        /**
         * logs a debug message
         */
        DEBUG; 
    }
}
