package me.FurH.Core.gc;

/**
 *
 * @author FurmigaHumana
 * All Rights Reserved unless otherwise explicitly stated.
 */
public interface IMemoryMonitor {

    /**
     * This method is called by the memory monitor, it is intended to release objects that may hold data on memory.
     *
     * @throws Throwable
     */
    public void gc() throws Throwable;
}
