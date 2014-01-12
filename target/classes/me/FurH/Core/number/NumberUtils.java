package me.FurH.Core.number;

import me.FurH.Core.exceptions.CoreException;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class NumberUtils {

    /**
     * Get if the string is a valid integer
     * 
     * @param string the string
     * @return true if is a valid integer, false otherwise.
     */
    public static boolean isInteger(String string) {
        try { Integer.parseInt(string); } catch (Exception ex) { return false; } return true;
    }
    
    /**
     * Get if the string is a valid double
     * 
     * @param string the string
     * @return true if is a valid double, false otherwise.
     */
    public static boolean isDouble(String string) {
        try { Double.parseDouble(string); } catch (Exception ex) { return false; } return true;
    }
    
    /**
     * Get if the string is a valid long
     * 
     * @param string the string
     * @return true if is a valid long, false otherwise.
     */
    public static boolean isLong(String string) {
        try { Long.parseLong(string); } catch (Exception ex) { return false; } return true;
    }
    
    /**
     * Get if the string is a valid byte
     * 
     * @param string the string
     * @return true if is a valid byte, false otherwise.
     */
    public static boolean isByte(String string) {
        try { Byte.parseByte(string); } catch (Exception ex) { return false; } return true;
    }
    
    /**
     * Get if the string is a valid short
     * 
     * @param string the string
     * @return true if is a valid short, false otherwise.
     */
    public static boolean isShort(String string) {
        try { Short.parseShort(string); } catch (Exception ex) { return false; } return true;
    }

    /**
     * Get if the string is a valid float
     * 
     * @param string the string
     * @return true if is a valid float, false otherwise.
     */
    public static boolean isFloat(String string) {
        try { Float.parseFloat(string); } catch (Exception ex) { return false; } return true;
    }

    /**
     * Get the integer value of the given string
     * 
     * @param str the string
     * @return the integer value
     * @throws CoreException if it is not a valid integer
     */
    public static int toInteger(String str) throws CoreException {
        int ret = 0;
        
        try {
            ret = Integer.parseInt(str.replaceAll("[^0-9-]", ""));
        } catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid integer!");
        }

        return ret;
    }
    
    /**
     * Get the double value of the given string
     * 
     * @param str the string
     * @return the double value
     * @throws CoreException if it is not a valid double
     */
    public static double toDouble(String str) throws CoreException {
        double ret = 0.0;

        try {
            ret = Double.parseDouble(str.replaceAll("[^0-9-.]", ""));
        } catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid double!");
        }

        return ret;
    }
    
    /**
     * Get the long value of the given string
     * 
     * @param str the string
     * @return the long value
     * @throws CoreException if it is not a valid long
     */
    public static long toLong(String str) throws CoreException {
        long ret = 0L;
        
        try {
            ret = Long.parseLong(str.replaceAll("[^0-9-]", ""));
        } catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid long!");
        }

        return ret;
    }
    
    /**
     * Get the byte value of the given string
     * 
     * @param str the string
     * @return the byte value
     * @throws CoreException if it is not a valid byte
     */
    public static byte toByte(String str) throws CoreException {
        byte ret = 0;
        
        try {
            ret = Byte.parseByte(str.replaceAll("[^0-9-]", ""));
        } catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid byte!");
        }

        return ret;
    }
    
    /**
     * Get the short value of the given string
     * 
     * @param str the string
     * @return the short value
     * @throws CoreException if it is not a valid short
     */
    public static short toShort(String str) throws CoreException {
        short ret = 0;
        
        try {
            ret = Short.parseShort(str.replaceAll("[^0-9-]", ""));
        } catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid short!");
        }

        return ret;
    }
    
    /**
     * Get the float value of the given string
     * 
     * @param str the string
     * @return the float value
     * @throws CoreException if it is not a valid float
     */
    public static float toFloat(String str) throws CoreException {
        float ret = 0F;
        
        try {
            ret = Float.parseFloat(str.replaceAll("[^0-9-]", ""));
        } catch (Exception ex) {
            throw new CoreException(ex, str + " is not a valid float!");
        }

        return ret;
    }
}
