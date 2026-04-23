package com.fuchsbau.shorin.Engine.Physics.Util;

import java.util.HashMap;
import java.util.Map;

public class TupleDictionary {

    private final Map<Long, Object> data = new HashMap<>();

    private long key(int i, int j) {
        return ((long) i << 32) | (j & 0xFFFFFFFFL);
    }

    public void set(int i, int j, Object value) {
        data.put(key(i, j), value);
    }

    public <T> T get(int i, int j) {
        return (T) data.get(key(i, j));
    }

    public void delete(int i, int j) {
        data.remove(key(i, j));
    }
}
