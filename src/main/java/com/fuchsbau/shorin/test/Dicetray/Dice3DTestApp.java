package com.fuchsbau.shorin.test.Dicetray;

import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.PickResult;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;


public class Dice3DTestApp extends Application {
    private static final String[] DICE_TYPES = {"d4", "d6", "d8", "d10", "d12", "d20"};

    static final int[] lastFace = {-1};

    @Override
    public void start(Stage stage) {
        AnchorPane root = new AnchorPane();

        DiceTrayView diceTray = new DiceTrayView();
        diceTray.attachBottomRight(root, 0.20, 220, 150, 420, 300);
        diceTray.setDiceTypes("d4", "d6", "d8", "d10", "d12", "d20");
        diceTray.setSwitchIntervalSeconds(2.0);
        diceTray.setAutoCycleEnabled(true);
        diceTray.start();

        Scene scene = new Scene(root, 900, 700, true);
        stage.setTitle("JavaFX 3D Dice Test");
        stage.setScene(scene);
        stage.show();
    }

    public static StackPane createDiceTestPane() {
        StackPane host = new StackPane();
        host.setStyle("-fx-background-color: linear-gradient(to bottom, #20242b, #11151b);");

        Group world = new Group();


        MeshView dieView = new MeshView();
        dieView.setCullFace(CullFace.BACK);
        dieView.setDrawMode(DrawMode.FILL);

        PhongMaterial normalMat = new PhongMaterial(Color.LIGHTGRAY);
        PhongMaterial hoverMat = new PhongMaterial(Color.ORANGE);
        dieView.setMaterial(normalMat);


        Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

        MeshView wireView = new MeshView();
        wireView.setDrawMode(DrawMode.LINE);
        wireView.setCullFace(CullFace.NONE);
        wireView.setMaterial(new PhongMaterial(Color.BLACK));
        wireView.setScaleX(1.01);
        wireView.setScaleY(1.01);
        wireView.setScaleZ(1.01);
        wireView.setMouseTransparent(true);


        dieView.setTranslateX(30);
        dieView.setTranslateY(20);

        wireView.setTranslateX(30);
        wireView.setTranslateY(20);

        DiceShape.DiceShapeData initialData = DiceShape.get(DICE_TYPES[0]);
        dieView.setMesh(buildConvexPolyhedronMesh(initialData, 70f).getMesh());
        wireView.setMesh(buildConvexPolyhedronMesh(initialData, 70f).getMesh());


        wireView.getTransforms().addAll(rotateX, rotateY, rotateZ);
        dieView.getTransforms().addAll(rotateX, rotateY, rotateZ);


        AmbientLight ambient = new AmbientLight(Color.color(0.55, 0.55, 0.55));
        PointLight lightTop = new PointLight(Color.WHITE);
        lightTop.setTranslateX(-150);
        lightTop.setTranslateY(-220);
        lightTop.setTranslateZ(-250);

        PointLight lightSide = new PointLight(Color.color(0.7, 0.2, 0.8));
        lightSide.setTranslateX(220);
        lightSide.setTranslateY(-40);
        lightSide.setTranslateZ(-120);

        world.getChildren().addAll(dieView, wireView, ambient, lightTop, lightSide);

        SubScene subScene = new SubScene(world, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.TRANSPARENT);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        // Kamera "von oben" auf das Objekt.
        // Y negativ = oberhalb des Würfels, Z negativ = Abstand zur Szene.
        camera.setTranslateX(0);
        camera.setTranslateY(-500);
        camera.setTranslateZ(-420);

        // Nach unten schauen
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-55);

        subScene.setCamera(camera);

        subScene.widthProperty().bind(host.widthProperty());
        subScene.heightProperty().bind(host.heightProperty());

        subScene.setOnMouseMoved(e -> {
            PickResult pick = e.getPickResult();

            if (pick == null || pick.getIntersectedNode() != dieView) {
                dieView.setMaterial(normalMat);
                lastFace[0] = -1;
                return;
            }

            int triIndex = pick.getIntersectedFace();
            if (triIndex < 0) {
                dieView.setMaterial(normalMat);
                lastFace[0] = -1;
                return;
            }

            if (triIndex != lastFace[0]) {
                lastFace[0] = triIndex;
                System.out.println("Triangle index: " + triIndex);
            }

            dieView.setMaterial(hoverMat);
        });

        host.getChildren().add(subScene);

        AnimationTimer timer = new AnimationTimer() {
            private double switchTimer = 0;
            private int diceIndex = 0;


            private long last = -1;
            private double angleX = 0;
            private double angleY = 0;
            private double angleZ = 0;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    return;
                }
                double dt = (now - last) / 1_000_000_000.0;

                switchTimer += dt;
                if (switchTimer >= 2.0) {
                    switchTimer = 0;
                    diceIndex = (diceIndex + 1) % DICE_TYPES.length;

                    DiceShape.DiceShapeData data = DiceShape.get(DICE_TYPES[diceIndex]);
                    TriangleMesh newMesh = (TriangleMesh) buildConvexPolyhedronMesh(data, 70f).getMesh();

                    dieView.setMesh(newMesh);
                    wireView.setMesh(newMesh);
                }

                last = now;

                // unterschiedliche Drehgeschwindigkeiten
                angleX += 45.0 * dt;
                angleY += 73.0 * dt;
                angleZ += 12.0 * dt;

                rotateX.setAngle(angleX);
                rotateY.setAngle(angleY);
                rotateZ.setAngle(angleZ);
            }
        };
        timer.start();

        return host;
    }

    private static MeshView buildConvexPolyhedronMesh(DiceShape.DiceShapeData data, float scale) {
        TriangleMesh mesh = new TriangleMesh();

        // Punkte
        for (double[] v : data.vertices) {
            mesh.getPoints().addAll(
                    (float) v[0] * scale,
                    (float) v[1] * scale,
                    (float) v[2] * scale
            );
        }

        // Eine Dummy-Texturkoordinate ist Pflicht
        mesh.getTexCoords().addAll(0, 0);

        // Faces triangulieren
        for (int[] face : data.faces) {
            int usableLength = face.length;
            if (data.skipLastFaceIndex) {
                usableLength -= 1;
            }

            if (usableLength < 3) {
                continue;
            }

            int v0 = face[0];

            // Fan-Triangulation: (0, i, i+1)
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

        return new MeshView(mesh);
    }

    public static void main(String[] args) {
        launch(args);
    }

    public enum ShapeType {
        CONVEXPOLYHEDRON
    }
}