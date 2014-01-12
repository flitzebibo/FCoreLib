package me.FurH.Core.ip;

import java.util.regex.Pattern;
import org.bukkit.entity.Player;

/**
 *
 * @author FurmigaHumana
 */
public class IpUtils {

    private static Pattern IPv4 = Pattern.compile("((\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})?)|(\\d{1,3}(?:\\,\\d{1,3}){3}(?::\\d{1,5})?)|(\\d{1,3}(?:\\-\\d{1,3}){3}(?::\\d{1,5})?)|(\\d{1,3}(?: \\d{1,3}){3}(?::\\d{1,5})?))");
    private static Pattern IPv6 = Pattern.compile("(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}");

    /**
     * Get if the given string is an IPv4 address
     * 
     * @param ip the ip string
     * @return true if the string is an IPv4 address, false otherwise.
     */
    public static boolean isIPv4(String ip) {
        return IPv4.matcher(ip).matches();
    }
    
    /**
     * Get if the given string is an IPv6 address
     * 
     * @param ip the ip string
     * @return true if the string is an IPv6 address, false otherwise.
     */
    public static boolean isIPv6(String ip) {
        return IPv6.matcher(ip).matches();
    }
    
    /**
     * Replace the . of the ip to - to allow better database compatibility
     * 
     * @param ip the ip to replace
     * @return the ip with the dots replaced
     */
    public static String getIpFromDb(String ip) {
        return ip.replaceAll("-", ".");
    }
    
    /**
     * Replace the - of the ip to . to retrieve the original ip after the usage of the @getIpFromDb method
     * 
     * @param ip the ip to restore
     * @return the ip with the dots restored
     */
    public static String getIpToDb(String ip) {
        return ip.replaceAll("\\.", "-");
    }

    /**
     * Get the decimal representation of the given ip
     * 
     * @param ip the ip address
     * @return the decimal representation of the ip
     */
    public static long ipToDecimal(String ip) {
        long result = 0;
        String[] split = ip.split("\\.");

        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(split[3 - i]) << (i * 8));
        }

        return result & 0xFFFFFFFF;
    }

    /**
     * Get the ip address from the decimal representation
     * 
     * @param ip the decimal ip
     * @return the ip address
     */
    public static String decimalToIp(long ip) {
        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                sb.insert(0, '.');
            }

            ip >>= 8;
        }

        return sb.toString();
    }
    
    /**
     * Get the player ip address
     * 
     * @param player the player to get the ip
     * @return the ip address without ports
     */
    public static String getPlayerIp(Player player) {
        return player.getAddress().toString().substring(1, player.getAddress().toString().indexOf(':'));
    }
}
