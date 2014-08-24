package me.FurH.Core.inventory;


import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import me.FurH.Core.exceptions.CoreException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;


/**
 * 
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class InventoryStack {

	// ([0-9]+|[a-zA-Z_]+):([0-9]+) - Match any MATERIAL:DATA or ID:DATA
	private static Pattern stringItem = Pattern
			.compile("([0-9]*|[a-zA-Z_]*):([0-9]*)");

	/**
	 * Get the String representation of the given ItemStack
	 * 
	 * @param stack
	 *            the ItemStack
	 * @return the string representation of the @param stack
	 * @throws CoreException
	 */
	public static String getStringFromArray(ItemStack[] items) throws CoreException{
		StringBuilder sbItems = new StringBuilder();
		StringBuilder sbAmount = new StringBuilder();
		StringBuilder sbDurability = new StringBuilder();
		StringBuilder sbEnchants = new StringBuilder();
		for (ItemStack item : items) {
			int itemId = 0;
			int amount = 0;
			short durability = 0;
			Map<Enchantment, Integer> enchants = null;
			if (item != null) {
				itemId = item.getTypeId();
				amount = item.getAmount();
				durability = item.getDurability();
				enchants = item.getEnchantments();
				if (!enchants.keySet().isEmpty()) {
					for (Enchantment enchant : enchants.keySet()) {
						int id = enchant.getId();
						int level = enchants.get(enchant);
						sbEnchants.append(id + ":" + level + "-");
					}
					sbEnchants.deleteCharAt(sbEnchants.lastIndexOf("-"));
				}
			}
			sbItems.append(itemId).append(",");
			sbAmount.append(amount).append(",");
			sbDurability.append(durability).append(",");
			sbEnchants.append(",");
		}
		sbItems.deleteCharAt(sbItems.lastIndexOf(","));
		sbAmount.deleteCharAt(sbAmount.lastIndexOf(","));
		sbDurability.deleteCharAt(sbDurability.lastIndexOf(","));
		sbEnchants.deleteCharAt(sbEnchants.lastIndexOf(","));
		return sbItems.append(";").append(sbAmount).append(";")
				.append(sbDurability).append(";").append(sbEnchants).toString();

	}

	/**
	 * Get the ItemStack Array represented by the given String
	 * 
	 * @param string
	 *            the ItemStack Array string
	 * @return the ItemStack Array
	 * @throws CoreException
	 */
	public static ItemStack[] getArrayFromString(String str) throws CoreException{
		ItemStack[] items = null;
		String[] itemSplit = str.split(";");
		String[] itemid = itemSplit[0].split(",");
		String[] amount = itemSplit[1].split(",");
		String[] durability = itemSplit[2].split(",");
		String[] enchants = itemSplit[3].split(",", -1);
		items = new ItemStack[itemid.length];
		for (int i = 0; i < items.length; i++) {
			items[i] = new ItemStack(Integer.parseInt(itemid[i]),
					Integer.parseInt(amount[i]),
					Short.parseShort(durability[i]));
			if (!enchants[i].isEmpty()) {
				String[] itemEnchants = enchants[i].split("-");
				for (String enchant : itemEnchants) {
					String[] enchantSplit = enchant.split(":");
					int id = Integer.parseInt(enchantSplit[0]);
					int level = Integer.parseInt(enchantSplit[1]);
					Enchantment e = new EnchantmentWrapper(id);
					items[i].addUnsafeEnchantment(e, level);
				}
			}
		}
		return items;
	}

	/**
	 * Get the String representation of the given ItemStack
	 * 
	 * @param stack
	 *            the ItemStack
	 * @return the string representation of the @param stack
	 * @throws CoreException
	 */
	public static String getStringFromItemStack(ItemStack stack)
			throws CoreException {

		return mapToString(stack.serialize());
	}

	/**
	 * Get the ItemStack represented by the given String
	 * 
	 * @param string
	 *            the ItemStack string
	 * @return the ItemStack
	 * @throws CoreException
	 */
	public static org.bukkit.inventory.ItemStack getItemStackFromString (
			String string) throws CoreException{
		//Bukkit.getConsoleSender().sendMessage("DEBUG: getItemStackFromString");
		//Bukkit.getConsoleSender().sendMessage(string);
		if ("".equals(string) || string.isEmpty()) {
			return new ItemStack(Material.AIR);
		}

		if (string.equals("0")) {
			return new ItemStack(Material.AIR);
		}
		return ItemStack.deserialize(stringToMap(string));
	}

	private static String mapToString(Map<String, Object> map) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String key : map.keySet()) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append("&");
			}
			String value = map.get(key).toString();
			try {
				stringBuilder.append((key != null ? URLEncoder.encode(key,
						"UTF-8") : ""));
				stringBuilder.append("=");
				stringBuilder.append(value != null ? URLEncoder.encode(value,
						"UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(
						"This method requires UTF-8 encoding support", e);
			}
		}
		return stringBuilder.toString();
	}

	private static Map<String, Object> stringToMap(String input) {
		Map<String, Object> map = new HashMap<String, Object>();
		String[] nameValuePairs = input.split("&");
		for (String nameValuePair : nameValuePairs) {
			String[] nameValue = nameValuePair.split("=");
			try {
				map.put(URLDecoder.decode(nameValue[0], "UTF-8"),
						nameValue.length > 1 ? URLDecoder.decode(nameValue[1],
								"UTF-8") : "");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(
						"This method requires UTF-8 encoding support", e);
			}
		}

		return map;
	}

	/**
	 * Return the nms ItemStack object version of the
	 * org.bukkit.inventory.ItemStack using reflection
	 * 
	 * @param stack
	 *            the bukkit itemstack
	 * @return the nms itemstack object
	 */
	public static Object getCraftVersion(org.bukkit.inventory.ItemStack stack) {

		if (stack != null) {
			try {
				Class<?> cls = Class.forName("org.bukkit.craftbukkit."
						+ getServerVersion() + "inventory.CraftItemStack");

				Method method = cls.getMethod("asNMSCopy",
						org.bukkit.inventory.ItemStack.class);
				method.setAccessible(true);

				return method.invoke(null, stack);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return null;
	}

	/**
	 * Simple java reflection that is in the wrong place, it works like the
	 * '(String) object' conversion.
	 * 
	 * @param obj
	 *            the object to be converted
	 * @param type
	 *            the class type to convert the object
	 * @return the converted object
	 */
	// public static Object convert(Object obj, Class<?> type) {
	// return type.cast(obj);
	// }

	/**
	 * Encode a String into Base64
	 * 
	 * @param string
	 *            the String to encode
	 * @return the encoded String, or "0" if the string is null.
	 */
	public static String encode(String string) {
		Bukkit.getConsoleSender().sendMessage("InventoryStack encode");
		Bukkit.getConsoleSender().sendMessage(string);
		if (string == null) {
			return Base64Coder.decodeString("");
		}
		return Base64Coder.encodeString(string);
	}

	/**
	 * Decode a Base64 String
	 * 
	 * @param string
	 *            the String to decode
	 * @return the decoded String, or an empty String if the @param string is
	 *         null
	 */
	public static String decode(String string) {
		Bukkit.getConsoleSender().sendMessage("InventoryStack decode");
		Bukkit.getConsoleSender().sendMessage(string);

		if (string == null) {
			return "";
		}
		return Base64Coder.decodeString(string);
	}

	private static String getServerVersion() {
		String packageName = Bukkit.getServer().getClass().getPackage()
				.getName();
		String apiVersion = packageName
				.substring(packageName.lastIndexOf('.') + 1);
		return apiVersion + ".";
	}
}