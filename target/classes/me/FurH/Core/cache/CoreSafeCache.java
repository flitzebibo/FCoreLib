package me.FurH.Core.cache;

import java.util.concurrent.ConcurrentHashMap;
import me.FurH.Core.gc.IMemoryMonitor;
import me.FurH.Core.gc.MemoryMonitor;

/**
 *
 * @param <K> 
 * @param <V> 
 * @author FurmigaHumana
 */
public class CoreSafeCache<K, V> extends ConcurrentHashMap<K, V> implements IMemoryMonitor {
    
    private static final long serialVersionUID = 426161011525380934L;
    private boolean softCache = false;

    private int capacity = -1;
    private int size = 0;
    
    private int reads = 0;
    private int writes = 0;

    /**
     * Creates a new Safe Cache with a maximum capacity
     *
     * @param cacheSize the maximum capacity of this cache
     */
    public CoreSafeCache(int cacheSize) {
        super();
        this.capacity = cacheSize;
    }
    
    public CoreSafeCache(int cacheSize, boolean softCache) {
        super();
        this.capacity = cacheSize;
        setSoftCache(softCache);
    }

    /**
     * Creates a new Safe Cache with no size limit
     */
    public CoreSafeCache() {
        super();
    }

    public CoreSafeCache(boolean softCache) {
        super(); setSoftCache(softCache);
    }

    /**
     * Set this cache to soft cache, it means that it can be automatically cleaned out to release memory.
     *
     * @param softCache true if this is a soft cache, false otherwise.
     */
    public final void setSoftCache(boolean softCache) {
        this.softCache = softCache;

        if (this.softCache) {
            MemoryMonitor.register(this);
        }
    }

    @Override
    public V get(Object key) {
        reads++;
        return super.get(key);
    }

    @Override
    public V put(K key, V value) {
        writes++;

        if (containsKey(key)) {
            return super.replace(key, value);
        }
        
        size++;
        
        if (capacity != -1) {
            if (size > capacity) {
                super.clear();
            }
        }
        
        return super.put(key, value);
    }
    
    @Override
    public V remove(Object key) {
        
        size--;
        
        return super.remove(key);
    }
    
    /**
     * Get the key based on its value
     *
     * @param value the value to get the key
     * @return the Key of the value, or null if none
     */
    public K getKey(V value) {
        
        for (K key : keySet()) {
            if (get(key).equals(value)) {
                return key;
            }
        }
        
        return null;
    }
    
    /**
     * Remove a value from the map, the key will also be removed
     *
     * @param value the value
     * @return the key removed
     */
    public K removeValue(V value) {
        K key = getKey(value);
        
        if (key != null) {
            remove(key);
        }
        
        return key;
    }
    
    @Override
    public boolean containsValue(Object value) {
        reads++;
        return super.containsValue(value);
    }
    
    @Override
    public boolean containsKey(Object key) {
        reads++;
        return super.containsKey(key);
    }

    @Override
    public void clear() {
        this.size = 0;
        super.clear();
    }

    /**
     * Get the total reads of this cache
     *
     * @return the total cache reads
     */
    public int getReads() {
        return reads;
    }

    /**
     * Get the total writes of this cache
     *
     * @return the total cache writes
     */
    public int getWrites() {
        return writes;
    }
    
    /**
     * Get the maximum size of this cache
     *
     * @return the cache capacity
     */
    public int getMaxSize() {
        return capacity;
    }

    @Override
    public void gc() throws Throwable {
        this.size = 0;
        this.reads = 0;
        this.writes = 0;
        this.clear();
    }
}