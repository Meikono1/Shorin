package com.fuchsbau.shorin.test;

import com.fuchsbau.shorin.Engine.CustomMesh.Vec3;
import com.fuchsbau.shorin.Engine.Dice.*;
import com.fuchsbau.shorin.Engine.Util.FloorContact;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class DicePhysicsTestPane {

    private final Canvas canvas = new Canvas(1200, 800);
    private final List<DiceObject> diceObjects = new ArrayList<>();

    private final Map<DiceType, DiceDefinition> definitions;

    private DiceTray tray = new DiceTray(250, 140, 700, 500);

    private static final double GRAVITY = 900.0;
    private static final double DEFAULT_WALL_BOUNCE = 0.72;
    private static final double DEFAULT_FLOOR_BOUNCE = 0.38;

    private DiceType selectedType = DiceType.D6;

    public DicePhysicsTestPane(java.util.Map<DiceType, DiceDefinition> definitions) {
        this.definitions = definitions;

        setupInput();
        startLoop();
    }

    public Node build() {

        ComboBox<DiceType> box = new ComboBox<>();
        box.getItems().addAll(DiceType.values());
        box.setValue(DiceType.D6);

        box.valueProperty().addListener((obs, o, n) -> selectedType = n);

        StackPane pane = new StackPane(canvas, box);
        StackPane.setAlignment(box, Pos.TOP_LEFT);

        pane.setStyle("-fx-background-color: rgb(18,18,24);");

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.widthProperty().addListener((obs, oldV, newV) -> updateTray());
        canvas.heightProperty().addListener((obs, oldV, newV) -> updateTray());

        updateTray();
        return pane;
    }

    private void updateTray() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        double trayW = Math.max(400, w * 0.55);
        double trayH = Math.max(260, h * 0.52);

        tray = new DiceTray(
                (w - trayW) / 2.0,
                (h - trayH) / 2.0,
                trayW,
                trayH
        );
    }

    private void setupInput() {
        canvas.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) {
                return;
            }

            spawnDie();
        });
    }

    private void spawnDie() {
        double spawnX = tray.x() + tray.width() * 0.5 + random(-80, 80);
        double spawnY = tray.y() + tray.height() * 0.3 + random(-50, 50);

        DiceDefinition def = getDefinition(selectedType);

        DiceObject die = new DiceObject(selectedType, def, spawnX, spawnY, 180);

        die.setScale(1.0);
        die.setRadius(26);

        die.setAngleX(random(0, 360));
        die.setAngleY(random(0, 360));
        die.setAngleZ(random(0, 360));

        die.setVelocityX(random(-650, 650));
        die.setVelocityY(random(-500, 500));
        die.setVelocityZ(random(-40, 40));

        die.setAngularVelocityX(random(-540, 540));
        die.setAngularVelocityY(random(-540, 540));
        die.setAngularVelocityZ(random(-540, 540));

        die.setRolling(true);
        die.setSleeping(false);

        die.setCurrentFaceValue(1);
        die.setLastStableFaceValue(1);

        diceObjects.add(die);
    }

    private void startLoop() {
        new AnimationTimer() {
            private long lastNow = -1;

            @Override
            public void handle(long now) {
                if (lastNow < 0) {
                    lastNow = now;
                    render();
                    return;
                }

                double dt = (now - lastNow) / 1_000_000_000.0;
                lastNow = now;

                update(dt);
                render();
            }
        }.start();
    }

    private void update(double dt) {
        for (DiceObject die : diceObjects) {
            if (die.isSleeping()) {
                continue;
            }

            double linearFriction = getLinearFriction(die);
            double angularFriction = getAngularFriction(die);
            double wallBounce = getWallBounce(die);
            double floorBounce = getFloorBounce(die);

            // Gravity auf Z
            die.setVelocityZ(die.getVelocityZ() - GRAVITY * dt);

            // Position
            die.setCenterX(die.getCenterX() + die.getVelocityX() * dt);
            die.setCenterY(die.getCenterY() + die.getVelocityY() * dt);
            die.setCenterZ(die.getCenterZ() + die.getVelocityZ() * dt);

            // Rotation
            die.setAngleX(die.getAngleX() + die.getAngularVelocityX() * dt);
            die.setAngleY(die.getAngleY() + die.getAngularVelocityY() * dt);
            die.setAngleZ(die.getAngleZ() + die.getAngularVelocityZ() * dt);

            // Traywände in X/Y
            if (die.getCenterX() - die.getRadius() < tray.minX()) {
                die.setCenterX(tray.minX() + die.getRadius());
                die.setVelocityX(-die.getVelocityX() * wallBounce);
            } else if (die.getCenterX() + die.getRadius() > tray.maxX()) {
                die.setCenterX(tray.maxX() - die.getRadius());
                die.setVelocityX(-die.getVelocityX() * wallBounce);
            }

            if (die.getCenterY() - die.getRadius() < tray.minY()) {
                die.setCenterY(tray.minY() + die.getRadius());
                die.setVelocityY(-die.getVelocityY() * wallBounce);
            } else if (die.getCenterY() + die.getRadius() > tray.maxY()) {
                die.setCenterY(tray.maxY() - die.getRadius());
                die.setVelocityY(-die.getVelocityY() * wallBounce);
            }

            FloorContact floorContact = DicePhysics.resolveFloorCollision(die, dt, linearFriction, angularFriction, floorBounce);

            if (!floorContact.colliding()) {
                // in der Luft schwächere Dämpfung
                die.setVelocityX(applyDecay(die.getVelocityX(), 0.15, dt));
                die.setVelocityY(applyDecay(die.getVelocityY(), 0.15, dt));

                die.setAngularVelocityX(applyDecay(die.getAngularVelocityX(), 0.10, dt));
                die.setAngularVelocityY(applyDecay(die.getAngularVelocityY(), 0.10, dt));
                die.setAngularVelocityZ(applyDecay(die.getAngularVelocityZ(), 0.10, dt));
            }

            clampSmallValues(die);

            boolean grounded = DicePhysics.isGroundedByVertices(die);
            boolean motionStopped =
                    grounded &&
                            die.getVelocityX() == 0.0 &&
                            die.getVelocityY() == 0.0 &&
                            die.getVelocityZ() == 0.0 &&
                            die.getAngularVelocityX() == 0.0 &&
                            die.getAngularVelocityY() == 0.0 &&
                            die.getAngularVelocityZ() == 0.0;

            if (motionStopped) {
                die.setRolling(false);
                die.setSleeping(true);

                int rolled;
                if (die.getType() == DiceType.D2) {
                    rolled = DicePhysics.determineD2Value(die);
                } else {
                    rolled = DicePhysics.determineTopFaceValue(die);
                }

                die.setCurrentFaceValue(rolled);
                die.setLastStableFaceValue(rolled);
            }
        }
    }

    private void clampSmallValues(DiceObject die) {
        if (Math.abs(die.getVelocityX()) < 2.5) die.setVelocityX(0);
        if (Math.abs(die.getVelocityY()) < 2.5) die.setVelocityY(0);
        if (Math.abs(die.getVelocityZ()) < 6.0) die.setVelocityZ(0);

        if (Math.abs(die.getAngularVelocityX()) < 4.0) die.setAngularVelocityX(0);
        if (Math.abs(die.getAngularVelocityY()) < 4.0) die.setAngularVelocityY(0);
        if (Math.abs(die.getAngularVelocityZ()) < 4.0) die.setAngularVelocityZ(0);
    }

    /**
     * frictionPerSecond:
     * 0.0  = keine Reibung
     * 1.0  = sehr starke Reibung
     */
    private double applyDecay(double value, double frictionPerSecond, double dt) {
        double decay = Math.max(0.0, 1.0 - frictionPerSecond * dt);
        return value * decay;
    }

    private double getLinearFriction(DiceObject die) {
        return 1.2;
    }

    private double getAngularFriction(DiceObject die) {
        return 1.8;
    }

    private double getWallBounce(DiceObject die) {
        // später aus DiceDefinition holen
        return DEFAULT_WALL_BOUNCE;
    }

    private double getFloorBounce(DiceObject die) {
        // später aus DiceDefinition holen
        return DEFAULT_FLOOR_BOUNCE;
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.setFill(Color.rgb(18, 18, 24));
        g.fillRect(0, 0, w, h);

        drawTray(g);

        for (DiceObject die : diceObjects) {
            drawDie(g, die);
        }

        g.setFill(Color.LIGHTGRAY);
        g.fillText("Linksklick: D6 in den Tray werfen", 20, 30);
        g.fillText("Würfel: " + diceObjects.size(), 20, 50);
    }

    private void drawTray(GraphicsContext g) {
        double x = tray.x();
        double y = tray.y();
        double w = tray.width();
        double h = tray.height();

        g.setFill(Color.rgb(55, 44, 32));
        g.fillRoundRect(x, y, w, h, 24, 24);

        g.setFill(Color.rgb(35, 85, 45));
        g.fillRoundRect(x + 16, y + 16, w - 32, h - 32, 16, 16);

        g.setStroke(Color.rgb(110, 90, 60));
        g.setLineWidth(4);
        g.strokeRoundRect(x, y, w, h, 24, 24);
    }

    private void drawDie(GraphicsContext g, DiceObject die) {
        double screenX = die.getCenterX();
        double screenY = die.getCenterY() - die.getCenterZ() * 0.35;

        // Schatten
        double shadowRadius = die.getRadius() * die.getScale();
        g.setFill(Color.rgb(0, 0, 0, 0.16));
        g.fillOval(
                die.getCenterX() - shadowRadius,
                die.getCenterY() - shadowRadius * 0.55,
                shadowRadius * 2,
                shadowRadius * 1.1
        );

        drawMesh(g, die);

        g.setFill(Color.WHITE);
        g.fillText(die.getType().name(), screenX - 12, screenY - die.getRadius() - 10);

        String state = die.isSleeping() ? "STOP" : "ROLL";
        g.fillText(state + " " + die.getLastStableFaceValue(), screenX - 20, screenY + 4);
    }

    private double random(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void drawMesh(GraphicsContext g, DiceObject die) {
        List<int[]> faces = die.getDefinition().faces();
        List<Vec3> world = DicePhysics.getWorldVertices(die);

        List<ProjectedVertex> projected = new ArrayList<>(world.size());
        for (Vec3 v : world) {
            projected.add(project(v));
        }

        List<int[]> sortedFaces = new ArrayList<>(faces);
        sortedFaces.sort((a, b) -> Double.compare(avgDepth(b, world), avgDepth(a, world)));

        for (int[] face : sortedFaces) {
            if (face.length < 3) {
                continue;
            }

            Vec3 normal = computeFaceNormal(face, world);

            // Nur sichtbare Faces zeichnen
            /*if (normal.z() >= 0) {
                continue;
            }*/

            double[] xs = new double[face.length];
            double[] ys = new double[face.length];

            for (int i = 0; i < face.length; i++) {
                ProjectedVertex p = projected.get(face[i]);
                xs[i] = p.x();
                ys[i] = p.y();
            }

            double shade = Math.max(0.35, Math.min(1.0, -normal.z()));
            Color base = die.isSleeping() ? Color.DARKSEAGREEN : Color.STEELBLUE;
            Color fill = new Color(
                    clamp(base.getRed() * shade),
                    clamp(base.getGreen() * shade),
                    clamp(base.getBlue() * shade),
                    1.0
            );

            g.setFill(fill);
            g.fillPolygon(xs, ys, face.length);

            g.setStroke(Color.BLACK);
            g.strokePolygon(xs, ys, face.length);
        }
    }

    private DiceDefinition getDefinition(DiceType type) {
        DiceDefinition definition = definitions.get(type);
        if (definition == null) {
            throw new IllegalStateException("Keine DiceDefinition für Typ vorhanden: " + type);
        }
        return definition;
    }

    private ProjectedVertex project(Vec3 v) {
        double screenX = v.x();
        double screenY = v.y() - v.z() * 0.35;
        return new ProjectedVertex(screenX, screenY);
    }

    private double avgDepth(int[] face, List<Vec3> world) {
        double sum = 0.0;
        for (int index : face) {
            sum += world.get(index).z();
        }
        return sum / face.length;
    }

    private Vec3 computeFaceNormal(int[] face, List<Vec3> world) {
        Vec3 a = world.get(face[0]);
        Vec3 b = world.get(face[1]);
        Vec3 c = world.get(face[2]);

        Vec3 ab = b.sub(a);
        Vec3 ac = c.sub(a);

        return ab.cross(ac).normalize();
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private record ProjectedVertex(double x, double y) {
    }
}