package com.fuchsbau.shorin.test.Dicetray;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape.DiceShapeData;
import javafx.animation.AnimationTimer;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
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

import java.util.Collection;

public class DiceTrayView {

    private final StackPane container = new StackPane();

    private final Canvas backgroundCanvas = new Canvas();
    private final Canvas trayCanvas = new Canvas();

    private final Group world = new Group();
    private final SubScene subScene = new SubScene(world, 300, 200, true, SceneAntialiasing.BALANCED);

    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    private final MeshView dieView = new MeshView();
    private final MeshView wireView = new MeshView();

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(0, Rotate.Z_AXIS);

    private final PhongMaterial normalMat = new PhongMaterial(Color.LIGHTGRAY);
    private final PhongMaterial hoverMat = new PhongMaterial(Color.ORANGE);

    private DiceRenderAdapter renderAdapter;

    private final int[] lastFace = {-1};

    private String[] diceTypes = {"d4", "d6", "d8", "d10", "d12", "d20"};
    private int diceIndex = 0;
    private double switchIntervalSeconds = 2.0;
    private boolean autoCycleEnabled = true;
    private float meshScale = 70f;

    private AnimationTimer timer;
    private long last = -1;
    private double switchTimer = 0;
    private double angleX = 0;
    private double angleY = 0;
    private double angleZ = 0;

    public DiceTrayView() {
        buildUi();
        build3d();
        buildInteraction();
        buildTimer();
        redrawCanvases();
        loadCurrentDie();
    }

    public StackPane getContainer() {
        return container;
    }

    public void attachBottomRight(AnchorPane parent,
                                  double percentSize,
                                  double minW, double minH,
                                  double maxW, double maxH) {
        parent.getChildren().add(container);

        AnchorPane.setRightAnchor(container, 10.0);
        AnchorPane.setBottomAnchor(container, 10.0);

        container.prefWidthProperty().bind(parent.widthProperty().multiply(percentSize));
        container.prefHeightProperty().bind(parent.heightProperty().multiply(percentSize));

        container.setMinSize(minW, minH);
        container.setMaxSize(maxW, maxH);
    }

    public void setDiceTypes(String... diceTypes) {
        if (diceTypes == null || diceTypes.length == 0) {
            throw new IllegalArgumentException("diceTypes darf nicht leer sein");
        }
        this.diceTypes = diceTypes;
        this.diceIndex = 0;
        loadCurrentDie();
    }

    public void setDiceType(String diceType) {
        this.diceTypes = new String[]{diceType};
        this.diceIndex = 0;
        loadCurrentDie();
    }

    public void setSwitchIntervalSeconds(double seconds) {
        this.switchIntervalSeconds = seconds;
    }

    public void setAutoCycleEnabled(boolean enabled) {
        this.autoCycleEnabled = enabled;
    }

    public void setMeshScale(float meshScale) {
        this.meshScale = meshScale;
        loadCurrentDie();
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private void buildUi() {
        container.setPickOnBounds(false);
        container.setStyle("-fx-background-color: transparent;");

        backgroundCanvas.widthProperty().bind(container.widthProperty());
        backgroundCanvas.heightProperty().bind(container.heightProperty());

        trayCanvas.widthProperty().bind(container.widthProperty());
        trayCanvas.heightProperty().bind(container.heightProperty());

        subScene.widthProperty().bind(container.widthProperty());
        subScene.heightProperty().bind(container.heightProperty());
        subScene.setFill(Color.TRANSPARENT);

        backgroundCanvas.widthProperty().addListener((obs, o, n) -> redrawBackground());
        backgroundCanvas.heightProperty().addListener((obs, o, n) -> redrawBackground());
        trayCanvas.widthProperty().addListener((obs, o, n) -> redrawTray());
        trayCanvas.heightProperty().addListener((obs, o, n) -> redrawTray());

        container.getChildren().addAll(backgroundCanvas, trayCanvas, subScene);
    }

    private void build3d() {
        renderAdapter = new DiceRenderAdapter(world, 70f);


        dieView.setMaterial(normalMat);
        dieView.setCullFace(CullFace.BACK);
        dieView.setDrawMode(DrawMode.FILL);

        wireView.setDrawMode(DrawMode.LINE);
        wireView.setCullFace(CullFace.NONE);
        wireView.setMaterial(new PhongMaterial(Color.BLACK));
        wireView.setScaleX(1.01);
        wireView.setScaleY(1.01);
        wireView.setScaleZ(1.01);
        wireView.setMouseTransparent(true);

        dieView.getTransforms().addAll(rotateX, rotateY, rotateZ);
        wireView.getTransforms().addAll(rotateX, rotateY, rotateZ);

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

        camera.setNearClip(0.1);
        camera.setFarClip(5000);
        camera.setTranslateX(0);
        camera.setTranslateY(-500);
        camera.setTranslateZ(-420);
        camera.setRotationAxis(Rotate.X_AXIS);
        camera.setRotate(-55);


        subScene.setCamera(camera);
    }

    private void buildInteraction() {
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
    }

    private void buildTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    return;
                }

                double dt = (now - last) / 1_000_000_000.0;
                last = now;

                if (autoCycleEnabled && diceTypes.length > 1) {
                    switchTimer += dt;
                    if (switchTimer >= switchIntervalSeconds) {
                        switchTimer = 0;
                        diceIndex = (diceIndex + 1) % diceTypes.length;
                        loadCurrentDie();
                    }
                }

                angleX += 45.0 * dt;
                angleY += 73.0 * dt;
                angleZ += 12.0 * dt;

                rotateX.setAngle(angleX);
                rotateY.setAngle(angleY);
                rotateZ.setAngle(angleZ);
            }
        };
    }

    private void loadCurrentDie() {
        DiceShapeData data = DiceShape.get(diceTypes[diceIndex]);
        float scale = computeNormalizedScale(data, meshScale);
        TriangleMesh mesh = buildConvexPolyhedronMesh(data, scale);

        dieView.setMesh(mesh);
        wireView.setMesh(mesh);
    }

    private float computeNormalizedScale(DiceShapeData data, float targetRadius) {
        double maxDist = 0;

        for (double[] v : data.vertices) {
            double d = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            if (d > maxDist) {
                maxDist = d;
            }
        }

        if (maxDist == 0) return targetRadius;
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

    private void redrawCanvases() {
        redrawBackground();
        redrawTray();
    }

    private void redrawBackground() {
        GraphicsContext g = backgroundCanvas.getGraphicsContext2D();
        double w = backgroundCanvas.getWidth();
        double h = backgroundCanvas.getHeight();

        g.clearRect(0, 0, w, h);
        g.setFill(Color.rgb(8, 10, 14, 0.45));
        g.fillRoundRect(0, 0, w, h, 18, 18);
    }

    private void redrawTray() {
        GraphicsContext g = trayCanvas.getGraphicsContext2D();
        double w = trayCanvas.getWidth();
        double h = trayCanvas.getHeight();

        g.clearRect(0, 0, w, h);

        double pad = 14;
        double trayX = pad;
        double trayY = pad;
        double trayW = w - pad * 2;
        double trayH = h - pad * 2;

        g.setFill(Color.rgb(70, 52, 38, 0.95));
        g.fillRoundRect(trayX, trayY, trayW, trayH, 20, 20);

        g.setStroke(Color.rgb(120, 90, 60, 1.0));
        g.setLineWidth(4);
        g.strokeRoundRect(trayX, trayY, trayW, trayH, 20, 20);

        g.setStroke(Color.rgb(35, 25, 18, 0.7));
        g.setLineWidth(2);
        g.strokeRoundRect(trayX + 3, trayY + 3, trayW - 6, trayH - 6, 16, 16);
    }

    public void renderSimulation(Collection<PhysicsBody> bodies) {
        renderAdapter.sync(bodies);
    }
}