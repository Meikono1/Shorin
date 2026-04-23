package com.fuchsbau.shorin.test.Dicetray;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape.DiceShapeData;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DiceRenderAdapter {

    private final Group world;
    private final Map<Integer, MeshView> dieViews = new HashMap<>();
    private final float targetRadius;

    private final PhongMaterial normalMat = new PhongMaterial(Color.LIGHTGRAY);

    public DiceRenderAdapter(Group world, float targetRadius) {
        this.world = world;
        this.targetRadius = targetRadius;
    }

    public void sync(Collection<PhysicsBody> bodies) {
        Set<Integer> aliveIds = new HashSet<>();

        for (PhysicsBody body : bodies) {
            aliveIds.add(body.id);

            MeshView view = dieViews.get(body.id);
            if (view == null) {
                view = createViewFor(body);
                dieViews.put(body.id, view);
                world.getChildren().add(view);
            }

            updateViewFromBody(view, body);
        }

        // entfernte Würfel aus der Szene löschen
        dieViews.entrySet().removeIf(entry -> {
            boolean remove = !aliveIds.contains(entry.getKey());
            if (remove) {
                world.getChildren().remove(entry.getValue());
            }
            return remove;
        });
    }

    public void clear() {
        for (MeshView view : dieViews.values()) {
            world.getChildren().remove(view);
        }
        dieViews.clear();
    }

    private MeshView createViewFor(PhysicsBody body) {
        //String diceType = body.shape != null && body.shape.shapeId != null
        //        ? body.shape.shapeId
        //        : "d6";
        String diceType = "d6";


        DiceShapeData data = DiceShape.get(diceType);
        float scale = computeNormalizedScale(data, targetRadius);

        TriangleMesh mesh = buildConvexPolyhedronMesh(data, scale);

        MeshView view = new MeshView(mesh);
        view.setMaterial(normalMat);
        view.setCullFace(CullFace.BACK);
        view.setDrawMode(DrawMode.FILL);

        return view;
    }

    private void updateViewFromBody(MeshView view, PhysicsBody body) {
        // Simulation:
        // x = rechts/links
        // y = vor/zur Seite im Tray
        // z = Höhe
        //
        // JavaFX:
        // x = rechts
        // y = runter
        // z = Tiefe
        //
        // Für Draufsicht ist diese Zuordnung meist passend:
        view.setTranslateX(body.position.x);
        view.setTranslateY(-body.position.z);
        view.setTranslateZ(body.position.y);

        applyQuaternion(view, body.quaternion.x, body.quaternion.y, body.quaternion.z, body.quaternion.w);
    }

    private void applyQuaternion(MeshView view, double qx, double qy, double qz, double qw) {
        double len = Math.sqrt(qx * qx + qy * qy + qz * qz + qw * qw);
        if (len == 0) {
            view.setRotationAxis(RotateAxes.Y_AXIS);
            view.setRotate(0);
            return;
        }

        qx /= len;
        qy /= len;
        qz /= len;
        qw /= len;

        double angle = 2.0 * Math.acos(qw);
        double s = Math.sqrt(Math.max(0.0, 1.0 - qw * qw));

        double ax, ay, az;
        if (s < 0.0001) {
            ax = 1.0;
            ay = 0.0;
            az = 0.0;
        } else {
            ax = qx / s;
            ay = qy / s;
            az = qz / s;
        }

        view.setRotationAxis(new Point3D(ax, ay, az));
        view.setRotate(Math.toDegrees(angle));
    }

    private float computeNormalizedScale(DiceShapeData data, float targetRadius) {
        double maxDist = 0.0;

        for (double[] v : data.vertices) {
            double d = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
            if (d > maxDist) {
                maxDist = d;
            }
        }

        if (maxDist <= 0.0) {
            return targetRadius;
        }
        return (float) (targetRadius / maxDist);
    }

    private TriangleMesh buildConvexPolyhedronMesh(DiceShapeData data, float scale) {
        TriangleMesh mesh = new TriangleMesh();

        for (double[] v : data.vertices) {
            mesh.getPoints().addAll(
                    (float) v[0] * scale,
                    (float) v[1] * scale,
                    (float) v[2] * scale
            );
        }

        mesh.getTexCoords().addAll(0, 0);

        for (int[] face : data.faces) {
            int usableLength = face.length;
            if (data.skipLastFaceIndex) {
                usableLength -= 1;
            }

            if (usableLength < 3) {
                continue;
            }

            int v0 = face[0];
            for (int i = 1; i < usableLength - 1; i++) {
                int v1 = face[i];
                int v2 = face[i + 1];

                mesh.getFaces().addAll(
                        v0, 0,
                        v1, 0,
                        v2, 0
                );
            }
        }

        return mesh;
    }

    private static final class RotateAxes {
        private static final Point3D Y_AXIS = new Point3D(0, 1, 0);
    }
}