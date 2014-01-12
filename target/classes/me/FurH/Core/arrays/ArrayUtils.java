package me.FurH.Core.arrays;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import me.FurH.Core.exceptions.CoreException;
import me.FurH.Core.file.FileUtils;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public class ArrayUtils {

    /**
     * Join the 2D array into a single array
     *
     * @param buffer the 2D array to join
     * @return a single array with all the 2D array data
     */
    public static byte[] joinByteArray(byte[][] buffer) {

        int lenght = 0;
        for (byte[] slice : buffer) {
            lenght += slice.length;
        }

        byte[] ret = new byte [ lenght ];
        int read = 0;

        for (int j1 = 0; j1 < buffer.length; j1++) {
            byte[] copy = buffer[j1];

            System.arraycopy(copy, 0, ret, read, copy.length);

            read += copy.length;
        }

        return ret;
    }

    /**
     * Split the byte array into pieces of the given size, the last piece may not have the same size of the others.
     *
     * @param array the array to split
     * @param each the size of each piece
     * @return the 2D array with the pieces
     */
    public static byte[][] splitByteArray(byte[] array, int each) {
        
        int slice = (int) Math.floor(array.length / each);
        byte[][] slices = new byte[ slice + 1 ][];

        int read = 0;

        for (int j1 = 0; j1 < slices.length; j1++) {

            int to = read + each;

            if (to > array.length) {
                to = array.length;
            }

            slices[j1] = Arrays.copyOfRange(array, read, to);
            read += each;
        }

        return slices;
    }
    
    /**
     * Compress the given byte array using the java.util.zip.Deflater
     *
     * @param data the data to be compressed
     * @param level the level of compression (-1 to 9)
     * @return the compressed data
     * @throws CoreException
     */
    public static byte[] compress(byte[] data, int level) throws CoreException {

        if (level < Deflater.DEFAULT_COMPRESSION) {
            throw new CoreException("The compression level can't be lower than " + Deflater.DEFAULT_COMPRESSION);
        }

        if (level > Deflater.BEST_COMPRESSION) {
            throw new CoreException("The compression level can't be higher than " + Deflater.BEST_COMPRESSION);
        }

        ByteArrayOutputStream baos = null;
        Deflater def = null;

        try {

            def = new Deflater(level);
            def.setInput(data);

            baos = new ByteArrayOutputStream(data.length);
            def.finish();

            byte[] buffer = new byte[ 1024 ];
            while (!def.finished()) {
                int read = def.deflate(buffer);
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to compress data!");
        } finally {
            FileUtils.closeQuietly(baos);
            closeQuietly(def);
        }
    }
    
    /**
     * Decompress the given compressed data using the java.util.zip.Inflater
     *
     * @param data the data compressed with the @compress method
     * @return the decompressed data
     * @throws CoreException
     */
    public static byte[] decompress(byte[] data) throws CoreException {

        ByteArrayOutputStream baos = null;
        Inflater inf = null;

        try {
            
            inf = new Inflater();
            inf.setInput(data);

            baos = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[ 1024 ];

            while (!inf.finished()) {
                int read = inf.inflate(buffer);
                baos.write(buffer, 0, read);
            }

            return baos.toByteArray();
        } catch (Exception ex) {
            throw new CoreException(ex, "Failed to decompress data!");
        } finally {
            FileUtils.closeQuietly(baos);
            closeQuietly(inf);
        }
    }

    /**
     * Closes the stream quietly
     * 
     * @param stream the stream to close
     */
    public static void closeQuietly(Deflater stream) {
        if (stream != null) {
            try {
                stream.end();
            } catch (Throwable ex) { }
        }
    }
    
    /**
     * Closes the stream quietly
     * 
     * @param stream the stream to close
     */
    public static void closeQuietly(Inflater stream) {
        if (stream != null) {
            try {
                stream.end();
            } catch (Throwable ex) { }
        }
    }
}