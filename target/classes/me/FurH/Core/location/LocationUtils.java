package me.FurH.Core.location;

import me.FurH.Core.exceptions.CoreException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author FurmigaHumana
 */
public class LocationUtils {

    /**
     * Get the position string representation in the WORLD:X:Y:Z:YAW:PITCH format for the given location
     * 
     * @param loc the location
     * @return the string representation
     * @deprecated replaced with the positionToString
     */
    @Deprecated
    public static String positionToString2(Location loc) {
        return positionToString2(loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    /**
     * Get the position string representation in the WORLD:X:Y:Z:YAW:PITCH format for the given parameters
     * 
     * @param world the world name
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @param yaw the YAW
     * @param pitch the pitch
     * @return the string representation
     * @deprecated replaced with the positionToString
     */
    @Deprecated
    public static String positionToString2(String world, double x, double y, double z, float yaw, float pitch) {
        return world + ":" + x + ":" + y + ":" + z + ":" + yaw + ":" + pitch;
    }

    /**
     * Get the position string representation in the X:Z:Y:YAW:PITCH:WORLD format for the given location
     * 
     * @param loc the location
     * @return the string representation
     */
    public static String positionToString(Location loc) {
        return positionToString(loc.getX(), loc.getZ(), loc.getY(), loc.getYaw(), loc.getPitch(), loc.getWorld().getName());
    }
    
    /**
     * Get the position string representation in the X:Z:Y:YAW:PITCH:WORLD format for the given parameters
     * 
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param y the Y coordinate
     * @param yaw the YAW
     * @param pitch the PITCH
     * @param world the world name
     * @return the string representation
     */
    public static String positionToString(double x, double z, double y, float yaw, float pitch, String world) {
        return x + ":" + z + ":" + y + ":" + yaw + ":" + pitch + ":" + world;
    }

    /**
     * Get the location string representation in the WORLD:X:Y:Z format for the given location
     * 
     * @param loc the location
     * @return the string representation
     * replaced with locationToString
     */
    public static String locationToString2(Location loc) {
        return locationToString2(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
    
    /**
     * Get the location string representation in the WORLD:X:Y:Z format for the given arguments
     *
     * @param world the world name
     * @param x the X coordinate
     * @param y the Y coordinate
     * @param z the Z coordinate
     * @return the string representation
     * replaced with locationToString
     */
    public static String locationToString2(String world, int x, int y, int z) {
        return world + ":" + x + ":" + y + ":" + z;
    }
    
    /**
     * Get the string representation in the X:Z:Y:WORLD format for the given location
     * 
     * @param loc the location
     * @return the string representation
     */
    public static String locationToString(Location loc) {
        return locationToString(loc.getBlockX(), loc.getBlockZ(), loc.getBlockY(), loc.getWorld().getName());
    }

    /**
     * Get the location string representation in the X:Z:Y:WORLD format for the given parameters
     * 
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param y the Y coordinate
     * @param world the world name
     * @return the string representation
     */
    public static String locationToString(int x, int z, int y, String world) {
        return x + ":" + z + ":" + y + ":" + world;
    }

    /**
     * Get the position represented by the string, the string must be in the WORLD:X:Y:Z:YAW:PITCH format
     * 
     * @param string the location string
     * @return the Location represented by this string
     * @throws CoreException
     * @deprecated replaced with stringToPosition
     */
    @Deprecated
    public static Location stringToPosition2(String string) throws CoreException {

        try {
            
            String[] split = string.split(":");
            World w = Bukkit.getWorld(split[0]);

            if (w != null) {
                return new Location(w, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]), Float.parseFloat(split[4]), Float.parseFloat(split[5]));
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the position string: " + string);
        }

        return null;
    }
    
    /**
     * Get the position represented by this string, the string must be in the X:Z:Y:YAW:PITCH:WORLD format
     * 
     * @param string the location string
     * @return the Location represented by this string
     * @throws CoreException
     */
    public static Location stringToPosition(String string) throws CoreException {

        try {

            String[] split = string.split(":");
            World w = Bukkit.getWorld(split[5]);

            if (w != null) {
                return new Location(w, Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[1]), Float.parseFloat(split[3]), Float.parseFloat(split[4]));
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the position string: " + string);
        }

        return null;
    }

    /**
     * Get the location represented by this string, the string must be in the X:Z:Y:WORLD format
     * 
     * @param string the location string
     * @return the Location represented by this string
     * @throws CoreException
     */
    public static Location stringToLocation(String string) throws CoreException {

        try {

            String[] split = string.split(":");
            World w = Bukkit.getWorld(split[3]);

            if (w != null) {
                return new Location(w, Double.parseDouble(split[0]), Double.parseDouble(split[2]), Double.parseDouble(split[1]));
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the location string: " + string);
        }

        return null;
    }
    
    /**
     * Get the location represented by this string, the string must be in the WORLD:X:Y:Z format
     *
     * @param string the location string
     * @return the Location represented by this string
     * @throws CoreException
     * replaced with stringToLocation
     */
    public static Location stringToLocation2(String string) throws CoreException {

        try {

            String[] split = string.split(":");
            World w = Bukkit.getWorld(split[0]);

            if (w != null) {
                return new Location(w, Double.parseDouble(split[1]), Double.parseDouble(split[2]), Double.parseDouble(split[3]));
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse the location string: " + string);
        }

        return null;
    }
}