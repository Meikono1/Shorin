package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

import java.util.ArrayList;
import java.util.List;

public class Cylinder extends ConvexPolyhedron {

    public double radiusTop;
    public double radiusBottom;
    public double height;
    public int numSegments;

    public Cylinder(double radiusTop, double radiusBottom, double height, int numSegments) {
        super(null, null, null, null, null);

        if (radiusTop < 0) throw new IllegalArgumentException("radiusTop darf nicht negativ sein.");
        if (radiusBottom < 0) throw new IllegalArgumentException("radiusBottom darf nicht negativ sein.");

        int N = numSegments;

        List<Vec3> vertices = new ArrayList<>();
        List<Vec3> axes = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        List<Integer> bottomface = new ArrayList<>();
        List<Integer> topface = new ArrayList<>();

        // Erster unterer Punkt
        vertices.add(new Vec3(-radiusBottom * Math.sin(0), -height * 0.5, radiusBottom * Math.cos(0)));
        bottomface.add(0);

        // Erster oberer Punkt
        vertices.add(new Vec3(-radiusTop * Math.sin(0), height * 0.5, radiusTop * Math.cos(0)));
        topface.add(1);

        for (int i = 0; i < N; i++) {
            double theta = ((2 * Math.PI) / N) * (i + 1);
            double thetaN = ((2 * Math.PI) / N) * (i + 0.5);

            if (i < N - 1) {
                vertices.add(new Vec3(-radiusBottom * Math.sin(theta), -height * 0.5, radiusBottom * Math.cos(theta)));
                bottomface.add(2 * i + 2);
                vertices.add(new Vec3(-radiusTop * Math.sin(theta), height * 0.5, radiusTop * Math.cos(theta)));
                topface.add(2 * i + 3);
                faces.add(new int[]{2 * i, 2 * i + 1, 2 * i + 3, 2 * i + 2});
            } else {
                faces.add(new int[]{2 * i, 2 * i + 1, 1, 0});
            }

            if (N % 2 == 1 || i < N / 2) {
                axes.add(new Vec3(-Math.sin(thetaN), 0, Math.cos(thetaN)));
            }
        }

        faces.add(bottomface.stream().mapToInt(Integer::intValue).toArray());
        axes.add(new Vec3(0, 1, 0));

        // Top face umkehren
        int[] temp = new int[topface.size()];
        for (int i = 0; i < topface.size(); i++) {
            temp[i] = topface.get(topface.size() - i - 1);
        }
        faces.add(temp);

        // Felder setzen
        this.vertices = vertices;
        this.faces = faces;
        this.uniqueAxes = axes;
        this.radiusTop = radiusTop;
        this.radiusBottom = radiusBottom;
        this.height = height;
        this.numSegments = numSegments;

        if (this.faceNormals.isEmpty()) computeNormals();
        updateBoundingSphereRadius();
        computeEdges();
    }

    public Cylinder(double radiusTop, double radiusBottom, double height) {
        this(radiusTop, radiusBottom, height, 8);
    }
}