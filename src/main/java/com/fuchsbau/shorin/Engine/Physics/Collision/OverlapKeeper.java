package com.fuchsbau.shorin.Engine.Physics.Collision;

import java.util.ArrayList;
import java.util.List;

public class OverlapKeeper {

    public List<Integer> current = new ArrayList<>();
    public List<Integer> previous = new ArrayList<>();

    public int getKey(int i, int j) {
        if (j < i) {
            int temp = j;
            j = i;
            i = temp;
        }
        return (i << 16) | j;
    }

    public void set(int i, int j) {
        int key = getKey(i, j);
        int index = 0;
        while (index < current.size() && key > current.get(index)) index++;
        if (index < current.size() && key == current.get(index)) return;
        current.add(index, key);
    }

    public void tick() {
        List<Integer> tmp = current;
        current = previous;
        previous = tmp;
        current.clear();
    }

    public void getDiff(List<Integer> additions, List<Integer> removals) {
        List<Integer> a = current;
        List<Integer> b = previous;

        int j = 0;
        for (int keyA : a) {
            while (j < b.size() && keyA > b.get(j)) j++;
            boolean found = j < b.size() && keyA == b.get(j);
            if (!found) unpackAndPush(additions, keyA);
        }

        j = 0;
        for (int keyB : b) {
            while (j < a.size() && keyB > a.get(j)) j++;
            boolean found = j < a.size() && a.get(j) == keyB;
            if (!found) unpackAndPush(removals, keyB);
        }
    }

    private void unpackAndPush(List<Integer> array, int key) {
        array.add((key & 0xffff0000) >> 16);
        array.add(key & 0x0000ffff);
    }
}