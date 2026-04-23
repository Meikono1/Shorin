package com.fuchsbau.shorin.test;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.test.Dice.AxisData;
import com.fuchsbau.shorin.test.Dice.Margin;
import com.fuchsbau.shorin.test.Dice.RealtimeDicePhysics;
import com.fuchsbau.shorin.test.Dice.VectorData;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point3D;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Dice3DPhysicsRealtimeTestPane {

    private static final Logger log = FileLogger.getLogger();

    private final Canvas canvas = new Canvas(1200, 800);
    private final RealtimeDicePhysics dicePhysics = new RealtimeDicePhysics();

    private Integer dragKey = null;
    private double dragStartX, dragStartY;
    private boolean wasDrag = false;

    // 3D-Szene
    private SubScene subScene3D;
    private final Group dice3DGroup = new Group();
    // Würfel-ID → Box-Node
    private final Map<Integer, Box> diceBoxMap = new HashMap<>();

    private double trayX, trayY, trayW, trayH;

    private static final double DICE_RADIUS = 35.0;
    // Kantenlänge des 3D-Würfels = 2 * radius / sqrt(3) ≈ radius * 1.15
    private static final double DICE_SIDE = DICE_RADIUS * 0.7;
    private static final double STEP_DT = 1.0 / 200.0;

    private boolean running = false;
    private boolean physicsReady = false;
    private int frameCounter = 0;

    public Dice3DPhysicsRealtimeTestPane() {
        log.info("[DicePane] Konstruktor");
    }

    // ---------- build ----------

    // NEU – build() – Physics-Init nach dem Bind, wenn Canvas Größe bekommt
    public Node build() {
        Button throwBtn = new Button("Realtime Würfeln (2x D6)");
        throwBtn.setOnAction(e -> doThrow());

        HBox controls = new HBox(12, throwBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setStyle("-fx-padding: 8 16 8 16;");

        subScene3D = build3DSubScene();

        // root zuerst – alle binds dagegen
        StackPane root = new StackPane(canvas, subScene3D, controls);
        StackPane.setAlignment(controls, Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: rgb(18,18,24);");

        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        subScene3D.setFill(Color.TRANSPARENT);
        subScene3D.widthProperty().bind(root.widthProperty());
        subScene3D.heightProperty().bind(root.heightProperty());
        subScene3D.setMouseTransparent(false);
        subScene3D.setPickOnBounds(false);
        ;

        canvas.widthProperty().addListener((o, ov, nv) -> onResize());
        canvas.heightProperty().addListener((o, ov, nv) -> onResize());
        canvas.setOnMouseClicked(e -> {
            if (wasDrag) {
                wasDrag = false;
                return;
            }
            doThrow();
        });

        root.sceneProperty().addListener((os, oldScene, newScene) -> {
            if (newScene == null || physicsReady) return;
            javafx.application.Platform.runLater(() ->
                    javafx.application.Platform.runLater(() -> {
                        updateTray();
                        initPhysics();
                        physicsReady = true;
                        onResize();
                        log.info("[DicePane] Physics lazy-init – tray=" + trayW + "x" + trayH
                                + " canvas=" + canvas.getWidth() + "x" + canvas.getHeight());
                    })
            );
        });

        startLoop();
        log.info("[DicePane] build fertig");
        return root;
    }

    // ---------- 3D-Setup ----------

    private SubScene build3DSubScene() {
        PerspectiveCamera cam = new PerspectiveCamera(false);
        cam.setFieldOfView(50);
        cam.setNearClip(0.1);
        cam.setFarClip(5_000);
        cam.getTransforms().add(new Translate(0, 0, -600));

        AmbientLight ambient = new AmbientLight(Color.rgb(200, 190, 175));
        PointLight point = new PointLight(Color.WHITE);
        point.setTranslateX(100);
        point.setTranslateY(-300);
        point.setTranslateZ(-400);

        Group world = new Group(ambient, point, dice3DGroup);

        SubScene sub = new SubScene(world, 100, 100, true, SceneAntialiasing.BALANCED);
        sub.setCamera(cam);
        log.info("[DicePane] 3D-SubScene gebaut");

        sub.setOnMouseMoved(e -> {
            log.info("[sub] mouseMoved sceneX=" + e.getSceneX()
                    + " picked=" + e.getPickResult().getIntersectedNode());
            Node picked = e.getPickResult().getIntersectedNode();
            handleHover(picked, true);
        });

        sub.setOnMouseExited(e -> {
            // alles zurücksetzen
            diceBoxMap.values().forEach(b ->
                    ((PhongMaterial) b.getMaterial()).setDiffuseColor(Color.rgb(240, 230, 200))
            );
        });

        sub.setOnMousePressed(e -> {
            Node picked = e.getPickResult().getIntersectedNode();
            if (!(picked instanceof Box box)) return;

            // Key aus Map raussuchen
            dragKey = diceBoxMap.entrySet().stream()
                    .filter(en -> en.getValue() == box)
                    .map(Map.Entry::getKey)
                    .findFirst().orElse(null);

            if (dragKey == null) return;

            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            wasDrag = false;
            running = false;
            log.info("[drag] press key=" + dragKey);
            e.consume();
        });

        sub.setOnMouseDragged(e -> {
            if (dragKey == null) return;
            wasDrag = true;

            Box box = diceBoxMap.get(dragKey);
            if (box != null) {
                ((PhongMaterial) box.getMaterial()).setDiffuseColor(Color.rgb(255, 180, 100));
            }
            e.consume();
        });

        sub.setOnMouseReleased(e -> {
            if (dragKey == null) return;

            double dx = e.getSceneX() - dragStartX;
            double dy = e.getSceneY() - dragStartY;
            double len = Math.sqrt(dx * dx + dy * dy);

            if (len > 5) {
                PhysicsBody die = dicePhysics.diceList.get(dragKey);
                if (die != null) {
                    double scale = Math.min(len * 2.5, 600.0);
                    die.velocity.x += (dx / len) * scale;
                    die.velocity.y += (dy / len) * scale;
                    die.velocity.z = 80 + Math.random() * 40;
                    die.angularVelocity.x += (Math.random() - 0.5) * 20;
                    die.angularVelocity.y += (Math.random() - 0.5) * 20;
                    log.info("[drag] release key=" + dragKey
                            + " impuls=(" + round1((dx / len) * scale) + "," + round1((dy / len) * scale) + ")");
                }
            }

            Box box = diceBoxMap.get(dragKey);
            if (box != null) {
                ((PhongMaterial) box.getMaterial()).setDiffuseColor(Color.rgb(240, 230, 200));
            }
            dragKey = null;
            running = true;
            e.consume();
        });


        return sub;
    }

    // ---------- Physics-Init (unverändert, nur Log) ----------

    private void initPhysics() {
        Margin margin = new Margin(10, 10, 10, 10);
        dicePhysics.init(TRAY_H / 2.0, TRAY_W / 2.0, margin);
        dicePhysics.createShape("d6", DICE_RADIUS);
        log.info("[DicePane] Physics init OK – barriers h=" + TRAY_H / 2.0 + " w=" + TRAY_W / 2.0);
    }

    private void onResize() {
        updateTray();
        if (!physicsReady) {
            log.info("[DicePane] onResize – Physics noch nicht bereit, übersprungen");
            return;
        }
        Margin margin = new Margin(10, 10, 10, 10);
        dicePhysics.updateBarriers(trayH / 2.0, trayW / 2.0, margin);
        log.info("[DicePane] onResize – Barriers aktualisiert tray=" + trayW + "x" + trayH);
    }

    private static final double TRAY_W = 840.0;
    private static final double TRAY_H = 520.0;

    private void updateTray() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        trayW = TRAY_W;
        trayH = TRAY_H;
        trayX = (w - trayW) / 2.0;
        trayY = (h - trayH) / 2.0;
    }

    // ---------- Würfeln ----------

    private void doThrow() {
        if (!physicsReady) {
            log.warning("[DicePane] doThrow – Physics noch nicht bereit");
            return;
        }

        running = false;
        frameCounter = 0;

        dicePhysics.clearDice();
        dice3DGroup.getChildren().clear();
        diceBoxMap.clear();
        log.info("[DicePane] throw – Würfel gecleart");

        spawnDie(0);
        spawnDie(1);

        running = true;
        log.info("[DicePane] throw – 2 Würfel gespawnt, running=true");
    }

    private void spawnDie(int id) {
        double safeW = (trayW / 2.0) * RealtimeDicePhysics.BARRIERS_SCALE * 0.7;
        double safeH = (trayH / 2.0) * RealtimeDicePhysics.BARRIERS_SCALE * 0.7;
        double px = (Math.random() - 0.5) * 2.0 * safeW;
        double py = (Math.random() - 0.5) * 2.0 * safeH;
        double pz = 180 + Math.random() * 80;

        VectorData vd = new VectorData();
        vd.pos = new Vec3(px, py, pz);
        vd.velocity = new Vec3(
                (Math.random() - 0.5) * 30,
                (Math.random() - 0.5) * 30,
                -100 - Math.random() * 10
        );
        vd.angle = new Vec3(
                (Math.random() - 0.5) * 15,
                (Math.random() - 0.5) * 15,
                (Math.random() - 0.5) * 15
        );
        vd.axis = new AxisData();
        vd.axis.x = Math.random();
        vd.axis.y = Math.random();
        vd.axis.z = Math.random();
        vd.axis.a = Math.random();
        vd.type = "d6";

        DiceOptions opts = new DiceOptions();
        opts.secret = false;

        dicePhysics.createDice(id, "d6", "default", vd, 1.0, opts);
        dicePhysics.addDice(id);

        // 3D-Box erzeugen und registrieren
        Box box = makeDiceBox();
        diceBoxMap.put(id, box);
        dice3DGroup.getChildren().add(box);

        log.info("[DicePane] spawnDie id=" + id + " pos=(" + round1(px) + "," + round1(py) + "," + round1(pz) + ")");
    }

    /**
     * Erzeugt eine einfache Box mit Würfel-Material
     */
    private Box makeDiceBox() {
        Box box = new Box(DICE_SIDE, DICE_SIDE, DICE_SIDE);

        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(Color.rgb(240, 230, 200));
        mat.setSpecularColor(Color.rgb(255, 255, 240));
        mat.setSpecularPower(30);
        // Selbstleuchtung – verhindert komplett schwarze Flächen
        mat.setSelfIlluminationMap(null);
        mat.setDiffuseColor(Color.rgb(240, 230, 200));
        box.setMaterial(mat);


        return box;
    }

    // ---------- Loop ----------

    private void startLoop() {
        new AnimationTimer() {
            private long last = -1;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    render();
                    return;
                }
                last = now;

                if (running) {
                    dicePhysics.step(STEP_DT);
                    frameCounter++;
                    sync3D();
                } else if (frameCounter == 0) {
                    // noch nie geworfen – nichts tun
                } else {
                    // Simulation gestoppt aber sync noch einmal
                    sync3D();
                }

                render();
            }
        }.start();
        log.info("[DicePane] AnimationTimer gestartet");
    }

    /**
     * Synchronisiert jede PhysicsBody-Position + Quaternion
     * auf die zugehörige JavaFX-Box.
     * Läuft jeden Frame → kein Alloc, nur set-Calls.
     */
    private void sync3D() {
        // SubScene-Mitte berechnen – das ist der Physics-Ursprung in Screen-Coords
        double midX = subScene3D.getWidth() / 2.0;
        double midY = subScene3D.getHeight() / 2.0;

        for (Map.Entry<Integer, PhysicsBody> entry : dicePhysics.diceList.entrySet()) {
            int key = entry.getKey();
            PhysicsBody die = entry.getValue();

            Box box = diceBoxMap.get(key);
            if (box == null) continue;

            // Physics (0,0) = Mitte → + midX/midY verschieben
            box.setTranslateX(midX + die.position.x);
            box.setTranslateY(midY + die.position.y);
            box.setTranslateZ(-die.position.z);

            if (frameCounter % 60 == 0) {
                /*log.info("[sync3D] key=" + key
                        + " phys=(" + round1(die.position.x) + "," + round1(die.position.y) + "," + round1(die.position.z) + ")"
                        + " jfx=(" + round1(midX + die.position.x) + "," + round1(midY + die.position.y) + "," + round1(-die.position.z) + ")"
                        + " mid=(" + round1(midX) + "," + round1(midY) + ")");

                 */
            }

            // Quaternion – unverändert
            double qx = die.quaternion.x;
            double qy = die.quaternion.y;
            double qz = die.quaternion.z;
            double qw = die.quaternion.w;

            double angle = 2.0 * Math.acos(Math.max(-1.0, Math.min(1.0, qw)));
            double sinH = Math.sin(angle / 2.0);
            double axX = sinH > 1e-6 ? qx / sinH : 0;
            double axY = sinH > 1e-6 ? qy / sinH : 0;
            double axZ = sinH > 1e-6 ? qz / sinH : 1;

            if (box.getTransforms().isEmpty()) {
                box.getTransforms().add(new Rotate(Math.toDegrees(angle), axX, axY, axZ));
            } else {
                Rotate r = (Rotate) box.getTransforms().get(0);
                r.setAngle(Math.toDegrees(angle));
                r.setAxis(new Point3D(axX, axY, axZ));
            }
        }
    }
    // ---------- Render 2D ----------

    private void drawTray(GraphicsContext g) {
        g.setFill(Color.rgb(55, 44, 32));
        g.fillRoundRect(trayX, trayY, trayW, trayH, 24, 24);

        g.setFill(Color.rgb(35, 85, 45));
        g.fillRoundRect(trayX + 16, trayY + 16, trayW - 32, trayH - 32, 16, 16);

        g.setStroke(Color.rgb(110, 90, 60));
        g.setLineWidth(4);
        g.strokeRoundRect(trayX, trayY, trayW, trayH, 24, 24);
    }

    private void drawHUD(GraphicsContext g) {
        g.setFill(Color.LIGHTGRAY);
        g.setFont(javafx.scene.text.Font.font(14));
        g.fillText(
                "Realtime | Würfel: " + dicePhysics.getDiceBodies().size() +
                        " | Frame: " + frameCounter +
                        " | Bodies: " + dicePhysics.getBodyCount() +
                        " | Contacts: " + dicePhysics.getContactCount() +
                        " | Friction: " + dicePhysics.getFrictionContactCount(),
                20, 30
        );

        int line = 0;
        for (PhysicsBody die : dicePhysics.getDiceBodies()) {
            double y = 55 + line * 18;
            g.fillText(
                    "D" + die.id +
                            " p=(" + round1(die.position.x) + ", " + round1(die.position.y) + ", " + round1(die.position.z) + ")" +
                            " v=(" + round1(die.velocity.x) + ", " + round1(die.velocity.y) + ", " + round1(die.velocity.z) + ")" +
                            " w=(" + round1(die.angularVelocity.x) + ", " + round1(die.angularVelocity.y) + ", " + round1(die.angularVelocity.z) + ")",
                    20, y
            );
            line++;
        }

        // temporär in drawHUD() unten anfügen
        g.setFill(Color.rgb(255, 100, 100, 0.5));
        // Physics-Ursprung auf Screen: Mitte des Tray
        double ox = trayX + trayW / 2.0;
        double oy = trayY + trayH / 2.0;
        g.fillOval(ox - 6, oy - 6, 12, 12);  // roter Punkt = Physics (0,0)

        // Barrier-Grenzen einzeichnen
        g.setStroke(Color.rgb(255, 50, 50, 0.6));
        g.setStroke(Color.rgb(255, 50, 50, 0.6));
        g.setLineWidth(2);
        double bw = (TRAY_W / 2.0 - 10) * RealtimeDicePhysics.BARRIERS_SCALE;
        double bh = (TRAY_H / 2.0 - 10) * RealtimeDicePhysics.BARRIERS_SCALE;
        g.strokeRect(ox - bw, oy - bh, bw * 2, bh * 2);
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.setFill(Color.rgb(18, 18, 24));
        g.fillRect(0, 0, w, h);

        drawTray(g);
        // drawDice entfällt – übernimmt jetzt die SubScene
        drawHUD(g);
    }

    private void handleHover(Node picked, boolean entering) {
        diceBoxMap.values().forEach(b ->
                ((PhongMaterial) b.getMaterial()).setDiffuseColor(Color.rgb(240, 230, 200))
        );
        if (picked instanceof Box box && diceBoxMap.containsValue(box)) {
            ((PhongMaterial) box.getMaterial()).setDiffuseColor(Color.rgb(255, 240, 180));
            log.info("[hover] box getroffen");
        }
    }
}
