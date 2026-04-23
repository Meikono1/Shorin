package com.fuchsbau.shorin.Engine.Dice;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class DiceDefinitions {

    public static Map<DiceType, DiceDefinition> diceMap = createAll();

    private DiceDefinitions() {
    }

    public static Map<DiceType, DiceDefinition> createAll() {
        Map<DiceType, DiceDefinition> map = new EnumMap<>(DiceType.class);

        map.put(DiceType.D2, createD2());
        map.put(DiceType.D4, createD4());
        map.put(DiceType.D6, createD6());
        map.put(DiceType.D8, createD8());
        map.put(DiceType.D12, createD12());
        map.put(DiceType.D20, createD20());

        return map;
    }

    private static DiceDefinition createD2() {
        int segments = 20;

        List<Vec3> vertices = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();

        // Mittelpunkt
        vertices.add(new Vec3(0, 0, 0));

        // Randpunkte
        for (int i = 0; i < segments; i++) {
            double angle = (Math.PI * 2.0 * i) / segments;
            double x = Math.cos(angle);
            double y = Math.sin(angle);
            vertices.add(new Vec3(x, y, 0));
        }

        // Triangle-Fan
        for (int i = 1; i <= segments; i++) {
            int next = (i == segments) ? 1 : i + 1;
            faces.add(new int[]{0, i, next});
        }

        int[] faceValues = new int[segments];
        for (int i = 0; i < segments; i++) {
            faceValues[i] = (i < segments / 2) ? 1 : 2;
        }

        return new DiceDefinition(
                DiceType.D2,
                List.copyOf(vertices),
                List.copyOf(faces),
                faceValues,
                List.of(),
                1.0,
                true
        );
    }

    private static DiceDefinition createD4() {
        double k = 1.0 / Math.sqrt(3);

        List<Vec3> vertices = List.of(
                new Vec3( 1,  1,  1),
                new Vec3(-1, -1,  1),
                new Vec3(-1,  1, -1),
                new Vec3( 1, -1, -1)
        );

        List<int[]> faces = List.of(
                new int[]{0, 1, 2},
                new int[]{0, 3, 1},
                new int[]{0, 2, 3},
                new int[]{1, 3, 2}
        );

        return new DiceDefinition(
                DiceType.D4,
                List.of(
                        new Vec3(k, k, k),
                        new Vec3(-k, -k, k),
                        new Vec3(-k, k, -k),
                        new Vec3(k, -k, -k)
                ),
                List.of(
                        new int[]{0, 1, 2},
                        new int[]{0, 3, 1},
                        new int[]{0, 2, 3},
                        new int[]{1, 3, 2}
                ),
                new int[]{1, 2, 3, 4},
                computeFaceNormals(vertices, faces),
                1.0,
                false
        );
    }

    private static DiceDefinition createD6() {
        List<Vec3> vertices = List.of(
                new Vec3(-1, -1, -1), // 0
                new Vec3( 1, -1, -1), // 1
                new Vec3( 1,  1, -1), // 2
                new Vec3(-1,  1, -1), // 3
                new Vec3(-1, -1,  1), // 4
                new Vec3( 1, -1,  1), // 5
                new Vec3( 1,  1,  1), // 6
                new Vec3(-1,  1,  1)  // 7
        );

        List<int[]> faces = List.of(
                new int[]{0, 3, 2, 1}, // back  (-z)
                new int[]{4, 5, 6, 7}, // front (+z)
                new int[]{0, 1, 5, 4}, // bottom(-y)
                new int[]{1, 2, 6, 5}, // right (+x)
                new int[]{2, 3, 7, 6}, // top   (+y)
                new int[]{3, 0, 4, 7}  // left  (-x)
        );

        int[] faceValues = new int[]{1, 6, 2, 3, 4, 5};

        return new DiceDefinition(
                DiceType.D6,
                vertices,
                faces,
                faceValues,
                computeFaceNormals(vertices, faces),
                Math.sqrt(3),
                false
        );
    }

    private static DiceDefinition createD8() {
        List<Vec3> vertices = List.of(
                new Vec3(-1, -1, -1), // 0
                new Vec3( 1, -1, -1), // 1
                new Vec3( 1,  1, -1), // 2
                new Vec3(-1,  1, -1), // 3
                new Vec3(-1, -1,  1), // 4
                new Vec3( 1, -1,  1), // 5
                new Vec3( 1,  1,  1), // 6
                new Vec3(-1,  1,  1)  // 7
        );

        List<int[]> faces = List.of(
                new int[]{0, 1, 2, 3},
                new int[]{4, 7, 6, 5},
                new int[]{0, 4, 5, 1},
                new int[]{1, 5, 6, 2},
                new int[]{2, 6, 7, 3},
                new int[]{3, 7, 4, 0}
        );

        return new DiceDefinition(
                DiceType.D8,
                List.of(
                        new Vec3(1, 0, 0),
                        new Vec3(-1, 0, 0),
                        new Vec3(0, 1, 0),
                        new Vec3(0, -1, 0),
                        new Vec3(0, 0, 1),
                        new Vec3(0, 0, -1)
                ),
                List.of(
                        new int[]{0, 2, 4},
                        new int[]{0, 4, 3},
                        new int[]{0, 3, 5},
                        new int[]{0, 5, 2},
                        new int[]{1, 4, 2},
                        new int[]{1, 3, 4},
                        new int[]{1, 5, 3},
                        new int[]{1, 2, 5}
                ),
                new int[]{1, 2, 3, 4, 5, 6, 7, 8},
                computeFaceNormals(vertices, faces),
                1.0,
                false
        );
    }

    private static DiceDefinition createD12() {
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;

        List<Vec3> icoVerts = List.of(
                new Vec3(-1,  phi, 0),
                new Vec3( 1,  phi, 0),
                new Vec3(-1, -phi, 0),
                new Vec3( 1, -phi, 0),

                new Vec3(0, -1,  phi),
                new Vec3(0,  1,  phi),
                new Vec3(0, -1, -phi),
                new Vec3(0,  1, -phi),

                new Vec3( phi, 0, -1),
                new Vec3( phi, 0,  1),
                new Vec3(-phi, 0, -1),
                new Vec3(-phi, 0,  1)
        );

        List<int[]> icoFaces = List.of(
                new int[]{0,11,5},
                new int[]{0,5,1},
                new int[]{0,1,7},
                new int[]{0,7,10},
                new int[]{0,10,11},

                new int[]{1,5,9},
                new int[]{5,11,4},
                new int[]{11,10,2},
                new int[]{10,7,6},
                new int[]{7,1,8},

                new int[]{3,9,4},
                new int[]{3,4,2},
                new int[]{3,2,6},
                new int[]{3,6,8},
                new int[]{3,8,9},

                new int[]{4,9,5},
                new int[]{2,4,11},
                new int[]{6,2,10},
                new int[]{8,6,7},
                new int[]{9,8,1}
        );

        List<int[]> faces = List.of(
                new int[]{0, 1, 2, 3, 4},
                new int[]{0, 4, 7, 6, 5},
                new int[]{0, 5, 15, 10, 1},
                new int[]{1, 10, 14, 9, 2},
                new int[]{2, 9, 8, 3, 1},
                new int[]{3, 8, 18, 17, 4},
                new int[]{4, 17, 16, 7, 0},
                new int[]{5, 6, 11, 15, 0},
                new int[]{6, 7, 16, 12, 11},
                new int[]{8, 9, 14, 13, 18},
                new int[]{10, 15, 11, 12, 14},
                new int[]{12, 16, 17, 18, 13}
        );

        List<Vec3> vertices = new ArrayList<>();
        for (int[] face : icoFaces) {
            Vec3 a = icoVerts.get(face[0]);
            Vec3 b = icoVerts.get(face[1]);
            Vec3 c = icoVerts.get(face[2]);

            Vec3 center = new Vec3(
                    (a.x + b.x + c.x) / 3.0,
                    (a.y + b.y + c.y) / 3.0,
                    (a.z + b.z + c.z) / 3.0
            );

            vertices.add(center);
        }

        List<Vec3> dodecaVerts = new ArrayList<>();
        for (int[] face : icoFaces) {
            Vec3 a = icoVerts.get(face[0]);
            Vec3 b = icoVerts.get(face[1]);
            Vec3 c = icoVerts.get(face[2]);

            Vec3 center = new Vec3(
                    (a.x + b.x + c.x) / 3.0,
                    (a.y + b.y + c.y) / 3.0,
                    (a.z + b.z + c.z) / 3.0
            );

            dodecaVerts.add(center);
        }

        List<List<Integer>> facesPerIcoVertex = new ArrayList<>();
        for (int i = 0; i < icoVerts.size(); i++) {
            facesPerIcoVertex.add(new ArrayList<>());
        }

        for (int fi = 0; fi < icoFaces.size(); fi++) {
            int[] face = icoFaces.get(fi);
            facesPerIcoVertex.get(face[0]).add(fi);
            facesPerIcoVertex.get(face[1]).add(fi);
            facesPerIcoVertex.get(face[2]).add(fi);
        }

        List<int[]> dodecaFaces = new ArrayList<>();
        for (int vi = 0; vi < icoVerts.size(); vi++) {
            Vec3 axis = icoVerts.get(vi);
            Vec3 ref = perpendicular(axis);
            Vec3 ref2 = axis.cross(ref);

            List<Integer> around = facesPerIcoVertex.get(vi);
            around.sort((a, b) -> {
                double aa = angleOnPlane(dodecaVerts.get(a), axis, ref, ref2);
                double ab = angleOnPlane(dodecaVerts.get(b), axis, ref, ref2);
                return Double.compare(aa, ab);
            });

            dodecaFaces.add(around.stream().mapToInt(Integer::intValue).toArray());
        }

        return new DiceDefinition(
                DiceType.D12,
                List.copyOf(dodecaVerts),
                List.copyOf(dodecaFaces),
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12},
                computeFaceNormals(vertices, faces),
                1.0,
                false
        );
    }

    private static DiceDefinition createD20() {
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        double n = 1.0 / Math.sqrt(1 + phi * phi);

        List<Vec3> vertices = List.of(
                new Vec3(-1,  phi, 0).scale(n),
                new Vec3( 1,  phi, 0).scale(n),
                new Vec3(-1, -phi, 0).scale(n),
                new Vec3( 1, -phi, 0).scale(n),

                new Vec3(0, -1,  phi).scale(n),
                new Vec3(0,  1,  phi).scale(n),
                new Vec3(0, -1, -phi).scale(n),
                new Vec3(0,  1, -phi).scale(n),

                new Vec3( phi, 0, -1).scale(n),
                new Vec3( phi, 0,  1).scale(n),
                new Vec3(-phi, 0, -1).scale(n),
                new Vec3(-phi, 0,  1).scale(n)
        );

        List<int[]> faces = List.of(
                new int[]{0,11,5},
                new int[]{0,5,1},
                new int[]{0,1,7},
                new int[]{0,7,10},
                new int[]{0,10,11},

                new int[]{1,5,9},
                new int[]{5,11,4},
                new int[]{11,10,2},
                new int[]{10,7,6},
                new int[]{7,1,8},

                new int[]{3,9,4},
                new int[]{3,4,2},
                new int[]{3,2,6},
                new int[]{3,6,8},
                new int[]{3,8,9},

                new int[]{4,9,5},
                new int[]{2,4,11},
                new int[]{6,2,10},
                new int[]{8,6,7},
                new int[]{9,8,1}
        );

        return new DiceDefinition(
                DiceType.D20,
                List.of(
                        new Vec3(-1, phi, 0).scale(n),
                        new Vec3(1, phi, 0).scale(n),
                        new Vec3(-1, -phi, 0).scale(n),
                        new Vec3(1, -phi, 0).scale(n),
                        new Vec3(0, -1, phi).scale(n),
                        new Vec3(0, 1, phi).scale(n),
                        new Vec3(0, -1, -phi).scale(n),
                        new Vec3(0, 1, -phi).scale(n),
                        new Vec3(phi, 0, -1).scale(n),
                        new Vec3(phi, 0, 1).scale(n),
                        new Vec3(-phi, 0, -1).scale(n),
                        new Vec3(-phi, 0, 1).scale(n)
                ),
                List.of(
                        new int[]{0, 11, 5}, new int[]{0, 5, 1}, new int[]{0, 1, 7}, new int[]{0, 7, 10}, new int[]{0, 10, 11},
                        new int[]{1, 5, 9}, new int[]{5, 11, 4}, new int[]{11, 10, 2}, new int[]{10, 7, 6}, new int[]{7, 1, 8},
                        new int[]{3, 9, 4}, new int[]{3, 4, 2}, new int[]{3, 2, 6}, new int[]{3, 6, 8}, new int[]{3, 8, 9},
                        new int[]{4, 9, 5}, new int[]{2, 4, 11}, new int[]{6, 2, 10}, new int[]{8, 6, 7}, new int[]{9, 8, 1}
                ),
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20},
                computeFaceNormals(vertices, faces),
                1.0,
                false
        );
    }

    private static double angleOnPlane(Vec3 p, Vec3 axis, Vec3 ref, Vec3 ref2) {
        Vec3 proj = p.sub(axis.scale(p.dot(axis)));
        return Math.atan2(proj.dot(ref2), proj.dot(ref));
    }

    private static Vec3 perpendicular(Vec3 v) {
        if (Math.abs(v.x) < 0.9) {
            return new Vec3(1, 0, 0).cross(v);
        }
        return new Vec3(0, 1, 0).cross(v);
    }

    private static List<Vec3> computeFaceNormals(List<Vec3> vertices, List<int[]> faces) {
        List<Vec3> normals = new ArrayList<>(faces.size());

        for (int[] face : faces) {
            if (face.length < 3) {
                normals.add(new Vec3(0, 0, 1));
                continue;
            }

            Vec3 a = vertices.get(face[0]);
            Vec3 b = vertices.get(face[1]);
            Vec3 c = vertices.get(face[2]);

            Vec3 ab = b.sub(a);
            Vec3 ac = c.sub(a);

            normals.add(ab.cross(ac));
        }

        return List.copyOf(normals);
    }
}