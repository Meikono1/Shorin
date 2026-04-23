package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

import java.util.List;

public class Box extends CollisionShape {

    private static final Vec3 worldCornerTempPos = new Vec3();
    private static final Vec3[] worldCornersTemp = {
            new Vec3(), new Vec3(), new Vec3(), new Vec3(),
            new Vec3(), new Vec3(), new Vec3(), new Vec3()
    };

    public Vec3 halfExtents;
    public ConvexPolyhedron convexPolyhedronRepresentation;

    public Box(Vec3 halfExtents) {
        super(new CollisionShapeOptions(1, -1, true, ShapeType.BOX));
        this.halfExtents = halfExtents;
        this.convexPolyhedronRepresentation = null;
        updateConvexPolyhedronRepresentation();
        updateBoundingSphereRadius();
    }

    public void updateConvexPolyhedronRepresentation() {
        double sx = halfExtents.x;
        double sy = halfExtents.y;
        double sz = halfExtents.z;

        List<Vec3> vertices = List.of(
                new Vec3(-sx, -sy, -sz),
                new Vec3(sx, -sy, -sz),
                new Vec3(sx, sy, -sz),
                new Vec3(-sx, sy, -sz),
                new Vec3(-sx, -sy, sz),
                new Vec3(sx, -sy, sz),
                new Vec3(sx, sy, sz),
                new Vec3(-sx, sy, sz)
        );

        List<int[]> faces = List.of(
                new int[]{3, 2, 1, 0}, // -z
                new int[]{4, 5, 6, 7}, // +z
                new int[]{5, 4, 0, 1}, // -y
                new int[]{2, 3, 7, 6}, // +y
                new int[]{0, 4, 7, 3}, // -x
                new int[]{1, 2, 6, 5}  // +x
        );

        List<Vec3> axes = List.of(
                new Vec3(0, 0, 1),
                new Vec3(0, 1, 0),
                new Vec3(1, 0, 0)
        );


        convexPolyhedronRepresentation = new ConvexPolyhedron(vertices, faces, null, axes, null);
        convexPolyhedronRepresentation.material = this.material;
    }

    public void calculateLocalInertia(double mass, Vec3 target) {
        if (target == null) target = new Vec3();
        Box.calculateInertia(halfExtents, mass, target);
    }

    public static void calculateInertia(Vec3 halfExtents, double mass, Vec3 target) {
        double ex = halfExtents.x;
        double ey = halfExtents.y;
        double ez = halfExtents.z;

        target.x = (1.0 / 12.0) * mass * (2 * ey * 2 * ey + 2 * ez * 2 * ez);
        target.y = (1.0 / 12.0) * mass * (2 * ex * 2 * ex + 2 * ez * 2 * ez);
        target.z = (1.0 / 12.0) * mass * (2 * ey * 2 * ey + 2 * ex * 2 * ex);
    }

    public List<Vec3> getSideNormals(List<Vec3> sixTargetVectors, Quaternion quat) {
        double ex = halfExtents.x;
        double ey = halfExtents.y;
        double ez = halfExtents.z;

        sixTargetVectors.get(0).set(ex, 0, 0);
        sixTargetVectors.get(1).set(0, ey, 0);
        sixTargetVectors.get(2).set(0, 0, ez);
        sixTargetVectors.get(3).set(-ex, 0, 0);
        sixTargetVectors.get(4).set(0, -ey, 0);
        sixTargetVectors.get(5).set(0, 0, -ez);

        if (quat != null) {
            for (Vec3 side : sixTargetVectors) {
                quat.vmult(side, side);
            }
        }

        return sixTargetVectors;
    }

    public double volume() {
        return 8.0 * halfExtents.x * halfExtents.y * halfExtents.z;
    }

    public void updateBoundingSphereRadius() {
        boundingSphereRadius = halfExtents.length();
    }

    public void forEachWorldCorner(Vec3 pos, Quaternion quat, WorldCornerCallback callback) {
        double ex = halfExtents.x;
        double ey = halfExtents.y;
        double ez = halfExtents.z;

        double[][] corners = {
                {ex, ey, ez},
                {-ex, ey, ez},
                {-ex, -ey, ez},
                {-ex, -ey, -ez},
                {ex, -ey, -ez},
                {ex, ey, -ez},
                {-ex, ey, -ez},
                {ex, -ey, ez}
        };

        for (double[] corner : corners) {
            worldCornerTempPos.set(corner[0], corner[1], corner[2]);
            quat.vmult(worldCornerTempPos, worldCornerTempPos);
            pos.add(worldCornerTempPos, worldCornerTempPos);
            callback.accept(worldCornerTempPos.x, worldCornerTempPos.y, worldCornerTempPos.z);
        }
    }

    public void calculateWorldAABB(Vec3 pos, Quaternion quat, Vec3 min, Vec3 max) {
        double ex = halfExtents.x;
        double ey = halfExtents.y;
        double ez = halfExtents.z;

        worldCornersTemp[0].set(ex, ey, ez);
        worldCornersTemp[1].set(-ex, ey, ez);
        worldCornersTemp[2].set(-ex, -ey, ez);
        worldCornersTemp[3].set(-ex, -ey, -ez);
        worldCornersTemp[4].set(ex, -ey, -ez);
        worldCornersTemp[5].set(ex, ey, -ez);
        worldCornersTemp[6].set(-ex, ey, -ez);
        worldCornersTemp[7].set(ex, -ey, ez);

        Vec3 wc = worldCornersTemp[0];
        quat.vmult(wc, wc);
        pos.add(wc, wc);
        max.copy(wc);
        min.copy(wc);

        for (int i = 1; i < 8; i++) {
            wc = worldCornersTemp[i];
            quat.vmult(wc, wc);
            pos.add(wc, wc);

            if (wc.x > max.x) max.x = wc.x;
            if (wc.y > max.y) max.y = wc.y;
            if (wc.z > max.z) max.z = wc.z;
            if (wc.x < min.x) min.x = wc.x;
            if (wc.y < min.y) min.y = wc.y;
            if (wc.z < min.z) min.z = wc.z;
        }
    }

    @FunctionalInterface
    public interface WorldCornerCallback {
        void accept(double x, double y, double z);
    }
}