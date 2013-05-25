package me.FurH.Core.internals;

import java.io.File;
import me.FurH.Core.cache.CoreSafeCache;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.file.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class InternalManager extends ClassLoader {
    
    private static CoreSafeCache<String, IEntityPlayer> entities = new CoreSafeCache<String, IEntityPlayer>();
    private static String tocls = "me.FurH.Core.internals.CEntityPlayer";
    
    private static InternalManager classLoader;
    private static String version = null;

    /**
     * Get the IEntityPlayer Object for the given Player
     *
     * @param player the Bukkit Player
     * @return the IEntityPlayer object
     * @throws CoreException  
     */
    public static IEntityPlayer getEntityPlayer(Player player) throws CoreException {
        
        if (version == null) {
            setupClasses();
        }
        
        if (entities.containsKey(player.getName())) {
            return entities.get(player.getName());
        }

        IEntityPlayer entity = ((IEntityPlayer) createObject(IEntityPlayer.class, tocls)).setEntityPlayer(player);
        entities.put(player.getName(), entity);

        return entity;
    }
    
    public static void removeEntityPlayer(Player player) {
        entities.remove(player.getName());
    }

    private static Object createObject(Class<? extends Object> assing, String path) throws CoreException {

        try {

            Class<?> cls = Class.forName(path);

            if (assing.isAssignableFrom(cls)) {
                return cls.getConstructor().newInstance();
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to create class '" + path + "' object instance!");
        }

        return null;
    }

    private static void setupClasses() throws CoreException {
        
        File classes = new File("plugins" + File.separator + "FCoreLib" + File.separator + "classes");
        if (!classes.exists()) {
            classes.mkdirs();
        }
        
        if (version == null) {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            version = pkg.substring(pkg.lastIndexOf('.') + 1);
        }

        File entityClass = new File(classes, "CEntityPlayer_"+version+".class");

        if (entityClass.exists()) {
            tocls = "me.FurH.Core.internals.CEntityPlayer_"+version;
            loadClass(entityClass);
        }
    }
    
    private static Class<?> loadClass(File file) throws CoreException {
        try {
            
            byte[] data = FileUtils.getBytesFromFile(file);
            
            if (classLoader == null) {
                classLoader = new InternalManager();
            }

            return classLoader.defineClass(null, data, 0, data.length);
            
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to load '" + file.getName() + "' as an class!");
        }
    }
}