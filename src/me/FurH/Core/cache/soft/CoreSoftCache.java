package me.FurH.Core.cache.soft;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @param <K> 
 * @param <V> 
 * @author FurmigaHumana
 */
public class CoreSoftCache<K, V> {

    private static final long serialVersionUID = -80132122077195160L;

    private final ReferenceQueue<V> queue = new ReferenceQueue<V>();
    private final LinkedHashMap<K, SoftReference<V>> map;

    private int capacity = 0;
    private int reads = 0;
    private int writes = 0;

    /**
     * Creates a new LRU cache with a limited size, this cache is not thread-safe and should not be used on multi-thread systems
     * 
     * @param cacheSize the cache size limit
     */
    public CoreSoftCache(int cacheSize) {

        map = new LinkedHashMap<K, SoftReference<V>>(cacheSize, 0.75f, true) {

            private static final long serialVersionUID = 2674509550119308224L;

            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<K, SoftReference<V>> eldest) {
                return capacity > 0 && (size() > (capacity));
            }
        };
        
        this.capacity = cacheSize;
    }

    public CoreSoftCache() {
        this(0);
    }

    public V get(K key) {
        reads++;
        
        SoftReference<V> soft = map.get(key);
        if (soft != null) {

            V result = soft.get();
            if (result == null) {
                map.remove(key); cleanup();
            }

            return result;
        }

        map.remove(key);
        return null;
    }

    public V put(K key, V value) {
        writes++;

        SoftReference<V> soft = new SoftReference<V>(value, queue);
        map.put(key, soft);

        return soft.get();
    }
    
    /**
     * Get the key based on its value
     *
     * @param value the value to get the key
     * @return the Key of the value, or null if none
     */
    public K getKey(V value) {
        K ret = null;

        List<K> keys = new ArrayList<K>(map.keySet());
        
        for (K key : keys) {
            
            V get = get(key);
            
            if (get == null) {
                continue;
            }
            
            if (get.equals(value)) {
                ret = key;
                break;
            }
        }

        keys.clear();
        return ret;
    }

    public K removeValue(V value) {
        K key = getKey(value);
        
        if (key != null) {
            remove(key);
        }
        
        return key;
    }
    
    public V remove(K key) {
        writes++; reads++;

        SoftReference<V> ret = map.remove(key);
        if (ret == null) {
            return null;
        }

        return ret.get();
    }

    public boolean containsValue(V value) {
        reads++;
        return map.containsValue(new SoftReference<V>(value));
    }
    
    public boolean containsKey(K key) {
        reads++;
        return map.containsKey(key);
    }

    public void clear() {
        map.clear();
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

    public int size() {
        return map.size();
    }
    
    public void cleanup() {
        Reference<? extends V> sv;
        while ((sv = queue.poll()) != null) {
            removeValue(sv.get());
        }
    }
}