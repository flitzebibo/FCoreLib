package me.FurH.Core;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 *
 * @author FurmigaHumana
 */
public class CoreValidator {
    
    /**
     * Check if the server is allowed to enable this plugin
     *
     * @return true if the server is allowed, false otherwise
     */
    public static boolean isAllowedServer() {
        boolean ret = false;

        try {

            URL url = new URL("http://localhost/core/validate.php?ip=" + getServerIp());
            URLConnection con = url.openConnection();
            InputStream stream = con.getInputStream();

            Scanner scanner = new Scanner(stream);

            if (scanner.findInLine("yes") != null) {
                ret = true;
            }

            scanner.close();
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return ret;
    }
    
    private static String getServerIp() {
        String ret = "127.0.0.1";
        
        try {

            URL url = new URL("http://localhost/core/index.php");
            URLConnection con = url.openConnection();
            InputStream stream = con.getInputStream();

            Scanner scanner = new Scanner(stream);

            if (scanner.hasNext()) {
                ret = scanner.nextLine();
            }

            if (ret.equalsIgnoreCase("localhost") || ret.equalsIgnoreCase("0.0.0.0") || ret.equalsIgnoreCase("::1")) {
                ret = "127.0.0.1";
            }

            scanner.close();
            stream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        
        return ret;
    }
}
