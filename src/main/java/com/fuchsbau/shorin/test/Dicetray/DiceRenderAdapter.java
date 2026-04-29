package com.fuchsbau.shorin.test.Dicetray;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;
import com.fuchsbau.shorin.Engine.Images.ImagePreLoader;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Shape.ShapeType;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape.DiceShapeData;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

import java.util.*;
import java.util.logging.Logger;

public class DiceRenderAdapter {

    private static final Logger logger = FileLogger.getLogger();

    private final Group world;
    private final Map<Integer, Node> dieViews = new HashMap<>();
    private final Map<String, TriangleMesh> meshCache = new HashMap<>();
    private final float targetRadius;

    Image diceTexture = ImagePreLoader.getCached(ImagePaths.SHORIN_CLEAN_MAP);

    private final PhongMaterial normalMat = new PhongMaterial();
    private final PhongMaterial hoverMat = new PhongMaterial();

    public DiceRenderAdapter(Group world, float targetRadius) {
        this.world = world;
        this.targetRadius = targetRadius;

        normalMat.setDiffuseColor(Color.WHITE);
        normalMat.setDiffuseMap(diceTexture);
        normalMat.setSpecularColor(Color.WHITE);
        normalMat.setSpecularPower(16);

        hoverMat.setDiffuseColor(Color.ORANGE);
        hoverMat.setDiffuseMap(diceTexture);
        hoverMat.setSpecularColor(Color.WHITE);
        hoverMat.setSpecularPower(24);
    }

    public void sync(Collection<PhysicsBody> bodies) {
        Set<Integer> aliveIds = new HashSet<>();

        for (PhysicsBody body : bodies) {
            aliveIds.add(body.id);

            Node view = dieViews.get(body.id);
            if (view == null) {
                view = createViewFor(body);
                dieViews.put(body.id, view);
                world.getChildren().add(view);
            }

            updateViewFromBody(view, body);
        }

        dieViews.entrySet().removeIf(entry -> {
            boolean remove = !aliveIds.contains(entry.getKey());
            if (remove) {
                world.getChildren().remove(entry.getValue());
            }
            return remove;
        });
    }

    public void clear() {
        for (Node view : dieViews.values()) {
            world.getChildren().remove(view);
        }
        dieViews.clear();
    }

    private Node createViewFor(PhysicsBody body) {
        String diceType = resolveDiceType(body);
        DiceShapeData data = DiceShape.get(diceType);

        // Cylinder-Pfad (D2 / Coin)
        if (data.type == ShapeType.CYLINDER) {
            Node view = createCylinderView(data);
            logger.fine("[Render] Cylinder-View erstellt id=" + body.id + " type=" + diceType);
            return view;
        }

        // Polyhedron-Pfad (D4, D6, D8, D10, D12, D20) – gecacht
        TriangleMesh mesh = meshCache.computeIfAbsent(diceType, key -> {
            DiceShapeData d = DiceShape.get(key);
            float scale = computeNormalizedScale(d, targetRadius);
            TriangleMesh m = buildConvexPolyhedronMesh(d, scale);
            logger.fine("[Render] Mesh gebaut für " + key + " (scale=" + scale + ")");
            return m;
        });

        MeshView view = new MeshView(mesh);
        view.setMaterial(normalMat);
        view.setOnMouseEntered(e -> view.setMaterial(hoverMat));
        view.setOnMouseExited(e -> view.setMaterial(normalMat));

        logger.fine("[Render] Mesh-View erstellt id=" + body.id + " type=" + diceType);
        return view;
    }

    /**
     * Baut einen JavaFX-Cylinder passend zur Shape-Definition.
     * radiusTop/radiusBottom sind relative Faktoren – wir multiplizieren mit targetRadius.
     * JavaFX kennt nur einheitlichen Radius, also nehmen wir den Mittelwert.
     */
    private Cylinder createCylinderView(DiceShapeData data) {
        double r = targetRadius * (data.radiusTop + data.radiusBottom) * 0.5;
        double h = targetRadius * data.height * 2.0;  // *2 weil height in DiceShape "halbe Höhe" meint

        Cylinder c = new Cylinder(r, h, Math.max(8, data.numSegments));
        c.setMaterial(normalMat);

        c.setOnMouseEntered(e -> c.setMaterial(hoverMat));
        c.setOnMouseExited(e -> c.setMaterial(normalMat));
        return c;
    }

    /**
     * Liest den Würfeltyp vom Body. Fallback auf d6 falls nichts gesetzt oder
     * unbekannt – damit ein falsch konfigurierter Body nicht den ganzen Frame killt.
     */
    private String resolveDiceType(PhysicsBody body) {
        String t = body.diceShape;
        if (t == null || t.isEmpty()) {
            logger.warning("[Render] body id=" + body.id + " hat keinen diceShape – fallback d6");
            return "d6";
        }
        try {
            DiceShape.get(t);
            return t;
        } catch (IllegalArgumentException ex) {
            logger.warning("[Render] unbekannter diceShape '" + t + "' – fallback d6");
            return "d6";
        }
    }

    private void updateViewFromBody(Node view, PhysicsBody body) {
        view.setTranslateX(body.position.x);
        view.setTranslateY(-body.position.y);
        view.setTranslateZ(body.position.z);

        // Quaternion-Achsen müssen dem Positions-Mapping folgen:
        // Flip bei Y bedeutet, die Quaternion-Y-Komponente dreht sich mit.
        applyQuaternion(view,
                body.quaternion.x,
                -body.quaternion.y,
                body.quaternion.z,
                body.quaternion.w);
    }

    private void applyQuaternion(Node view, double qx, double qy, double qz, double qw) {
        // unverändert – nur der Parametertyp oben wechselt
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
        if (data.vertices == null) {
            logger.warning("[Render] computeNormalizedScale auf Non-Polyhedron – return targetRadius");
            return targetRadius;
        }

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

        mesh.getTexCoords().addAll(
                0, 0,
                1, 0,
                1, 1,
                0, 1
        );

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
                        v1, 1,
                        v2, 2
                );
            }
        }

        return mesh;
    }

    public void setHover(int dieId, boolean hover) {
        Node view = dieViews.get(dieId);
        switch (view) {
            case null -> {
                return;
            }
            case MeshView mv -> mv.setMaterial(hover ? hoverMat : normalMat);
            case Cylinder cy -> cy.setMaterial(hover ? hoverMat : normalMat);
            default -> {
            }
        }

    }

    private static final class RotateAxes {
        private static final Point3D Y_AXIS = new Point3D(0, 1, 0);
    }
}