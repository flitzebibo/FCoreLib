package me.FurH.Core.inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import me.FurH.Core.number.NumberUtils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

/**
 *
 * @author FurmigaHumana
 * @deprecated This class has been replaced by InventoryStack
 */
@Deprecated
public class InvUtils {

    public static ItemStack[] toArrayStack(List<String> stacks) {
        return toArrayStack(stacks.toArray(new String[] { }));
    }
    
    public static ItemStack[] toArrayStack(String item) {
        return toArrayStack(item.substring(1, item.length() - 1).split(", "));
    }
    
    public static ItemStack[] toArrayStack(String[] stacks) {
        
        ItemStack[] items = new ItemStack[ stacks.length ];
        for (int i = 0; i < stacks.length; i++) {
            items[i] = stringToItemStack(stacks[i]);
        }

        return items;
    }
    
    private static Pattern itemStack1 = Pattern.compile("[0-9]+:[0-9]+");
    private static Pattern itemStack2 = Pattern.compile("[a-zA-Z_]+:[0-9]+");
    
    public static ItemStack stringToItemStack(String string) {
        ItemStack stack = new ItemStack(Material.AIR);
        
        try {
            
            if (string.equals("0")) {
                return null;
            }
            
            if (NumberUtils.isInteger(string)) {
                return new ItemStack(NumberUtils.toInteger(string), 1);
            }
            
            if (Material.getMaterial(string) != null) {
                return new ItemStack(Material.getMaterial(string), 1);
            }
            
            if (itemStack1.matcher(string).matches()) {
                return new ItemStack(NumberUtils.toInteger(string.split(":")[0]), 1, (short) 0, Byte.parseByte(string.split(":")[1]));
            }
            
            if (itemStack2.matcher(string).matches()) {
                return new ItemStack(Material.getMaterial(string.split(":")[0]), 1, (short) 0, Byte.parseByte(string.split(":")[1]));
            }

            if (string.equals("[]")) {
                return stack;
            }

            if (!string.contains(":")) {
                return stack;
            }

            String[] inv = string.split(":");
            if (inv.length < 4) {
                return stack;
            }

            String id = inv[0];
            String data = inv[1];
            String amount = inv[2];
            String durability = inv[3];
            String enchantments = inv[4];

            boolean meta = false;
            try {
                String fire = inv[5];
                meta = true;
            } catch (Exception ex) {
                meta = false;
            }

            if (!string.equals("0:0:0:1:[]")) {

                try {
                    stack = new ItemStack(Integer.parseInt(id));
                    stack.setAmount(Integer.parseInt(amount));
                    stack.setDurability(Short.parseShort(durability));

                    int i = Integer.parseInt(data);
                    if (i > 128) {
                        i = 128;
                    }

                    stack.getData().setData((byte)i);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                Map<Enchantment, Integer> enchants = getEnchantments(enchantments);

                if (stack.getType() == Material.ENCHANTED_BOOK) {
                    EnchantmentStorageMeta enchant = (EnchantmentStorageMeta) stack.getItemMeta();

                    for (Enchantment e : enchants.keySet()) {
                        enchant.addStoredEnchant(e, enchants.get(e), true);
                    }

                    stack.setItemMeta(enchant);
                } else {
                    stack.addUnsafeEnchantments(enchants);
                }

                if (meta) {
                    stack = setItemMeta(stack, inv[5]);

                    if (stack.getType() == Material.FIREWORK) {
                        stack = getFireWork(stack, string);
                    }

                    if (stack.getType() == Material.BOOK || stack.getType() == Material.BOOK_AND_QUILL || stack.getType() == Material.WRITTEN_BOOK) {
                        stack = setBookMeta(stack, inv[5]);
                    }

                    if (stack.getType() == Material.LEATHER_HELMET || stack.getType() == Material.LEATHER_CHESTPLATE || stack.getType() == Material.LEATHER_LEGGINGS || stack.getType() == Material.LEATHER_BOOTS) {
                        stack = setArmorMeta(stack, inv[5]);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return stack;
    }

    private static Map<Enchantment, Integer> getEnchantments(String enchantments) {
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();

        if (!enchantments.equals("[]")) {
            enchantments = enchantments.replaceAll("[^a-zA-Z0-9_:,=]", "");
            String[] enchant = enchantments.split(",");

            List<String> encht = new ArrayList<String>();
            encht.addAll(Arrays.asList(enchant));

            for (String exlvl : encht) {
                if (exlvl.contains("=")) {
                    String[] split = exlvl.split("=");
                    String name = split[0];
                    String lvl = split[1];
                    try {
                        Enchantment ext = Enchantment.getByName(name);
                        enchants.put(ext, Integer.parseInt(lvl));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        
        return enchants;
    }
    
    public static String toListString(ItemStack[] source) {
        return toStackStringList(source).toString();
    }
    
    public static List<String> toStackStringList(ItemStack[] source) {
        List<String> items = new ArrayList<String>();

        for (ItemStack item : source) {
            items.add(itemStackToString(item));
        }

        return items;
    }
    
    public static String itemStackToString(ItemStack item) {
        String ret = "0";

        try {

            if (item == null) { 
                return "0"; 
            }

            if (item.getType() == Material.AIR) {
                return "0";
            }

            int type = item.getTypeId();
            int amount = item.getAmount();
            byte data = item.getData().getData();
            short durability = item.getDurability();

            Map<Enchantment, Integer> e1 = item.getEnchantments();
            
            if (item.getType() == Material.ENCHANTED_BOOK) {
                EnchantmentStorageMeta enchant = (EnchantmentStorageMeta) item.getItemMeta();
                e1 = enchant.getStoredEnchants();
            }

            List<String> enchantments = new ArrayList<String>();
            for (Enchantment key : e1.keySet()) {
                enchantments.add(key.getName() + "=" + e1.get(key));
            }

            ret = ("'"+type+":"+data+":"+amount+":"+durability+":"+enchantments+"'").replaceAll("[^a-zA-Z0-9:,_=\\[\\]]", "");

            if (item.hasItemMeta() && item.getType() == Material.FIREWORK) {
                ret += getFireWork(item);
            }

            if (item.hasItemMeta() && item.getType() == Material.BOOK || item.getType() == Material.BOOK_AND_QUILL || item.getType() == Material.WRITTEN_BOOK) {
                ret += getBookMeta(item);
            }

            if (item.hasItemMeta() && item.getType() == Material.LEATHER_HELMET || item.getType() == Material.LEATHER_CHESTPLATE || item.getType() == Material.LEATHER_LEGGINGS || item.getType() == Material.LEATHER_BOOTS) {
                ret += getArmorMeta(item);
            }

            if (item.hasItemMeta()) {
                ret += getItemMeta(item);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return ret;
    }

    public static String getArmorMeta(ItemStack stack) {
        return ":"+getColor(((LeatherArmorMeta) stack.getItemMeta()).getColor());
    }

    public static ItemStack setArmorMeta(ItemStack stack, String string) {
        LeatherArmorMeta meta = (LeatherArmorMeta) stack.getItemMeta();

        try {
            meta.setColor(getColor(Integer.parseInt(string)));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stack.setItemMeta(meta);
        return stack;
    }
    
    public static String getBookMeta(ItemStack stack) {
        BookMeta meta = (BookMeta) stack.getItemMeta();
        
        int hasTitle = meta.hasTitle() ? 1 : 0;
        String title = encode(meta.getTitle());
        
        int hasAuthor = meta.hasAuthor() ? 1 : 0;
        String author = encode(meta.getAuthor());
        
        int hasPages = meta.hasPages() ? 1 : 0;
        
        String pages = "";
        for (String page : meta.getPages()) {
            pages += encode(page) + "!";
        }
        
        return ":"+hasTitle + ";" + title + ";" + hasAuthor + ";" + author + ";" + hasPages + ";" + pages.substring(0, pages.length());
    }

    public static ItemStack setBookMeta(ItemStack stack, String string) {
        BookMeta meta = (BookMeta) stack.getItemMeta();
        
        try {            
            String[] split = string.split(";");
            
            if (split.length < 5) {
                return stack;
            }
            
            boolean hasTitle = "1".equals(split[0]);
            String title = decode(split[1]);
            
            if (hasTitle) {
                meta.setTitle(title);
            }
            
            boolean hasAuthor = "1".equals(split[2]);

            if (hasAuthor) {
                String author = decode(split[3]);
                meta.setAuthor(author);
            }
            
            boolean hasPages = "1".equals(split[4]);
            if (hasPages) {
                
                List<String> pages = new ArrayList<String>();
                for (String s : split[5].split("!")) {
                    pages.add(decode(s));
                }

                meta.setPages(pages);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stack.setItemMeta(meta);
        return stack;
    }
    
    public static String getFireWork(ItemStack item) {
        FireworkMeta meta = (FireworkMeta) item.getItemMeta();

        if (!meta.hasEffects()) {
            return "";
        }

        String effects = "";

        for (FireworkEffect effect : meta.getEffects()) {

            String colors = "[";
            for (Color color : effect.getColors()) {
                colors += getColor(color) + "!";
            }
            if (!colors.equals("[")) { colors = colors.substring(0, colors.length() - 1); }
            colors += "]";

            String fadecolors = "[";
            for (Color color : effect.getFadeColors()) {
                fadecolors += getColor(color) + "!";
            }
            if (!fadecolors.equals("[")) { fadecolors = fadecolors.substring(0, fadecolors.length() - 1); }
            fadecolors += "]";

            String ef = "{"+getType(effect.getType()) +
                    ";" + colors +
                    ";" + fadecolors +
                    ";" + (effect.hasFlicker() ? "1" : "0") +
                    ";" + (effect.hasTrail() ? "1" : "0") + "}. ";
            effects += ef;
        }
        effects = effects.substring(0, effects.length() - 2);

        return meta.getPower() + ":" + effects;
    }

    public static ItemStack getFireWork(ItemStack stack, String string) {
        FireworkMeta meta = (FireworkMeta) stack.getItemMeta();

        try {
            String[] inv = string.split(":");
            
            if (inv.length < 6) {
                return stack;
            }
            
            int power = Integer.parseInt(inv[5]);

            List<String> effects = new ArrayList<String>();
            if (!inv[6].equals("[]")) {
                effects = Arrays.asList(inv[6].substring(1, inv[6].length() - 1).split("\\."));
            }

            for (String effect : effects) {
                FireworkEffect.Builder builder = FireworkEffect.builder();
                String[] data = effect.split(";");

                if (data.length < 4) {
                    continue;
                }

                if (data[0].contains("{")) {
                    data[0] = data[0].substring(1);
                }

                builder.with(getType(Integer.parseInt(data[0])));

                for (String color : Arrays.asList(data[1].substring(1, data[1].length() - 1).split("!"))) {
                    if (!color.isEmpty()) { builder.withColor(getColor(Integer.parseInt(color))); }
                }

                for (String color : Arrays.asList(data[2].substring(1, data[2].length() - 1).split("!"))) {
                    if (!color.isEmpty()) { builder.withFade(getColor(Integer.parseInt(color))); }
                }

                builder.flicker(Integer.parseInt(data[3]) == 1);
                if (data[4].contains("}")) {
                    data[4] = data[4].substring(0, data[4].length() - 1);
                }

                builder.trail(Integer.parseInt(data[4]) == 1);

                meta.addEffect(builder.build());
            }
            
            meta.setPower(power);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        stack.setItemMeta(meta);
        return stack;
    }
    
    public static Type getType(int id) {
        switch (id) {
            case 0:
                return Type.BALL;
            case 1:
                return Type.BALL_LARGE;
            case 2:
                return Type.BURST;
            case 3:
                return Type.CREEPER;
            case 4:
                return Type.STAR;
            default:
                return Type.BALL;
        }
    }
    
    public static int getType(Type type) {
        if (type == Type.BALL) {
            return 0;
        } else
        if (type == Type.BALL_LARGE) {
            return 1;
        } else
        if (type == Type.BURST) {
            return 2;
        } else
        if (type == Type.CREEPER) {
            return 3;
        } else
        if (type == Type.STAR) {
            return 4;
        }
        return 0;
    }
    
    public static int getColor(Color color) {
        return color.asRGB();
    }
    
    public static Color getColor(int rgb) {
        return Color.fromRGB(rgb);
    }
    
    public static String getItemMeta(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        
        int hasDisplayName = meta.hasDisplayName() ? 1 : 0;
        String displayName = encode(meta.getDisplayName());
        
        int hasLore = meta.hasLore() ? 1 : 0;
        
        String lore = "";
        if (meta.hasLore()) {
            for (String page : meta.getLore()) {
                lore += encode(page) + "!";
            }
        }
        
        if (!lore.isEmpty()) {
            lore = lore.substring(0, lore.length());
        }

        return ":"+hasDisplayName + ";" + displayName + ";" + hasLore + ";" + lore;
    }

    public static ItemStack setItemMeta(ItemStack stack, String string) {
        ItemMeta meta = stack.getItemMeta();

        try {
            String[] split = string.split(";");
            
            if (split.length < 3) {
                return stack;
            }

            boolean hasDisplayName = "1".equals(split[0]);
            String displayName = decode(split[1]);
            
            if (hasDisplayName) {
                meta.setDisplayName(displayName);
            }

            boolean hasLore = "1".equals(split[2]);
            if (hasLore) {
                List<String> lore = new ArrayList<String>();
                for (String s : split[3].split("!")) {
                    lore.add(decode(s));
                }

                meta.setLore(lore);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        stack.setItemMeta(meta);
        return stack;
    }

    public static String encode(String string) {
        return InventoryStack.encode(string);
    }

    public static String decode(String string) {
        return InventoryStack.decode(string);
    }
}