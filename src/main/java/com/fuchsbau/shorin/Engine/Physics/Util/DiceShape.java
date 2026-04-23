package com.fuchsbau.shorin.Engine.Physics.Util;

import com.fuchsbau.shorin.Engine.Physics.Shape.ShapeType;

import java.util.*;

public final class DiceShape {

    public static class DiceShapeData {
        public ShapeType type;
        public double[][] vertices;
        public int[][] faces;
        public int[] faceValues;
        public boolean skipLastFaceIndex;
        // Cylinder-spezifisch
        public double radiusTop = 1;
        public double radiusBottom = 1;
        public double height = 0.1;
        public int numSegments = 8;
    }

    private static final Map<String, DiceShapeData> SHAPES = new HashMap<>();

    static {
        // ── D2 ──────────────────────────────────────────────────────────────
        DiceShapeData d2 = new DiceShapeData();
        d2.type = ShapeType.CYLINDER;
        d2.radiusTop = 1;
        d2.radiusBottom = 1;
        d2.height = 0.1;
        d2.numSegments = 8;
        d2.skipLastFaceIndex = true;
        d2.faceValues = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 2, 1};
        SHAPES.put("d2", d2);

        // ── D4 ──────────────────────────────────────────────────────────────
        DiceShapeData d4 = new DiceShapeData();
        d4.type = ShapeType.CONVEXPOLYHEDRON;
        d4.vertices = new double[][]{{1, 1, 1}, {-1, -1, 1}, {-1, 1, -1}, {1, -1, -1}};
        d4.faces = new int[][]{{1, 0, 2, 1}, {0, 1, 3, 2}, {0, 3, 2, 3}, {1, 2, 3, 4}};
        d4.skipLastFaceIndex = true;
        d4.faceValues = new int[]{2, 4, 3, 1};
        SHAPES.put("d4", d4);

        // ── D6 ──────────────────────────────────────────────────────────────
        DiceShapeData d6 = new DiceShapeData();
        d6.type = ShapeType.CONVEXPOLYHEDRON;
        d6.vertices = new double[][]{{-1, -1, -1}, {1, -1, -1}, {1, 1, -1}, {-1, 1, -1}, {-1, -1, 1}, {1, -1, 1}, {1, 1, 1}, {-1, 1, 1}};
        d6.faces = new int[][]{{0, 3, 2, 1, 1}, {1, 2, 6, 5, 2}, {0, 1, 5, 4, 3}, {3, 7, 6, 2, 4}, {0, 4, 7, 3, 5}, {4, 5, 6, 7, 6}};
        d6.skipLastFaceIndex = true;
        d6.faceValues = new int[]{3, 5, 1, 6, 2, 4};
        SHAPES.put("d6", d6);

        // ── D8 ──────────────────────────────────────────────────────────────
        DiceShapeData d8 = new DiceShapeData();
        d8.type = ShapeType.CONVEXPOLYHEDRON;
        d8.vertices = new double[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        d8.faces = new int[][]{{0, 2, 4, 1}, {0, 4, 3, 2}, {0, 3, 5, 3}, {0, 5, 2, 4}, {1, 3, 4, 5}, {1, 4, 2, 6}, {1, 2, 5, 7}, {1, 5, 3, 8}};
        d8.skipLastFaceIndex = true;
        d8.faceValues = new int[]{5, 3, 1, 7, 2, 8, 6, 4};
        SHAPES.put("d8", d8);

        // ── D10 ─────────────────────────────────────────────────────────────
        DiceShapeData d10 = new DiceShapeData();
        d10.type = ShapeType.CONVEXPOLYHEDRON;
        d10.vertices = new double[][]{
                {1, 0, -0.105}, {0.809, 0.588, 0.105}, {0.309, 0.951, -0.105},
                {-0.309, 0.951, 0.105}, {-0.809, 0.588, -0.105}, {-1, 0, 0.105},
                {-0.809, -0.588, -0.105}, {-0.309, -0.951, 0.105}, {0.309, -0.951, -0.105},
                {0.809, -0.588, 0.105}, {0, 0, -1}, {0, 0, 1}
        };
        d10.faces = new int[][]{
                {5, 6, 7, 11, 0}, {4, 3, 2, 10, 1}, {1, 2, 3, 11, 2}, {0, 9, 8, 10, 3},
                {7, 8, 9, 11, 4}, {8, 7, 6, 10, 5}, {9, 0, 1, 11, 6}, {2, 1, 0, 10, 7},
                {3, 4, 5, 11, 8}, {6, 5, 4, 10, 9}
        };
        d10.skipLastFaceIndex = true;
        d10.faceValues = new int[]{1, 2, 5, 10, 7, 4, 3, 8, 9, 6};
        SHAPES.put("d10", d10);

        // ── D12 ─────────────────────────────────────────────────────────────
        DiceShapeData d12 = new DiceShapeData();
        d12.type = ShapeType.CONVEXPOLYHEDRON;
        d12.vertices = new double[][]{
                {0, 0.618, 1.618}, {0, 0.618, -1.618}, {0, -0.618, 1.618}, {0, -0.618, -1.618},
                {1.618, 0, 0.618}, {1.618, 0, -0.618}, {-1.618, 0, 0.618}, {-1.618, 0, -0.618},
                {0.618, 1.618, 0}, {0.618, -1.618, 0}, {-0.618, 1.618, 0}, {-0.618, -1.618, 0},
                {1, 1, 1}, {1, 1, -1}, {1, -1, 1}, {1, -1, -1}, {-1, 1, 1}, {-1, 1, -1}, {-1, -1, 1}, {-1, -1, -1}
        };
        d12.faces = new int[][]{
                {2, 14, 4, 12, 0, 1}, {15, 9, 11, 19, 3, 2}, {16, 10, 17, 7, 6, 3}, {6, 7, 19, 11, 18, 4},
                {6, 18, 2, 0, 16, 5}, {18, 11, 9, 14, 2, 6}, {1, 17, 10, 8, 13, 7}, {1, 13, 5, 15, 3, 8},
                {13, 8, 12, 4, 5, 9}, {5, 4, 14, 9, 15, 10}, {0, 12, 8, 10, 16, 11}, {3, 19, 7, 17, 1, 12}
        };
        d12.skipLastFaceIndex = true;
        d12.faceValues = new int[]{7, 5, 3, 11, 12, 9, 4, 1, 2, 10, 8, 6};
        SHAPES.put("d12", d12);

        // ── D20 ─────────────────────────────────────────────────────────────
        DiceShapeData d20 = new DiceShapeData();
        d20.type = ShapeType.CONVEXPOLYHEDRON;
        d20.vertices = new double[][]{
                {-1, 1.618, 0}, {1, 1.618, 0}, {-1, -1.618, 0}, {1, -1.618, 0},
                {0, -1, 1.618}, {0, 1, 1.618}, {0, -1, -1.618}, {0, 1, -1.618},
                {1.618, 0, -1}, {1.618, 0, 1}, {-1.618, 0, -1}, {-1.618, 0, 1}
        };
        d20.faces = new int[][]{
                {0, 11, 5, 1}, {0, 5, 1, 2}, {0, 1, 7, 3}, {0, 7, 10, 4}, {0, 10, 11, 5},
                {1, 5, 9, 6}, {5, 11, 4, 7}, {11, 10, 2, 8}, {10, 7, 6, 9}, {7, 1, 8, 10},
                {3, 9, 4, 11}, {3, 4, 2, 12}, {3, 2, 6, 13}, {3, 6, 8, 14}, {3, 8, 9, 15},
                {4, 9, 5, 16}, {2, 4, 11, 17}, {6, 2, 10, 18}, {8, 6, 7, 19}, {9, 8, 1, 20}
        };
        d20.skipLastFaceIndex = true;
        d20.faceValues = new int[]{10, 17, 3, 16, 8, 7, 12, 20, 6, 19, 5, 18, 4, 11, 13, 15, 2, 14, 9, 1};
        SHAPES.put("d20", d20);
    }

    public static DiceShapeData get(String type) {
        DiceShapeData data = SHAPES.get(type);
        if (data == null) throw new IllegalArgumentException("Unbekannter Würfeltyp: " + type);
        return data;
    }

    private DiceShape() {
    }
}