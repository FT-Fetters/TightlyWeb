package com.heybcat.tightlyweb.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Fetters
 */
public class TightlyCache {

    private final Map<String, Object> cache;

    private final Map<String, Long> ttl;

    public static TightlyCache newCache() {
        return new TightlyCache();
    }

    private TightlyCache() {
        this.cache = new ConcurrentHashMap<>();
        this.ttl = new ConcurrentHashMap<>();
    }

    public void put(String key, Object value) {
        this.cache.put(key, value);
        this.ttl.put(key, -1L);
    }

    public void put(String key, Object value, long ttl) {
        this.cache.put(key, value);
        this.ttl.put(key, System.currentTimeMillis() + ttl);
    }


    public Object get(String key) {
        Long l = this.ttl.get(key);
        if (l == null) {
            return null;
        }
        if (l == -1) {
            return this.cache.get(key);
        }
        if (l < System.currentTimeMillis()) {
            this.cache.remove(key);
            return null;
        } else {
            return this.cache.get(key);
        }
    }


}
