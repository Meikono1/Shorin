package com.fuchsbau.shorin.test;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.test.Dice.*;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.*;

public class DicePhysicsTestPane {

    private final Canvas canvas = new Canvas(1200, 800);
    private final DicePhysics dicePhysics = new DicePhysics();

    // Simulationsergebnis – wird nach simulateThrow gesetzt
    private SimulationResult simResult = null;
    // Aktueller Playback-Frame
    private int playFrame = 0;
    // Läuft die Animation?
    private boolean playing = false;

    private double trayX, trayY, trayW, trayH;
    private int diceIdCounter = 0;

    // Radius der Würfel in Physics-Units
    private static final double DICE_RADIUS = 35.0;

    public DicePhysicsTestPane() {
        updateTray();
        initPhysics();
    }

    private void initPhysics() {
        Margin margin = new Margin(10, 10, 10, 10);
        dicePhysics.init(trayH / 2.0, trayW / 2.0, margin, false);
        dicePhysics.createShape("d6", DICE_RADIUS);
    }

    public Node build() {
        Button throwBtn = new Button("Würfeln (D6)");
        throwBtn.setOnAction(e -> doThrow());

        Label hint = new Label("Klick zum Würfeln");
        hint.setTextFill(Color.LIGHTGRAY);

        HBox controls = new HBox(12, throwBtn, hint);
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

    // ── Würfelwurf ───────────────────────────────────────────────────────────

    private void doThrow() {
        playing = false;
        simResult = null;
        playFrame = 0;

        // Alle alten Würfel entfernen
        dicePhysics.clearDice();
        //dicePhysics.diceList.clear();

        diceIdCounter = 0;

        // 2 D6 hinzufügen
        for (int i = 0; i < 2; i++) {
            spawnDie(i);
        }

        // Physik simulieren (synchron, wie im Original)
        simResult = dicePhysics.simulateThrow(10, 15, 1.0 / 200.0, false);

        // Ergebnis loggen
        for (int id : simResult.ids) {
            Integer val = dicePhysics.getDiceValue(id);
            System.out.println("Würfel " + id + " → " + val);
        }

        // Animation starten
        playFrame = 0;
        playing = true;
    }

    private void spawnDie(int id) {
        // Zufällige Startposition innerhalb des Trays
        double px = (Math.random() - 0.5) * (trayW * 0.6);
        double py = (Math.random() - 0.5) * (trayH * 0.6);
        double pz = 200 + Math.random() * 100;

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

        dicePhysics.createDice(id, "d6", "default", vd, 1, 0, opts);
        dicePhysics.addDice(id);
    }

    // ── Animation ────────────────────────────────────────────────────────────

    private void startLoop() {
        new AnimationTimer() {
            private long last = -1;
            private int frameDelay = 0;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    return;
                }
                last = now;

                // Playback: einen Frame pro Render-Tick vorrücken
                if (playing && simResult != null) {
                    frameDelay++;
                    if (frameDelay >= 1) { // Geschwindigkeit: 1 = normal, 2 = halb
                        frameDelay = 0;
                        playFrame++;
                        if (playFrame >= simResult.iterationsNeeded) {
                            playing = false;
                        }
                    }
                }

                render();
            }
        }.start();
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    private void render() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Hintergrund
        g.setFill(Color.rgb(18, 18, 24));
        g.fillRect(0, 0, w, h);

        drawTray(g);

        if (simResult != null) {
            drawDice(g);
        }

        drawHUD(g);
    }

    private void drawTray(GraphicsContext g) {
        g.setFill(Color.rgb(55, 44, 32));
        g.fillRoundRect(trayX, trayY, trayW, trayH, 24, 24);

        g.setFill(Color.rgb(35, 85, 45));
        g.fillRoundRect(trayX + 16, trayY + 16, trayW - 32, trayH - 32, 16, 16);

        g.setStroke(Color.rgb(110, 90, 60));
        g.setLineWidth(4);
        g.strokeRoundRect(trayX, trayY, trayW, trayH, 24, 24);
    }

    private void drawDice(GraphicsContext g) {
        int frame = Math.min(playFrame, simResult.iterationsNeeded - 1);

        for (int i = 0; i < simResult.ids.size(); i++) {
            float[] pos = simResult.positions.get(i);
            if (pos == null) continue;

            int pi = frame * 3;
            if (pi + 2 >= pos.length) continue;

            double px = pos[pi];
            double py = pos[pi + 1];
            double pz = pos[pi + 2];

            // Physics-Koordinaten → Bildschirm:
            // Physics: Y = oben/unten, X = links/rechts, Z = höhe
            // Screen:  Mitte = Tray-Mitte
            double screenX = trayX + trayW / 2.0 + px;
            double screenY = trayY + trayH / 2.0 + py - pz * 0.25;

            double r = DICE_RADIUS * 0.8;

            // Schatten
            g.setFill(Color.rgb(0, 0, 0, 0.3));
            g.fillOval(trayX + trayW / 2.0 + px - r,
                    trayY + trayH / 2.0 + py - r * 0.4,
                    r * 2, r * 0.8);

            // Würfel-Körper
            float[] quat = simResult.quaternions.get(i);
            if (quat == null) continue;

            int qi = frame * 4;
            if (qi + 3 >= quat.length) continue;

            double qx = quat[qi];
            double qy = quat[qi + 1];
            double qz = quat[qi + 2];
            double qw = quat[qi + 3];

            drawCube3D(g, screenX, screenY, r, qx, qy, qz, qw);
        }
    }

    private void drawCube3D(GraphicsContext g,
                            double cx, double cy, double size,
                            double qx, double qy, double qz, double qw) {

        // Würfelpunkte (lokal)
        double s = size;
        Vec3[] verts = new Vec3[] {
                new Vec3(-s, -s, -s),
                new Vec3( s, -s, -s),
                new Vec3( s,  s, -s),
                new Vec3(-s,  s, -s),
                new Vec3(-s, -s,  s),
                new Vec3( s, -s,  s),
                new Vec3( s,  s,  s),
                new Vec3(-s,  s,  s)
        };

        // Rotation anwenden
        for (Vec3 v : verts) {
            rotateByQuaternion(v, qx, qy, qz, qw);
        }

        // Projektion
        double[][] proj = new double[8][2];
        double dist = 400;

        for (int i = 0; i < 8; i++) {
            double z = verts[i].z + dist;
            proj[i][0] = cx + verts[i].x * dist / z;
            proj[i][1] = cy + verts[i].y * dist / z;
        }

        // Faces (Back → Front sortiert)
        int[][] faces = {
                {0,1,2,3}, // back
                {4,5,6,7}, // front
                {0,1,5,4},
                {2,3,7,6},
                {1,2,6,5},
                {0,3,7,4}
        };

        // einfache Painter’s Algorithm Sortierung
        Arrays.sort(faces, Comparator.comparingDouble(f ->
                avgZ(verts, f)));

        for (int[] f : faces) {
            g.setFill(Color.rgb(240, 230, 200, 0.95));
            g.setStroke(Color.rgb(80, 70, 50));

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
        double ix =  w*v.x + y*v.z - z*v.y;
        double iy =  w*v.y + z*v.x - x*v.z;
        double iz =  w*v.z + x*v.y - y*v.x;
        double iw = -x*v.x - y*v.y - z*v.z;

        v.x = ix*w + iw*(-x) + iy*(-z) - iz*(-y);
        v.y = iy*w + iw*(-y) + iz*(-x) - ix*(-z);
        v.z = iz*w + iw*(-z) + ix*(-y) - iy*(-x);
    }

    private double avgZ(Vec3[] verts, int[] face) {
        double z = 0;
        for (int i : face) z += verts[i].z;
        return z / face.length;
    }

    private void drawHUD(GraphicsContext g) {
        g.setFill(Color.LIGHTGRAY);
        g.setFont(javafx.scene.text.Font.font(14));
        g.fillText("Klick: Würfeln  |  Würfel: " +
                        (simResult != null ? simResult.ids.size() : 0) +
                        "  |  Frame: " + playFrame +
                        (simResult != null ? "/" + simResult.iterationsNeeded : ""),
                20, 30);

        if (simResult != null && !playing) {
            StringBuilder sb = new StringBuilder("Ergebnis: ");
            for (int id : simResult.ids) {
                Integer val = dicePhysics.getDiceValue(id);
                sb.append(val != null ? val : "?").append("  ");
            }
            g.setFill(Color.GOLD);
            g.setFont(javafx.scene.text.Font.font("Georgia",
                    javafx.scene.text.FontWeight.BOLD, 22));
            g.fillText(sb.toString(), trayX + trayW / 2.0 - 80, trayY - 12);
        }
    }
}