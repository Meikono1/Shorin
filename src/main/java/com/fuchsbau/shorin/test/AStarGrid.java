package com.fuchsbau.shorin.test;

import java.util.Arrays;

/**
 * Simple A* on a 2D grid with 8-neighbor movement.
 * Costs: entering a tile costs 1 or 2 (checkerboard: (r+c)&1==1 => 2).
 *
 * Reuses internal arrays, no per-node allocations.
 */
public final class AStarGrid {
    private final int rows;
    private final int cols;

    // Min-heap for open set (stores indices)
    private final IntMinHeap openSet;

    // Neighbors (8-dir)
    private static final int[] DR = {-1,-1, 0, 1, 1, 1, 0,-1};
    private static final int[] DC = { 0, 1, 1, 1, 0,-1,-1,-1};

    // constants
    private static final int COST_STRAIGHT = 5;
    private static final int COST_DIAG_A = 5;
    private static final int COST_DIAG_B = 10;

    // state arrays: size = (rows*cols*2)
    private final int[] g2;       // best cost so far for state
    private final int[] f2;       // g + h
    private final int[] parent2;  // previous state
    private final byte[] stateMark2; // 0=unseen,1=open,2=closed

    public AStarGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        int n = rows * cols;
        int states = n * 2;

        this.g2 = new int[states];
        this.f2 = new int[states];
        this.parent2 = new int[states];
        this.stateMark2 = new byte[states];
        this.openSet = new IntMinHeap(states);
    }

    /**
     * Finds a path. Returns path as array of indices (idx = r*cols+c), start..goal.
     * Returns empty array if no path.
     *
     * passable: whether a tile can be entered.
     */

    public int[] findPathDiagAlternating(
            int startRow, int startCol,
            int goalRow, int goalCol,
            Passable passable
    ) {
        if (!inBounds(startRow, startCol) || !inBounds(goalRow, goalCol)) return new int[0];
        if (!passable.isPassable(startRow, startCol) || !passable.isPassable(goalRow, goalCol)) return new int[0];

        int startIdx = toIndex(startRow, startCol);
        int goalIdx  = toIndex(goalRow, goalCol);

        Arrays.fill(g2, Integer.MAX_VALUE);
        Arrays.fill(f2, Integer.MAX_VALUE);
        Arrays.fill(parent2, -1);
        Arrays.fill(stateMark2, (byte)0);
        openSet.clear();

        // start with parity=0 (next diagonal costs 5)
        int startState = startIdx * 2;
        g2[startState] = 0;
        f2[startState] = heuristic5(startRow, startCol, goalRow, goalCol);
        stateMark2[startState] = 1;
        openSet.push(startState, f2[startState]);

        while (!openSet.isEmpty()) {
            int curState = openSet.popMin();
            if (stateMark2[curState] == 2) continue; // stale
            stateMark2[curState] = 2;

            int curIdx = curState / 2;
            int parity = curState & 1;

            if (curIdx == goalIdx) break;

            int cr = curIdx / cols;
            int cc = curIdx - cr * cols;

            int curG = g2[curState];
            if (curG == Integer.MAX_VALUE) continue;

            for (int dir = 0; dir < 8; dir++) {
                int nr = cr + DR[dir];
                int nc = cc + DC[dir];
                if (!inBounds(nr, nc)) continue;
                if (!passable.isPassable(nr, nc)) continue;

                boolean diagonal = (DR[dir] != 0 && DC[dir] != 0);

                int stepCost;
                int nextParity = parity;
                if (diagonal) {
                    stepCost = (parity == 0) ? COST_DIAG_A : COST_DIAG_B;
                    nextParity = parity ^ 1;
                } else {
                    stepCost = COST_STRAIGHT;
                }

                int nIdx = toIndex(nr, nc);
                int nState = nIdx * 2 + nextParity;

                if (stateMark2[nState] == 2) continue;

                int ng = curG + stepCost;
                if (ng < g2[nState]) {
                    g2[nState] = ng;
                    parent2[nState] = curState;

                    int h = heuristic5(nr, nc, goalRow, goalCol);
                    int nf = ng + h;
                    f2[nState] = nf;

                    stateMark2[nState] = 1;
                    openSet.push(nState, nf);
                }
            }
        }

        // pick best goal state (parity 0 or 1)
        int goalState0 = goalIdx * 2;
        int goalState1 = goalIdx * 2 + 1;
        int bestGoalState = (g2[goalState1] < g2[goalState0]) ? goalState1 : goalState0;

        if (g2[bestGoalState] == Integer.MAX_VALUE) return new int[0];
        return reconstructPathStates(bestGoalState);
    }

    // admissible heuristic: each move costs at least 5, and with 8-neighbor lower bound is max(dx,dy)
    private static int heuristic5(int r, int c, int gr, int gc) {
        int dx = Math.abs(gc - c);
        int dy = Math.abs(gr - r);
        return COST_STRAIGHT * Math.max(dx, dy);
    }

    private int[] reconstructPathStates(int goalState) {
        // count
        int len = 0;
        for (int s = goalState; s != -1; s = parent2[s]) len++;

        int[] path = new int[len];
        int i = len - 1;
        for (int s = goalState; s != -1; s = parent2[s]) {
            int idx = s / 2;         // convert state -> tile index
            path[i--] = idx;
        }
        return path;
    }

    private boolean inBounds(int r, int c) {
        return (r >= 0 && r < rows && c >= 0 && c < cols);
    }

    private int toIndex(int r, int c) {
        return r * cols + c;
    }

    @FunctionalInterface
    public interface Passable {
        boolean isPassable(int r, int c);
    }

    // --- Minimal int min-heap storing (idx, key) with stale entries allowed ---
    private static final class IntMinHeap {
        private final int[] heapIdx;
        private final int[] heapKey;
        private int size = 0;

        IntMinHeap(int capacity) {
            heapIdx = new int[capacity * 2]; // allow stales without resizing in many cases
            heapKey = new int[capacity * 2];
        }

        void clear() { size = 0; }
        boolean isEmpty() { return size == 0; }

        void push(int idx, int key) {
            if (size >= heapIdx.length) return; // hard stop; or resize if you want
            int i = size++;
            heapIdx[i] = idx;
            heapKey[i] = key;
            siftUp(i);
        }

        int popMin() {
            int res = heapIdx[0];
            size--;
            heapIdx[0] = heapIdx[size];
            heapKey[0] = heapKey[size];
            siftDown(0);
            return res;
        }

        private void siftUp(int i) {
            while (i > 0) {
                int p = (i - 1) >>> 1;
                if (heapKey[p] <= heapKey[i]) break;
                swap(p, i);
                i = p;
            }
        }

        private void siftDown(int i) {
            while (true) {
                int l = (i << 1) + 1;
                if (l >= size) break;
                int r = l + 1;
                int m = (r < size && heapKey[r] < heapKey[l]) ? r : l;
                if (heapKey[i] <= heapKey[m]) break;
                swap(i, m);
                i = m;
            }
        }

        private void swap(int a, int b) {
            int ti = heapIdx[a]; heapIdx[a] = heapIdx[b]; heapIdx[b] = ti;
            int tk = heapKey[a]; heapKey[a] = heapKey[b]; heapKey[b] = tk;
        }
    }
}
