package com.fuchsbau.shorin.Engine.Dice;

import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class DieMeshFactory {

    private final static Logger logger = FileLogger.getLogger();

    private DieMeshFactory() {
    }

    public static Node createDie(DiceType type, double size) {
        return switch (type) {
            case D2 -> createCoin(size);
            case D4 -> createMeshView(buildTetrahedron(size), Color.LIGHTGRAY);
            case D6 -> createMeshView(buildCube(size), Color.LIGHTGRAY);
            case D8 -> createMeshView(buildOctahedron(size), Color.LIGHTGRAY);
            case D12 -> createMeshView(buildDodecahedron(size), Color.LIGHTGRAY);
            case D20 -> createMeshView(buildIcosahedron(size), Color.LIGHTGRAY);
        };
    }

    private static Node createCoin(double size) {
        Cylinder coin = new Cylinder(size * 0.7, size * 0.18, 64);
        coin.setMaterial(new PhongMaterial(Color.GOLD));
        coin.setRotationAxis(Rotate.X_AXIS);
        coin.setRotate(90);
        return coin;
    }

    private static MeshGroup createMeshView(Polyhedron poly, Color color) {
        TriangleMesh mesh = new TriangleMesh();

        for (Point3 p : poly.vertices) {
            mesh.getPoints().addAll((float) p.x, (float) p.y, (float) p.z);
        }

        mesh.getTexCoords().addAll(0, 0);

        for (int[] face : poly.faces) {
            if (face.length < 3) continue;

            int a = face[0];
            for (int i = 1; i < face.length - 1; i++) {
                int b = face[i];
                int c = face[i + 1];

                mesh.getFaces().addAll(
                        a, 0,
                        b, 0,
                        c, 0
                );
            }
        }

        MeshView fill = new MeshView(mesh);
        fill.setMaterial(new PhongMaterial(color));
        fill.setCullFace(CullFace.BACK);
        fill.setDrawMode(DrawMode.FILL);

        MeshView wire = new MeshView(mesh);
        wire.setMaterial(new PhongMaterial(Color.BLACK));
        wire.setCullFace(CullFace.NONE);
        wire.setDrawMode(DrawMode.LINE);

        return new MeshGroup(fill, wire);
    }

    private static Polyhedron buildTetrahedron(double s) {
        double k = s / Math.sqrt(3);

        List<Point3> v = List.of(
                new Point3(k, k, k),
                new Point3(-k, -k, k),
                new Point3(-k, k, -k),
                new Point3(k, -k, -k)
        );

        List<int[]> f = List.of(
                new int[]{0, 1, 2},
                new int[]{0, 3, 1},
                new int[]{0, 2, 3},
                new int[]{1, 3, 2}
        );

        return new Polyhedron(v, f);
    }

    private static Polyhedron buildCube(double s) {
        double h = s / 2.0;

        List<Point3> v = List.of(
                new Point3(-h, -h, -h), // 0
                new Point3(h, -h, -h), // 1
                new Point3(h, h, -h), // 2
                new Point3(-h, h, -h), // 3
                new Point3(-h, -h, h), // 4
                new Point3(h, -h, h), // 5
                new Point3(h, h, h), // 6
                new Point3(-h, h, h)  // 7
        );

        List<int[]> f = List.of(
                new int[]{0, 1, 2, 3},
                new int[]{4, 7, 6, 5},
                new int[]{0, 4, 5, 1},
                new int[]{1, 5, 6, 2},
                new int[]{2, 6, 7, 3},
                new int[]{3, 7, 4, 0}
        );

        return new Polyhedron(v, f);
    }

    private static Polyhedron buildOctahedron(double s) {
        double h = s;

        List<Point3> v = List.of(
                new Point3(h, 0, 0),
                new Point3(-h, 0, 0),
                new Point3(0, h, 0),
                new Point3(0, -h, 0),
                new Point3(0, 0, h),
                new Point3(0, 0, -h)
        );

        List<int[]> f = List.of(
                new int[]{0, 2, 4},
                new int[]{0, 4, 3},
                new int[]{0, 3, 5},
                new int[]{0, 5, 2},
                new int[]{1, 4, 2},
                new int[]{1, 3, 4},
                new int[]{1, 5, 3},
                new int[]{1, 2, 5}
        );

        return new Polyhedron(v, f);
    }

    private static Polyhedron buildIcosahedron(double s) {
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        double n = s / Math.sqrt(1 + phi * phi);

        List<Point3> v = List.of(
                new Point3(-1, phi, 0).scale(n),
                new Point3(1, phi, 0).scale(n),
                new Point3(-1, -phi, 0).scale(n),
                new Point3(1, -phi, 0).scale(n),

                new Point3(0, -1, phi).scale(n),
                new Point3(0, 1, phi).scale(n),
                new Point3(0, -1, -phi).scale(n),
                new Point3(0, 1, -phi).scale(n),

                new Point3(phi, 0, -1).scale(n),
                new Point3(phi, 0, 1).scale(n),
                new Point3(-phi, 0, -1).scale(n),
                new Point3(-phi, 0, 1).scale(n)
        );

        List<int[]> f = List.of(
                new int[]{0, 11, 5},
                new int[]{0, 5, 1},
                new int[]{0, 1, 7},
                new int[]{0, 7, 10},
                new int[]{0, 10, 11},

                new int[]{1, 5, 9},
                new int[]{5, 11, 4},
                new int[]{11, 10, 2},
                new int[]{10, 7, 6},
                new int[]{7, 1, 8},

                new int[]{3, 9, 4},
                new int[]{3, 4, 2},
                new int[]{3, 2, 6},
                new int[]{3, 6, 8},
                new int[]{3, 8, 9},

                new int[]{4, 9, 5},
                new int[]{2, 4, 11},
                new int[]{6, 2, 10},
                new int[]{8, 6, 7},
                new int[]{9, 8, 1}
        );

        return new Polyhedron(v, f);
    }

    private static Polyhedron buildDodecahedron(double s) {
        Polyhedron ico = buildIcosahedron(s);
        List<Point3> icoVerts = ico.vertices;
        List<int[]> icoFaces = ico.faces;

        List<Point3> dodecaVerts = new ArrayList<>();
        for (int[] face : icoFaces) {
            Point3 a = icoVerts.get(face[0]);
            Point3 b = icoVerts.get(face[1]);
            Point3 c = icoVerts.get(face[2]);
            Point3 center = new Point3(
                    (a.x + b.x + c.x) / 3.0,
                    (a.y + b.y + c.y) / 3.0,
                    (a.z + b.z + c.z) / 3.0
            ).normalize().scale(s);
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
            Point3 axis = icoVerts.get(vi).normalize();
            Point3 ref = perpendicular(axis).normalize();
            Point3 ref2 = axis.cross(ref).normalize();

            List<Integer> around = facesPerIcoVertex.get(vi);
            around.sort((a, b) -> {
                Point3 pa = dodecaVerts.get(a);
                Point3 pb = dodecaVerts.get(b);

                double aa = angleOnPlane(pa, axis, ref, ref2);
                double ab = angleOnPlane(pb, axis, ref, ref2);
                return Double.compare(aa, ab);
            });

            dodecaFaces.add(around.stream().mapToInt(Integer::intValue).toArray());
        }

        return new Polyhedron(dodecaVerts, dodecaFaces);
    }

    private static double angleOnPlane(Point3 p, Point3 axis, Point3 ref, Point3 ref2) {
        Point3 proj = p.subtract(axis.scale(p.dot(axis))).normalize();
        double x = proj.dot(ref);
        double y = proj.dot(ref2);
        return Math.atan2(y, x);
    }

    private static Point3 perpendicular(Point3 v) {
        if (Math.abs(v.x) < 0.9) {
            return new Point3(1, 0, 0).cross(v);
        }
        return new Point3(0, 1, 0).cross(v);
    }

    private static final class MeshGroup extends Group {
        MeshGroup(Node... children) {
            super(children);
        }
    }

    private static final class Polyhedron {
        final List<Point3> vertices;
        final List<int[]> faces;

        Polyhedron(List<Point3> vertices, List<int[]> faces) {
            this.vertices = vertices;
            this.faces = faces;
        }
    }

    private static final class Point3 {
        final double x;
        final double y;
        final double z;

        Point3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        Point3 scale(double s) {
            return new Point3(x * s, y * s, z * s);
        }

        Point3 subtract(Point3 o) {
            return new Point3(x - o.x, y - o.y, z - o.z);
        }

        double dot(Point3 o) {
            return x * o.x + y * o.y + z * o.z;
        }

        Point3 cross(Point3 o) {
            return new Point3(
                    y * o.z - z * o.y,
                    z * o.x - x * o.z,
                    x * o.y - y * o.x
            );
        }

        double length() {
            return Math.sqrt(x * x + y * y + z * z);
        }

        Point3 normalize() {
            double len = length();
            if (len == 0) return new Point3(0, 0, 0);
            return new Point3(x / len, y / len, z / len);
        }
    }
}