package me.FurH.Core.inventory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.regex.Pattern;

import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.file.FileUtils;
import me.FurH.Core.internals.InternalManager;
import me.FurH.Core.number.NumberUtils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class InventoryStack {
    
    //([0-9]+|[a-zA-Z_]+):([0-9]+) - Match any MATERIAL:DATA or ID:DATA
    private static Pattern stringItem = Pattern.compile("([0-9]*|[a-zA-Z_]*):([0-9]*)");

    /**
     * Get the String representation of the given ItemStack
     * 
     * @param stack the ItemStack
     * @return the string representation of the @param stack
     * @throws CoreException
     */
    public static String getStringFromItemStack(org.bukkit.inventory.ItemStack stack) throws CoreException {
        String ret = null;

        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;

        try {

            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);

            Class<?> compoundCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTTagCompound");
            
            Object compound = compoundCLS.newInstance();
            Object craftStack = getCraftVersion(stack);

            Method save = null;

            for (Method m : craftStack.getClass().getMethods()) {
                if (m.getName().equalsIgnoreCase("save")) {
                    save = m; break;
                }
            }

            if (save == null) {
                throw new CoreException("Failed to find ItemStack 'save' method!");
            }

            Class<?> type = save.getParameterTypes()[0];
            
            if (craftStack != null) {
                save.invoke(craftStack, convert(compound, type));
            }

            Class<?> baseCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTBase");
            Method a = null;
            
            for (Method m : baseCLS.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 2) {
                    if (m.getParameterTypes()[1] == DataOutput.class) {
                        a = m; break;
                    }
                }
            }
            
            if (a == null) {
                throw new CoreException("Failed to find NBTBase required method!");
            }
            
            a.invoke(null, compound, dos);

            dos.flush();
            baos.flush();

            ret = new BigInteger(1, baos.toByteArray()).toString(32);
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the ItemStack '" + stack.toString() + "' into a string.");
        } finally {
            FileUtils.closeQuietly(baos);
            FileUtils.closeQuietly(dos);
        }

        return encode(ret);
    }

    /**
     * Get the String representation of the given ItemStack Array
     * 
     * @param source the ItemStack Array
     * @return the string representation
     * @throws CoreException
     */
    public static String getStringFromArray(org.bukkit.inventory.ItemStack[] source) throws CoreException {
        String ret = null;

        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;

        try {

            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            
            Class<?> listCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTTagList");
            Object list = listCLS.newInstance();

            Method add = null;
            
            for (Method m : listCLS.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase("add")) {
                    add = m; break;
                }
            }
            
            if (add == null) {
                throw new CoreException("Failed to find NBTTagList add method");
            }
            
            Class<?> addType = add.getParameterTypes()[0];
            
            Class<?> compoundCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTTagCompound");

            Method save = null;
            
            for (int j1 = 0; j1 < source.length; j1++) {
                
                Object craftStack = getCraftVersion(source[j1]);
                Object compound = compoundCLS.newInstance();

                if (craftStack != null) {

                    if (save == null) {
                        for (Method m : craftStack.getClass().getMethods()) {
                            if (m.getName().equalsIgnoreCase("save")) {
                                save = m; break;
                            }
                        }
                    }

                    if (save == null) {
                        throw new CoreException("Failed to find ItemStack 'save' method!");
                    }

                    Class<?> type = save.getParameterTypes()[0];
                    save.invoke(craftStack, convert(compound, type));

                }
                
                add.invoke(list, convert(compound, addType));
            }

            Class<?> baseCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTBase");
            Method a = null;
            
            for (Method m : baseCLS.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 2) {
                    if (m.getParameterTypes()[1] == DataOutput.class) {
                        a = m; break;
                    }
                }
            }
            
            if (a == null) {
                throw new CoreException("Failed to find NBTBase required method!");
            }
            
            a.invoke(null, list, dos);

            baos.flush();
            dos.flush();

            ret = new BigInteger(1, baos.toByteArray()).toString(32);
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the ItemStack Array into a string.");
        } finally {
            FileUtils.closeQuietly(baos);
            FileUtils.closeQuietly(dos);
        }

        return encode(ret);
    }

    /**
     * Get the ItemStack represented by the given String
     * 
     * @param string the ItemStack string
     * @return the ItemStack
     * @throws CoreException
     */
    @SuppressWarnings("deprecation")
	public static org.bukkit.inventory.ItemStack getItemStackFromString(String string) throws CoreException {
        org.bukkit.inventory.ItemStack ret = null;

        if ("".equals(string) || string.isEmpty()) {
            return new ItemStack(Material.AIR);
        }
        
        if (string.equals("0")) {
            return new ItemStack(Material.AIR);
        }
        
        if (NumberUtils.isInteger(string)) {
            return new ItemStack(NumberUtils.toInteger(string), 1);
        }
        
        Material material = Material.getMaterial(string);
        if (material != null) {
            return new ItemStack(Material.getMaterial(string), 1);
        }
        
        if (stringItem.matcher(string).matches()) {
            if (string.contains(":")) {

                ItemStack stack = new ItemStack(Material.AIR, 1);
                String[] split = string.split(":");

                if (NumberUtils.isInteger(split[0])) {

                    stack.setData(new MaterialData(NumberUtils.toInteger(split[0]), 
                            (byte) NumberUtils.toInteger(split[1])));

                } else {

                    stack.setData(new MaterialData(Material.getMaterial(split[0]),
                            (byte) NumberUtils.toInteger(split[1])));

                }
            }
        }

        ByteArrayInputStream bais = null;
        DataInputStream dis = null;

        try {

            bais = new ByteArrayInputStream(new BigInteger(decode(string), 32).toByteArray());
            dis = new DataInputStream(bais);

            Class<?> baseCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTBase");
            Method a = null;

            for (Method m : baseCLS.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 1 && !Modifier.isAbstract(m.getModifiers())) {
                    if (m.getParameterTypes()[0] == DataInput.class) {
                        a = m; break;
                    }
                }
            }

            if (a == null) {
                throw new CoreException("Failed to find NBTBase required method!");
            }

            Object compound = a.invoke(null, dis);
            compound = convert(compound, a.getReturnType());
            
            boolean isEmpty = ((Boolean) compound.getClass().getMethod("isEmpty").invoke(compound)).booleanValue();
            
            if (!isEmpty) {
                
                Class<?> itemStackCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"ItemStack");
                Object itemStack = itemStackCLS.getMethod("createStack", compound.getClass()).invoke(null, compound);
                                
                Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit."+InternalManager.getServerVersion()+"inventory.CraftItemStack");
                Method asCopy = craftItemStack.getMethod("asBukkitCopy", itemStack.getClass());
                
                ret = (ItemStack) asCopy.invoke(null, itemStack);

            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the String '" + string + "' into an ItemStack.");
        } finally {
            FileUtils.closeQuietly(bais);
            FileUtils.closeQuietly(dis);
        }

        return ret;
    }

    /**
     * Get the ItemStack Array represented by the given String
     * 
     * @param string the ItemStack Array string
     * @return the ItemStack Array
     * @throws CoreException
     */
    public static org.bukkit.inventory.ItemStack[] getArrayFromString(String string) throws CoreException {
        org.bukkit.inventory.ItemStack[] ret = null;

        ByteArrayInputStream bais = null;
        DataInputStream dis = null;

        if ("".equals(string) || string.isEmpty()) {
            return new ItemStack[] { new ItemStack(Material.AIR) };
        }

        try {

            bais = new ByteArrayInputStream(new BigInteger(decode(string), 32).toByteArray());
            dis = new DataInputStream(bais);
            
            Class<?> baseCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTBase");
            Method a = null;

            for (Method m : baseCLS.getDeclaredMethods()) {
                if (m.getParameterTypes().length == 1 && !Modifier.isAbstract(m.getModifiers())) {
                    if (m.getParameterTypes()[0] == DataInput.class) {
                        a = m; break;
                    }
                }
            }

            if (a == null) {
                throw new CoreException("Failed to find NBTBase required method!");
            }

            Object nbtlist = a.invoke(null, dis);
            nbtlist = convert(nbtlist, a.getReturnType());
                        
            int size = ((Integer) nbtlist.getClass().getMethod("size").invoke(nbtlist)).intValue();
            ret = new org.bukkit.inventory.ItemStack[ size ];
            
            Class<?> compoundCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"NBTTagCompound");
            Method get = nbtlist.getClass().getMethod("get", Integer.TYPE);
            
            Class<?> itemStackCLS = Class.forName("net.minecraft.server."+InternalManager.getServerVersion()+"ItemStack");
            Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit."+InternalManager.getServerVersion()+"inventory.CraftItemStack");
            
            for (int i = 0; i < size; i++) {
                
                Object compound = get.invoke(nbtlist, i);
                compound = convert(compound, compoundCLS);
                
                boolean isEmpty = ((Boolean) compound.getClass().getMethod("isEmpty").invoke(compound)).booleanValue();

                if (!isEmpty) {

                    Object itemStack = itemStackCLS.getMethod("createStack", compound.getClass()).invoke(null, compound);
                    Method asCopy = craftItemStack.getMethod("asBukkitCopy", itemStack.getClass());

                    ret[ i ] = (ItemStack) asCopy.invoke(null, itemStack);

                }
            }
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to convert the String '" + string + "' into an ItemStack Array.");
        } finally {
            FileUtils.closeQuietly(bais);
            FileUtils.closeQuietly(dis);
        }

        return ret;
    }

    /**
     * Return the nms ItemStack object version of the org.bukkit.inventory.ItemStack using reflection
     *
     * @param stack the bukkit itemstack
     * @return the nms itemstack object
     */
    public static Object getCraftVersion(org.bukkit.inventory.ItemStack stack) {

        if (stack != null) {
            try {
                Class<?> cls = Class.forName("org.bukkit.craftbukkit."+InternalManager.getServerVersion()+"inventory.CraftItemStack");
                
                Method method = cls.getMethod("asNMSCopy", org.bukkit.inventory.ItemStack.class);
                method.setAccessible(true);
                
                return method.invoke(null, stack);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Simple java reflection that is in the wrong place, it works like the '(String) object' conversion.
     *
     * @param obj the object to be converted
     * @param type the class type to convert the object
     * @return the converted object
     */
    public static Object convert(Object obj, Class<?> type) {
        return type.cast(obj);
    }
    
    /**
     * Encode a String into Base64
     * 
     * @param string the String to encode
     * @return the encoded String, or "0" if the string is null.
     */
    public static String encode(String string) {
        if (string == null) {
            return "MA==";
        }
        return Base64Coder.encodeString(string);
    }

    /**
     * Decode a Base64 String
     * 
     * @param string the String to decode
     * @return the decoded String, or an empty String if the @param string is null
     */
    public static String decode(String string) {
        if (string == null) {
            return "";
        }
        return Base64Coder.decodeString(string);
    }
}