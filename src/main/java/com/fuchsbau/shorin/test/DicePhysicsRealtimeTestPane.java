package com.fuchsbau.shorin.test;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Solver.GaussSeidelSolver;
import com.fuchsbau.shorin.Engine.Physics.World.PhysicsDebugProbe;
import com.fuchsbau.shorin.test.Dice.*;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DicePhysicsRealtimeTestPane {

    private final Canvas canvas = new Canvas(1200, 800);
    private final RealtimeDicePhysics dicePhysics = new RealtimeDicePhysics();

    private double trayX, trayY, trayW, trayH;

    private static final double DICE_RADIUS = 35.0;
    private static final double STEP_DT = 1.0 / 60.0;

    private boolean running = false;
    private int frameCounter = 0;

    public DicePhysicsRealtimeTestPane() {
        updateTray();
        initPhysics();
    }

    private void initPhysics() {
        Margin margin = new Margin(10, 10, 10, 10);
        dicePhysics.init(trayH, trayW, margin);
        dicePhysics.createShape("d6", DICE_RADIUS);

        // --- Debug Probe einhängen ---
        PhysicsDebugProbe probe = new PhysicsDebugProbe();
        probe.enabled = true;
        probe.logEveryN = 40;         // ~5 log-lines/sec bei 200Hz physics
        dicePhysics.getWorld().probe = probe;

        // Solver auch verkabeln, damit maxJn gemessen wird
        dicePhysics.getWorld().solver.getClass();
        if (dicePhysics.getWorld().solver
                instanceof GaussSeidelSolver gs) {
            gs.debugProbe = probe;
        }
    }

    public Node build() {
        Button throwBtn = new Button("Realtime Würfeln (2x D6)");
        //throwBtn.setOnAction(e -> doThrow());
        throwBtn.setOnAction(e -> doStackTest());

        HBox controls = new HBox(12, throwBtn);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setStyle("-fx-padding: 8 16 8 16;");

        StackPane pane = new StackPane(canvas, controls);
        StackPane.setAlignment(controls, Pos.TOP_LEFT);
        pane.setStyle("-fx-background-color: rgb(18,18,24);");

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.widthProperty().addListener((o, ov, nv) -> onResize());
        canvas.heightProperty().addListener((o, ov, nv) -> onResize());

        canvas.setOnMouseClicked(e -> doThrow());

        startLoop();
        return pane;
    }

    private void onResize() {
        updateTray();
        Margin margin = new Margin(10, 10, 10, 10);
        dicePhysics.updateBarriers(trayH / 2.0, trayW / 2.0, margin);
    }

    private void updateTray() {
        double w = Math.max(400, canvas.getWidth());
        double h = Math.max(300, canvas.getHeight());
        trayW = Math.max(400, w * 0.7);
        trayH = Math.max(260, h * 0.65);
        trayX = (w - trayW) / 2.0;
        trayY = (h - trayH) / 2.0;
    }

    private void doStackTest() {
        running = false;
        frameCounter = 0;

        dicePhysics.clearDice();

        // Würfel 0: ruhig in der Mitte, knapp über dem Boden
        spawnDieAt(0, 0, 0, 40, 0, 0, 0);
        // Würfel 1: direkt darüber, fällt nach unten
        spawnDieAt(1, 0, 0, 250, 0, 0, -50);

        running = true;
    }

    private void spawnDieAt(int id, double px, double py, double pz,
                            double vx, double vy, double vz) {
        VectorData vd = new VectorData();
        vd.pos = new Vec3(px, py, pz);
        vd.velocity = new Vec3(vx, vy, vz);
        vd.angle = new Vec3(0, 0, 0);
        vd.axis = new AxisData();
        vd.axis.x = 0;
        vd.axis.y = 0;
        vd.axis.z = 1;
        vd.axis.a = 0;
        vd.type = "d6";

        DiceOptions opts = new DiceOptions();
        opts.secret = false;

        dicePhysics.createDice(id, "d6", "default", vd, 1.0, opts);
        dicePhysics.addDice(id);
    }

    private void doThrow() {
        running = false;
        frameCounter = 0;

        dicePhysics.clearDice();

        spawnDie(0);
        spawnDie(1);

        running = true;
    }

    private void spawnDie(int id) {
        double px = (Math.random() - 0.5) * (trayW * 0.4);
        double py = (Math.random() - 0.5) * (trayH * 0.4);
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
    }

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
                }

                render();
            }
        }.start();
    }

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        g.setFill(Color.rgb(18, 18, 24));
        g.fillRect(0, 0, w, h);

        drawTray(g);
        drawDice(g);
        drawHUD(g);
    }

    void drawTray(GraphicsContext g) {
        g.setFill(Color.rgb(55, 44, 32));
        g.fillRoundRect(trayX, trayY, trayW, trayH, 24, 24);

        g.setFill(Color.rgb(35, 85, 45));
        g.fillRoundRect(trayX + 16, trayY + 16, trayW - 32, trayH - 32, 16, 16);

        g.setStroke(Color.rgb(110, 90, 60));
        g.setLineWidth(4);
        g.strokeRoundRect(trayX, trayY, trayW, trayH, 24, 24);
    }

    private void drawDice(GraphicsContext g) {
        for (PhysicsBody die : dicePhysics.getDiceBodies()) {
            double px = die.position.x;
            double py = die.position.y;
            double pz = die.position.z;

            double screenX = trayX + trayW / 2.0 + px;
            double screenY = trayY + trayH / 2.0 + py - pz * 0.25;
            double r = DICE_RADIUS;

            g.setFill(Color.rgb(0, 0, 0, 0.30));
            g.fillOval(
                    trayX + trayW / 2.0 + px - r,
                    trayY + trayH / 2.0 + py - r * 0.4,
                    r * 2,
                    r * 0.8
            );

            drawCube3D(
                    g,
                    screenX,
                    screenY,
                    r,
                    die.quaternion.x,
                    die.quaternion.y,
                    die.quaternion.z,
                    die.quaternion.w
            );

            Integer val = dicePhysics.getDiceValue(die.id);
            String label = val != null ? String.valueOf(val) : "?";

            g.setFill(Color.rgb(230, 210, 140));
            g.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            g.fillText(label, screenX - 4, screenY - r - 8);
        }
    }

    private void drawCube3D(GraphicsContext g,
                            double cx, double cy, double size,
                            double qx, double qy, double qz, double qw) {

        double s = size;

        Vec3[] verts = new Vec3[]{
                new Vec3(-s, -s, -s),
                new Vec3(s, -s, -s),
                new Vec3(s, s, -s),
                new Vec3(-s, s, -s),
                new Vec3(-s, -s, s),
                new Vec3(s, -s, s),
                new Vec3(s, s, s),
                new Vec3(-s, s, s)
        };

        for (Vec3 v : verts) {
            rotateByQuaternion(v, qx, qy, qz, qw);
        }

        double[][] proj = new double[8][2];
        double dist = 400;

        for (int i = 0; i < 8; i++) {
            double z = verts[i].z + dist;
            proj[i][0] = cx + verts[i].x * dist / z;
            proj[i][1] = cy + verts[i].y * dist / z;
        }

        int[][] faces = {
                {0, 1, 2, 3},
                {4, 5, 6, 7},
                {0, 1, 5, 4},
                {2, 3, 7, 6},
                {1, 2, 6, 5},
                {0, 3, 7, 4}
        };

        List<int[]> faceList = new ArrayList<>(Arrays.asList(faces));
        faceList.sort(Comparator.comparingDouble(f -> avgZ(verts, f)));

        for (int[] f : faceList) {
            g.setFill(Color.rgb(240, 230, 200, 0.95));
            g.setStroke(Color.rgb(80, 70, 50));
            g.setLineWidth(2);

            g.beginPath();
            g.moveTo(proj[f[0]][0], proj[f[0]][1]);
            for (int k = 1; k < f.length; k++) {
                g.lineTo(proj[f[k]][0], proj[f[k]][1]);
            }
            g.closePath();
            g.fill();
            g.stroke();
        }
    }

    private void rotateByQuaternion(Vec3 v, double x, double y, double z, double w) {
        double ix = w * v.x + y * v.z - z * v.y;
        double iy = w * v.y + z * v.x - x * v.z;
        double iz = w * v.z + x * v.y - y * v.x;
        double iw = -x * v.x - y * v.y - z * v.z;

        v.x = ix * w + iw * (-x) + iy * (-z) - iz * (-y);
        v.y = iy * w + iw * (-y) + iz * (-x) - ix * (-z);
        v.z = iz * w + iw * (-z) + ix * (-y) - iy * (-x);
    }

    private double avgZ(Vec3[] verts, int[] face) {
        double z = 0;
        for (int idx : face) {
            z += verts[idx].z;
        }
        return z / face.length;
    }

    void drawHUD(GraphicsContext g) {
        g.setFill(Color.LIGHTGRAY);
        g.setFont(Font.font(14));
        g.fillText(
                "Realtime | Würfel: " + dicePhysics.getDiceBodies().size() +
                        " | Frame: " + frameCounter +
                        " | Bodies: " + dicePhysics.getBodyCount() +
                        " | Contacts: " + dicePhysics.getContactCount() +
                        " | Friction: " + dicePhysics.getFrictionContactCount(),
                20, 30
        );

        // --- Probe-Readout (live, damit wir nicht im Log suchen müssen) ---
        PhysicsDebugProbe probe = dicePhysics.getWorld().probe;
        if (probe != null) {
            g.setFont(Font.font("Monospaced", 12));

            // ΔKE(solve) > 0 ist der Smoking Gun – rot einfärben wenn positiv
            double dSolve = probe.getLastDeltaKESolve();
            g.setFill(dSolve > 0.1 ? Color.rgb(255, 120, 120) : Color.LIGHTGRAY);
            g.fillText(
                    String.format("PROBE step=%d  KE=%.0f  KEr=%.0f  PE=%.0f  E=%.0f",
                            probe.getStepIndex(),
                            probe.getLastKE(), probe.getLastKER(),
                            probe.getLastPE(),
                            probe.getLastKE() + probe.getLastKER() + probe.getLastPE()),
                    20, canvas.getHeight() - 78
            );
            g.fillText(
                    String.format("ΔKE(solve)=%+.1f  ΔKE(integ)=%+.1f  ← Solver darf ΔKE NICHT erhöhen!",
                            dSolve, probe.getLastDeltaKEInteg()),
                    20, canvas.getHeight() - 60
            );
            g.fillText(
                    String.format("pairs=%d  contacts=%d  friction=%d  maxJn=%.1f  maxPen=%.3f",
                            probe.getLastBroadphasePairs(),
                            probe.getLastContactCount(),
                            probe.getLastFrictionCount(),
                            probe.getLastMaxJn(), probe.getLastMaxPen()),
                    20, canvas.getHeight() - 42
            );
        }

        // alter Dice-Status-Block unverändert darüber gelassen
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
    }

    double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}