package me.FurH.Core.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import me.FurH.Core.gc.IMemoryMonitor;
import me.FurH.Core.gc.MemoryMonitor;

/**
 *
 * @param <K> 
 * @param <V> 
 * @author FurmigaHumana
 */
public class CoreLRUCache<K, V> extends LinkedHashMap<K, V> implements IMemoryMonitor {

    private static final long serialVersionUID = -80132122077195160L;
    private boolean softCache = false;

    private int capacity = 0;
    private int reads = 0;
    private int writes = 0;

    /**
     * Creates a new LRU cache with a limited size, this cache is not thread-safe and should not be used on multi-thread systems
     * 
     * @param cacheSize the cache size limit
     */
    public CoreLRUCache(int cacheSize) {
        super();
        this.capacity = cacheSize;
    }
    
    public CoreLRUCache(int cacheSize, boolean softCache) {
        super();
        this.capacity = cacheSize;
        setSoftCache(softCache);
    }
    
    /**
     * Creates a new LRU cache with no size limit, this cache is not thread-safe and should not be used on multi-thread systems
     *
     */
    public CoreLRUCache() {
        super();
        this.capacity = 0;
    }
    
    public CoreLRUCache(boolean softCache) {
        super();
        this.capacity = 0;
        setSoftCache(softCache);
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
        return super.put(key, value);
    }
    
    /**
     * Get the key based on its value
     *
     * @param value the value to get the key
     * @return the Key of the value, or null if none
     */
    public K getKey(V value) {
        K ret = null;

        List<K> keys = new ArrayList<K>(keySet());
        for (K key : keys) {
            reads++;
            if (get(key).equals(value)) {
                ret = key;
                break;
            }
        }
        
        keys.clear();
        return ret;
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
    protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
        return capacity > 0 && size() > (capacity);
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
        this.reads = 0;
        this.writes = 0;
        this.clear();
    }
}
