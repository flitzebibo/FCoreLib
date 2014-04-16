package me.FurH.Core.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.number.NumberUtils;

/**
 *
 * @author FurmigaHumana
 */
public class CollectionUtils {

    /**
     * Build a String HashSet based on the given string
     * 
     * @param string the string to parse
     * @param split the area to split the string
     * @return the HashSet based on the string
     * @throws CoreException
     */
    public static HashSet<String> toStringHashSet(String string, String split) throws CoreException {

        HashSet<String> set = new HashSet<String>();

        try {
            
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");

            if (string.contains(split) && !"[]".equals(string)) {
                set.addAll(Arrays.asList(string.split(split)));
            } else
            if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(string);
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '"+string+" into a HashSet spliting at '"+split+"'");
        }

        return set;
    }
    
    /**
     * Build a String ArrayList based on the given string
     * 
     * @param string the string to parse
     * @param split the area to split the string
     * @return the ArrayList based on the string
     * @throws CoreException
     */
    public static List<String> toStringList(String string, String split) throws CoreException {

        List<String> set = new ArrayList<String>();

        try {
            
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            
            if (string.contains(split) && !"[]".equals(string)) {
                set.addAll(Arrays.asList(string.split(split)));
            } else
            if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                set.add(string);
            }
            
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '"+string+" into a ArrayList spliting at '"+split+"'");
        }

        return set;
    }

    /**
     * Build a Integer HashSet based on the given string
     * 
     * @param string the string to parse
     * @param split the area to split the string
     * @return the HashSet based on the string
     * @throws CoreException
     */
    public static HashSet<Integer> toIntegerHashSet(String string, String split) throws CoreException {
        HashSet<Integer> set = new HashSet<Integer>();

        try {
            
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");
            
            if (string.contains(split) && !"[]".equals(string)) {
                String[] splits = string.split(split);

                for (String str : splits) {
                    if (NumberUtils.isInteger(str)) {
                        set.add(NumberUtils.toInteger(str));
                    }
                }

            } else {
                if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                    set.add(Integer.parseInt(string));
                }
            }
            
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '"+string+" into a HashSet spliting at '"+split+"'");
        }
        
        return set;
    }
    
    /**
     * Build a Integer ArrayList based on the given string
     * 
     * @param string the string to parse
     * @param split the area to split the string
     * @return the ArrayList based on the string
     * @throws CoreException
     */
    public static List<Integer> toIntegerList(String string, String split) throws CoreException {
        
        List<Integer> set = new ArrayList<Integer>();

        try {
            
            string = string.replaceAll("\\[", "").replaceAll("\\]", "");

            if (string.contains(split) && !"[]".equals(string)) {
                String[] splits = string.split(split);

                for (String str : splits) {
                    if (NumberUtils.isInteger(str)) {
                        set.add(NumberUtils.toInteger(str));
                    }
                }
            } else {
                if (string != null && !"".equals(string) && !"null".equals(string) && !"[]".equals(string)) {
                    set.add(Integer.parseInt(string));
                }
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to parse string '"+string+" into a ArrayList spliting at '"+split+"'");
        }
        
        return set;
    }
    
    /**
     * Converts an Object into an Integer ArrayList, the object must be an Integer ArrayList
     * 
     * @param object the object to be converted
     * @return the ArrayList
     */
    @SuppressWarnings("unused")
	public static List<Integer> getIntegerList(Object object) {
        
        List<Integer> list = new ArrayList<Integer>();
        if (object instanceof List) {
            List<?> old = (List<?>) object;
            
            if (old == null)
            	return list;
            
            if ((object instanceof String) && (object.toString().equals("[]"))) {
                return list;
            }
            
            for (Object value : old) {
                if (value instanceof Number) {
                    list.add(((Number)value).intValue());
                }
            }
        }
        
        return list;
    }
    
    /**
     * Converts an Object into a String ArrayList, the object must be a String ArrayList
     * 
     * @param object the object to be converted
     * @return the ArrayList
     */
    @SuppressWarnings("unused")
	public static List<String> getStringList(Object object) {

        List<String> list = new ArrayList<String>();
        if (object instanceof List) {
            List<?> old = (List<?>) object;
            
            if (old == null)
                return list;
            
            if ((object instanceof String) && (object.toString().equals("[]"))) {
                return list;
            }
            
            for (Object value : old) {
                if ((value instanceof String) || isPrimitiveWrapper(value)) {
                    list.add(String.valueOf(value));
                }
            }
        }

        return list;
    }

    private static boolean isPrimitiveWrapper(Object input) {
        return input instanceof Integer || input instanceof Boolean ||
                input instanceof Character || input instanceof Byte ||
                input instanceof Short || input instanceof Double ||
                input instanceof Long || input instanceof Float;
    }
}