package me.FurH.Core.gc;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import me.FurH.Core.util.Utils;
import org.bukkit.Bukkit;

/**
 *
 * @author FurmigaHumana All Rights Reserved unless otherwise explicitly stated.
 */
public class MemoryMonitor {

    private static List<IMemoryMonitor> references;
    private SoftReference<byte[]> monitor;
    private int calls = 0;

    public MemoryMonitor() {

        references = Collections.synchronizedList(new ArrayList<IMemoryMonitor>());
        monitor = new SoftReference<byte[]>(new byte[ 1048576 * 10 ]);

        new Timer("FCoreLib Memory Monitor", true)
                .scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                if (monitor == null || monitor.get() == null) {

                    long free = getTotalFree();
                    System.out.println("Cleaning up " + references.size() + " references");

                    for (IMemoryMonitor reference : references) {
                        try {
                            reference.gc();
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    }

                    Runtime.getRuntime().runFinalization();
                    System.gc();

                    Bukkit.getPluginManager().callEvent(new GarbageEvent());

                    Runtime.getRuntime().runFinalization();
                    System.gc();

                    long freed = Math.abs(free - getTotalFree());
                    System.out.println("Memory Released " + Utils.getFormatedBytes(freed));

                    monitor = new SoftReference<byte[]>(new byte[ 1048576 * 10 ]); calls++;
                }
            }
        }, 50L, 50L);
    }

    public long getTotalFree() {
        Runtime rt = Runtime.getRuntime();

        long free = rt.freeMemory();
        long total = rt.totalMemory();
        long max = rt.maxMemory();
        long used = (total - free);

        return (max - used);
    }

    public static boolean register(IMemoryMonitor cleanable) {
        return references.add(cleanable);
    }

    public int getGCCalls() {
        return calls;
    }
}