package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;

import java.util.ArrayList;
import java.util.List;

public class ArrayCollisionMatrix {
    private List<Integer> matrix = new ArrayList<>();

    public int get(PhysicsBody bi, PhysicsBody bj) {
        int i = bi.index;
        int j = bj.index;
        if (j > i) { int temp = j; j = i; i = temp; }
        int idx = ((i * (i + 1)) >> 1) + j - 1;
        if (idx < 0 || idx >= matrix.size()) return 0;
        return matrix.get(idx);
    }

    public void set(PhysicsBody bi, PhysicsBody bj, boolean value) {
        int i = bi.index;
        int j = bj.index;
        if (j > i) { int temp = j; j = i; i = temp; }
        int idx = ((i * (i + 1)) >> 1) + j - 1;
        while (matrix.size() <= idx) matrix.add(0);
        matrix.set(idx, value ? 1 : 0);
    }

    public void reset() {
        for (int i = 0; i < matrix.size(); i++) {
            matrix.set(i, 0);
        }
    }

    public void setNumObjects(int n) {
        int size = (n * (n - 1)) >> 1;
        while (matrix.size() < size) matrix.add(0);
        while (matrix.size() > size) matrix.remove(matrix.size() - 1);
    }
}