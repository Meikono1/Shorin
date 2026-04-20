package com.fuchsbau.shorin.test;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class PolyhedronViewer extends Application {

    @Override
    public void start(Stage stage) {
        PolyhedronData d8 = new PolyhedronData(
                List.of(
                        new float[]{ 1, 0, 0},
                        new float[]{-1, 0, 0},
                        new float[]{ 0, 1, 0},
                        new float[]{ 0,-1, 0},
                        new float[]{ 0, 0, 1},
                        new float[]{ 0, 0,-1}
                ),
                List.of(
                        new int[]{0, 2, 4, 1},
                        new int[]{0, 4, 3, 2},
                        new int[]{0, 3, 5, 3},
                        new int[]{0, 5, 2, 4},
                        new int[]{1, 3, 4, 5},
                        new int[]{1, 4, 2, 6},
                        new int[]{1, 2, 5, 7},
                        new int[]{1, 5, 3, 8}
                ),
                true
        );

        MeshView meshView = createMeshView(d8, 120);

        meshView.setDrawMode(DrawMode.LINE);

        Group objectGroup = new Group(meshView);

        Rotate rotateX = new Rotate(-20, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
        objectGroup.getTransforms().addAll(rotateX, rotateY);

        Timeline spin = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(rotateY.angleProperty(), 0)),
                new KeyFrame(Duration.seconds(8), new KeyValue(rotateY.angleProperty(), 360))
        );
        spin.setCycleCount(Animation.INDEFINITE);
        spin.play();

        AmbientLight ambient = new AmbientLight(Color.color(0.55, 0.55, 0.55));

        PointLight light1 = new PointLight(Color.WHITE);
        light1.setTranslateX(-300);
        light1.setTranslateY(-200);
        light1.setTranslateZ(-600);

        PointLight light2 = new PointLight(Color.WHITE);
        light2.setTranslateX(300);
        light2.setTranslateY(150);
        light2.setTranslateZ(-400);

        Group world = new Group(objectGroup, ambient, light1, light2);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-700);
        camera.setNearClip(0.1);
        camera.setFarClip(5000);

        SubScene subScene = new SubScene(world, 900, 700, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.rgb(18, 18, 24));
        subScene.setCamera(camera);

        enableMouseRotation(subScene, rotateX, rotateY);

        StackPane root = new StackPane(subScene);
        Scene scene = new Scene(root, 900, 700, true);

        subScene.widthProperty().bind(root.widthProperty());
        subScene.heightProperty().bind(root.heightProperty());

        stage.setTitle("Polyhedron Viewer");
        stage.setScene(scene);
        stage.show();
    }

    private static MeshView createMeshView(PolyhedronData data, float scale) {
        TriangleMesh mesh = new TriangleMesh();

        // Punkte
        for (float[] v : data.vertices) {
            mesh.getPoints().addAll(
                    v[0] * scale,
                    v[1] * scale,
                    v[2] * scale
            );
        }

        // JavaFX verlangt mindestens 1 Texture Coordinate
        mesh.getTexCoords().addAll(0, 0);

        // Faces
        for (int[] face : data.faces) {
            int usableLength = data.skipLastFaceIndex ? face.length - 1 : face.length;

            if (usableLength < 3) {
                continue;
            }

            // Triangulation als Fan:
            // [a,b,c,d,e] -> (a,b,c), (a,c,d), (a,d,e)
            int a = face[0];
            for (int i = 1; i < usableLength - 1; i++) {
                int b = face[i];
                int c = face[i + 1];

                mesh.getFaces().addAll(
                        a, 0,
                        b, 0,
                        c, 0
                );
            }
        }

        MeshView meshView = new MeshView(mesh);
        meshView.setMaterial(new PhongMaterial(Color.LIGHTGRAY));
        meshView.setCullFace(CullFace.BACK);
        meshView.setDrawMode(DrawMode.FILL);

        return meshView;
    }

    private static void enableMouseRotation(SubScene scene, Rotate rotateX, Rotate rotateY) {
        final double[] last = new double[2];

        scene.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            last[0] = e.getSceneX();
            last[1] = e.getSceneY();
        });

        scene.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
            double dx = e.getSceneX() - last[0];
            double dy = e.getSceneY() - last[1];

            rotateY.setAngle(rotateY.getAngle() + dx * 0.7);
            rotateX.setAngle(rotateX.getAngle() - dy * 0.7);

            last[0] = e.getSceneX();
            last[1] = e.getSceneY();
        });
    }

    public static class PolyhedronData {
        public final List<float[]> vertices;
        public final List<int[]> faces;
        public final boolean skipLastFaceIndex;

        public PolyhedronData(List<float[]> vertices, List<int[]> faces, boolean skipLastFaceIndex) {
            this.vertices = vertices;
            this.faces = faces;
            this.skipLastFaceIndex = skipLastFaceIndex;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}