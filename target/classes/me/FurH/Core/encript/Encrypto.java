package me.FurH.Core.encript;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.file.FileUtils;

/**
 *
 * @author FurmigaHumana
 */
public class Encrypto {

    /**
     * Generates a random encrypted salt with the defined length using random UUID's
     * 
     * @param algorithm the algorithm used to encrypt the salt
     * @param length the salt length
     * @return the salt string
     * @throws CoreException
     */
    public static String salt(String algorithm, int length) throws CoreException {
        String hash = "";

        while (length > hash.length()) {
            hash += hash(algorithm, UUID.randomUUID().toString());
        }

        return hash.substring(0, length);
    }

    /**
     * Encrypts the given String into the defined algorithm and convert the result into a hex String
     * 
     * @param algorithm the encryptation algorithm
     * @param string the string to encrypt
     * @return the encrypted string
     * @throws CoreException
     */
    public static String hash(String algorithm, String string) throws CoreException {

        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            return Whirlpool.display(digest(algorithm, string));
        }

        return hex(digest(algorithm, string));
    }
    
    /**
     * Generate the hash for the file with the given algorithm and converts it to hex
     * 
     * @param algorithm the encryptation algorithm
     * @param file the file to generate the hash
     * @return the encrypted string
     * @throws CoreException
     */
    public static String hash(String algorithm, File file) throws CoreException {

        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            throw new CoreException("whirlpool is not supported with files!");
        }

        return hex(digest(algorithm, file));
    }

    /**
     * Encrypts the given String into the defined algorithm
     * 
     * @param algorithm the encryptation algorithm
     * @param string the string to encrypt
     * @return the encrypted array of bytes
     * @throws CoreException
     */
    public static byte[] digest(String algorithm, String string) throws CoreException {
        
        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            return whirlpool(string);
        }
        
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new CoreException(ex, "There is no algorithm called: " + algorithm);
        }

        return md.digest(string.getBytes());
    }
    
    /**
     * Encrypts the given File into the defined algorithm
     * 
     * @param algorithm the encryptation algorithm
     * @param file the file to generate the hash
     * @return the encrypted array of bytes
     * @throws CoreException
     */
    public static byte[] digest(String algorithm, File file) throws CoreException {
        
        if (algorithm.equalsIgnoreCase("whirl-pool")) {
            throw new CoreException("whirlpool is not supported with files!");
        }
        
        MessageDigest md = null;

        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new CoreException(ex, "There is no algorithm called: " + algorithm);
        }
        
        FileInputStream is = null;

        try {

            is = new FileInputStream(file);
            
            byte[] data = new byte[ 1024 ];
            int read = is.read(data, 0, 1024);

            while (read > -1) {
                md.update(data, 0, read);
                read = is.read(data, 0, 1024);
            }

        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to generate '"+algorithm+"' hash to the '"+file.getName()+"' file");
        } finally {
            FileUtils.closeQuietly(is);
        }

        return md.digest();
    }

    private static byte[] whirlpool(String string) {
        Whirlpool whirlpool = new Whirlpool();
        
        byte[] digest = new byte[ Whirlpool.DIGESTBYTES ];

        whirlpool.NESSIEinit();
        whirlpool.NESSIEadd(string);
        whirlpool.NESSIEfinalize(digest);

        return digest;
    }

    /**
     * Convertes the encrypted array of bytes into a hex string
     * 
     * @param data the encrypted array
     * @return the encrypted hex string
     * 
     * source http://stackoverflow.com/questions/4895523/java-string-to-sha1
     */
    public static String hex(byte[] data) {
        String result = "";

        for (int i = 0; i < data.length; i++) {
            result += Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }

    /**
     * Convertes the encrypted array of bytes into a hex string using supression points
     * 
     * @param data the encrypted array
     * @param supress supression points
     * @return the encrypted hex string
     */
    public static String hex(byte[] data, int supress) {
        String result = "";

        for (int i = 0; i < data.length; i++) {
            if (i % supress > 0) {
                result += Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1);
            }
        }

        return result;
    }
}