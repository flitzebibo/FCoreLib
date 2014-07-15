 package me.FurH.Core.configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.List;

import me.FurH.Core.CorePlugin;
import me.FurH.Core.cache.CoreSafeCache;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.file.FileUtils;
import me.FurH.Core.list.CollectionUtils;
import me.FurH.Core.util.Communicator;

import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author FurmigaHumana
 */
public class Configuration {

    private CoreSafeCache<String, YamlConfiguration> yamlcache = new CoreSafeCache<String, YamlConfiguration>();

    private String default_setting = "settings.yml";
    private String default_world = "world.yml";
    private String default_message = "messages.yml";

    private HashSet<String> update_required = new HashSet<String>();
    private boolean single_config = false;

    private YamlConfiguration settings;
    private YamlConfiguration messages;

    /**
     * The CorePlugin object used to handle this Configuration
     */
    protected CorePlugin plugin;
    private ConfigUpdater updater;

    /**
     * Creates a new Configuration object
     * 
     * @param plugin the CorePlugin used to handle this Configuration
     */
    public Configuration(CorePlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Set the single configuration mode
     * 
     * @param single_config1 the single configuration mod
     */
    public void setSingleConfig(boolean single_config1) {
        single_config = single_config1;
    }

    /**
     * Update all the files that need to be updated
     */
    public void updateConfig() {

        for (String dir : update_required) {
            File file = new File(dir);
            updateLines(file, getInputStream(file));
        }

        update_required.clear(); unload();
    }
    
    /**
     * Unload all loaded configurations
     */
    public void unload() {
        yamlcache.clear();
        
        settings = null;
        messages = null;
    }
    
    private YamlConfiguration config(File file) {
        Communicator com    = plugin.getCommunicator();
        YamlConfiguration yaml = null;

        try {
            if (file.getName().equals(default_setting)) {

                if (settings == null) {
                    settings = new YamlConfiguration();
                    settings.load(file);
                }

                return settings;
            }

            if (file.getName().equals(default_message)) {

                if (messages == null) {
                    messages = new YamlConfiguration();
                    messages.load(file);
                }

                return messages;
            }

            if (yamlcache.containsKey(file.getName())) {
                return yamlcache.get(file.getName());
            }

            yaml = new YamlConfiguration();
            yaml.load(file);
        } catch (FileNotFoundException ex) {
            com.error(ex, "The file " + file.getName() + "  was not found in the plugin directory!");
        } catch (IOException ex) {
            com.error(ex, "Failed to load the " + file.getName() + " configuration file!");
        } catch (InvalidConfigurationException ex) {
            com.log("[TAG] You have a broken node in your " + file.getName() + " file, use http://yaml-online-parser.appspot.com/ to find errors! " + ex.getMessage());
            update_required.add(file.getAbsolutePath());
        }

        if (yaml != null) {
            yamlcache.put(file.getName(), yaml);
        }

        return yaml;
    }

    /**
     * Return an boolean based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the boolean value
     */
    protected boolean getBoolean(World w, String node) {
        Object object = get(getWorldFile(w), node);
        return (object instanceof Boolean) ? (Boolean) object : false;
    }
    
    /**
     * Return an boolean based on the given path
     *
     * @param node the configuration node
     * @return the boolean value
     */
    protected boolean getBoolean(String node) {
        Object object = get(getSettingsFile(), node);
        return (object instanceof Boolean) ? (Boolean) object : false;
    }
    
    /**
     * Return an integer based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the integer value
     */
    protected int getInteger(World w, String node) {
        Object object = get(getWorldFile(w), node);
        return (object instanceof Number) ? ((Number) object).intValue() : 0;
    }

    /**
     * Return an integer based on the given path
     *
     * @param node the configuration node
     * @return the integer value
     */
    protected int getInteger(String node) {
        Object object = get(getSettingsFile(), node);
        return (object instanceof Number) ? ((Number) object).intValue() : 0;
    }
    
    /**
     * Return a double based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the double value
     */
    protected double getDouble(World w, String node) {
        Object object = get(getWorldFile(w), node);
        return (object instanceof Number) ? ((Number) object).doubleValue() : 0.0;
    }

    /**
     * Return a double based on the given path
     *
     * @param node the configuration node
     * @return the double value
     */
    protected double getDouble(String node) {
        Object object = get(getSettingsFile(), node);
        return (object instanceof Number) ? ((Number) object).doubleValue() : 0.0;
    }

    /**
     * Return a string based on the given path
     *
     * @param node the configuration node
     * @return the string value
     */
    public String getString(String node) {
        Object object = get(getSettingsFile(), node);
        try {
            return new String(object.toString().getBytes(), "UTF8");
        } catch (UnsupportedEncodingException ex) {
            return object.toString();
        }
    }

    /**
     * Return a string based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the string value
     */
    public String getString(World w, String node) {
        Object object = get(getWorldFile(w), node);
        try {
            return new String(object.toString().getBytes(), "UTF8");
        } catch (UnsupportedEncodingException ex) {
            return object.toString();
        }
    }

    /**
     * Return a long based on the given path
     *
     * @param node the configuration node
     * @return the long value
     */
    protected long getLong(String node) {
        Object object = get(getSettingsFile(), node);
        return (object instanceof Number) ? ((Number) object).longValue() : 0;
    }

    /**
     * Return a long based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the long value
     */
    protected long getLong(World w, String node) {
        Object object = get(getWorldFile(w), node);
        return (object instanceof Number) ? ((Number) object).longValue() : 0;
    }

    /**
     * Return a String converted to String HashSet based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the HashSet
     */
    protected HashSet<String> getStringAsStringSet(World w, String node) {
        try {
            return CollectionUtils.toStringHashSet(getString(w, node).replaceAll(" ", ""), ",");
        } catch (CoreException ex) {
            plugin.getCommunicator().error(ex);
        }
        return null;
    }

    /**
     * Return a String converted to String HashSet based on the given path
     *
     * @param node the configuration node
     * @return the HashSet
     */
    protected HashSet<String> getStringAsStringSet(String node) {
        try {
            return CollectionUtils.toStringHashSet(getString(node).replaceAll(" ", ""), ",");
        } catch (CoreException ex) {
            plugin.getCommunicator().error(ex);
        }
        return null;
    }

    /**
     * Return a String converted to Integer HashSet based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the HashSet
     */
    protected HashSet<Integer> getStringAsIntegerSet(World w, String node) {
        try {
            return CollectionUtils.toIntegerHashSet(getString(w, node).replaceAll(" ", ""), ",");
        } catch (CoreException ex) {
            plugin.getCommunicator().error(ex);
        }
        return null;
    }
    
    /**
     * Return a String converted to Integer HashSet based on the given path
     *
     * @param node the configuration node
     * @return the HashSet
     */
    protected HashSet<Integer> getStringAsIntegerSet(String node) {
        try {
            return CollectionUtils.toIntegerHashSet(getString(node).replaceAll(" ", ""), ",");
        } catch (CoreException ex) {
            plugin.getCommunicator().error(ex);
        }
        return null;
    }
    
    /**
     * Return a String converted to Integer ArrayList based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the ArrayList
     */
    protected List<Integer> getStringAsIntegerList(World w, String node) {
        try {
            return CollectionUtils.toIntegerList(getString(w, node).replaceAll(" ", ""), ",");
        } catch (CoreException ex) {
            plugin.getCommunicator().error(ex);
        }
        return null;
    }
    
    /**
     * Return a String converted to Integer ArrayList based on the given path
     *
     * @param node the configuration node
     * @return the ArrayList
     */
    protected List<Integer> getStringAsIntegerList(String node) {
        try {
            return CollectionUtils.toIntegerList(getString(node).replaceAll(" ", ""), ",");
        } catch (CoreException ex) {
            plugin.getCommunicator().error(ex);
        }
        return null;
    }

    /**
     * Get the message string from the messages file
     *
     * @param node the configuration node
     * @return the message string
     */
    public String getMessage(String node) {
        Object object = get(getMessagesFile(), node);

        try {
            return new String(object.toString().getBytes(), "UTF8");
        } catch (UnsupportedEncodingException ex) {
            return object.toString();
        }
    }
    
    /**
     * Return a String ArrayList based on the given path
     *
     * @param node the configuration node
     * @return the ArrayList
     */
    public List<String> getStringList(String node) {
        return CollectionUtils.getStringList(get(getSettingsFile(), node));
    }

    /**
     * Return a String ArrayList based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the ArrayList
     */
    public List<String> getStringList(World w, String node) {
        return CollectionUtils.getStringList(get(getWorldFile(w), node));
    }
    
    /**
     * Return a Integer ArrayList based on the given path
     *
     * @param node the configuration node
     * @return the ArrayList
     */
    public List<Integer> getIntegerList(String node) {
        return CollectionUtils.getIntegerList(get(getSettingsFile(), node));
    }
    
    /**
     * Return a Integer ArrayList based on the given path
     *
     * @param w the World
     * @param node the configuration node
     * @return the ArrayList
     */
    public List<Integer> getIntegerList(World w, String node) {
        return CollectionUtils.getIntegerList(get(getWorldFile(w), node));
    }

    /**
     * Get if the configuration file have a node
     *
     * @param w the World
     * @param node the configuration node
     * @return true if the configuration file have the node, false otherwise.
     */
    public boolean hasNode(World w, String node) {
        return hasNode(getWorldFile(w), node);
    }
    
    /**
     * Get if the configuration file have a node
     *
     * @param node the configuration node
     * @return true if the configuration file have the node, false otherwise.
     */
    public boolean hasNode(String node) {
        return hasNode(getSettingsFile(), node);
    }
    
    /**
     * Get if the configuration file have a node
     *
     * @param file the file to check
     * @param node the configuration node
     * @return true if the configuration file have the node, false otherwise.
     */
    public boolean hasNode(File file, String node) {
        return config(file).contains(node);
    }

    /**
     * Defines the value of some node in some file
     *
     * @param file the file to be changed
     * @param node the node to be set
     * @param value the value of the node
     */
    public void set(File file, String node, Object value) {
        Communicator com    = plugin.getCommunicator();

        YamlConfiguration config = config(file);
        config.set(node, value);
        
        try {
            config.save(file);
        } catch (IOException ex) {
            com.error(ex, "Failed to update the '" + node + ":" + value + "' " + file.getName() + " node.");
        }

        update_required.add(file.getAbsolutePath());
    }
    
    /**
     * Get an Object from the configuration file based on the given path
     *
     * @param file the file to get the node
     * @param node the node to get the object
     * @return the Object, or null if not present.
     */
    @SuppressWarnings("deprecation")
	public Object get(File file, String node) {
        Communicator com    = plugin.getCommunicator();
        Object backup = null;

        try {
            if (!config(file).contains(node)) {

                YamlConfiguration rsconfig = new YamlConfiguration();
                rsconfig.load(getInputStream(file));

                if (rsconfig.contains(node)) {
                    com.log("[TAG] Settings file updated, check at: \n" + node.replace(".", " &3>>&f "));
                    backup = rsconfig.get(node);
                    update_required.add(file.getAbsolutePath());
                } else {
                    com.log("[TAG] Invalid node at: "+node+", contact the developer!");
                }
            }
        } catch (IOException ex) {
            com.error(ex, "Can't load the "+file.getName()+" file: "+ex.getMessage()+", node " + node);
        } catch (InvalidConfigurationException ex) {
            com.log("[TAG] You have a broken node in your " + file.getName() + " file, use http://yaml-online-parser.appspot.com/ to find errors! " + ex.getMessage());
            update_required.add(file.getAbsolutePath());
        }

        Object value = config(file).get(node);
        if (value == null) {

            if (backup != null) {
                value = backup;
            } else {
                value = "0";
                com.log("[TAG] Can't get "+file.getName()+" node: "+node+", contact the developer.");
                update_required.add(file.getAbsolutePath());
            }
        }

        return value;
    }

    private InputStream getInputStream(File file) {

        String source = default_setting;

        if (file.getName().equals(default_setting)) {
            source = default_setting;
        } else
        if (file.getName().equals(default_message)) {
            source = default_message;
        } else {
            source = default_world;
        }

        return plugin.getResource(source);
    }
    
    /**
     * Get the file used for some world
     *
     * @param w the World
     * @return the file for the world
     */
    public File getWorldFile(World w) {
        File file = new File(plugin.getDataFolder() + File.separator + "worlds", (w != null || single_config) ? w.getName() + ".yml" : default_world);

        if (!file.exists()) {
            
            try {
                FileUtils.copyFile(plugin.getResource(default_world), file);
            } catch (CoreException ex) {
                plugin.getCommunicator().error(ex);
            }
            
            updateLines(file, getInputStream(file));
        }

        return file;
    }
    
    /**
     * Get the file used to store the messages
     * 
     * @return the messages file
     */
    public File getMessagesFile() {
        File file = new File(plugin.getDataFolder(), default_message);

        if (!file.exists()) { 
            
            try { 
                FileUtils.copyFile(plugin.getResource(default_message), file);
            } catch (CoreException ex) {
                plugin.getCommunicator().error(ex);
            }
            
            updateLines(file, getInputStream(file));
        }

        return file;
    }
    
    /**
     * Get the main settings file
     *
     * @return the settings file
     */
    public File getSettingsFile() {
        File file = new File(plugin.getDataFolder(), default_setting);

        if (!file.exists()) { 

            try { 
                FileUtils.copyFile(plugin.getResource(default_setting), file);
            } catch (CoreException ex) {
                plugin.getCommunicator().error(ex);
            }

            updateLines(file, getInputStream(file));
        }

        return file;
    }
    
    private void updateLines(File file, InputStream stream) {
        
        if (updater == null) {
            updater = new ConfigUpdater();
        }
        
        updater.updateLines(file, stream);
    }
}
