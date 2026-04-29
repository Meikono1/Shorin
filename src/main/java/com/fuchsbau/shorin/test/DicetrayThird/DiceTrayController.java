package com.fuchsbau.shorin.test.DicetrayThird;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.test.Dice.AxisData;
import com.fuchsbau.shorin.test.Dice.Margin;
import com.fuchsbau.shorin.test.Dice.RealtimeDicePhysics;
import com.fuchsbau.shorin.test.Dice.VectorData;
import com.fuchsbau.shorin.test.Dicetray.DiceRenderAdapter;
import com.fuchsbau.shorin.test.Dicetray.DiceTrayOverlayBuilder;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.AnchorPane;

import java.util.logging.Logger;

public class DiceTrayController {

    private static final Logger logger = FileLogger.getLogger();

    // --- Physik-Tuning: Fokus "realistisches Rollen/Taumeln" ---
    // Kleine Steps = stabile Kollision, kein Tunneling bei hohen Spins
    private static final double STEP_DT = 1.0 / 240.0;

    // Alle Würfeltypen, die die Tray kennt. Reihenfolge = Spawn-Reihenfolge
    // für Multi-Würfe, wenn gewünscht. Muss zu DiceShape.SHAPES passen.
    private static final String[] SUPPORTED_TYPES = {"d2", "d4", "d6", "d8", "d10", "d12", "d20"};

    // Jeder Würfel hat einen eigenen Radius — sonst werden D20 visuell zu klein
    // (mehr Flächen, kompakter) und D2 zu groß.
    // Werte relativ zum D6-Referenzradius (35).
    private static final double BASE_RADIUS = 32.0;

    private final DiceTrayOverlayBuilder overlay;
    private final DiceRenderAdapter renderAdapter;
    private final RealtimeDicePhysics dicePhysics;

    private AnimationTimer timer;
    private boolean running = false;
    private int nextDieId = 1;

    public DiceTrayController() {
        logger.info("[Tray] Controller init");

        this.overlay = new DiceTrayOverlayBuilder();
        this.renderAdapter = new DiceRenderAdapter(overlay.getWorld(), (float) BASE_RADIUS);
        this.dicePhysics = new RealtimeDicePhysics();

        initPhysics();
        buildLoop();

        logger.info("[Tray] Controller ready – " + SUPPORTED_TYPES.length + " Würfeltypen geladen");
    }

    private void initPhysics() {
        Margin margin = new Margin(10, 10, 10, 10);

        // Erstmal feste Testgröße.
        // Später kann das an die Overlay-Größe gebunden werden.
        double trayHalfHeight = 120;
        double trayHalfWidth = 180;

        dicePhysics.init(trayHalfHeight, trayHalfWidth, margin);

        // Alle Shapes einmalig erstellen → Shape-Objekte werden geteilt zwischen
        // allen Body-Instanzen eines Typs (großer Win: Narrowphase kann Cache nutzen).
        for (String type : SUPPORTED_TYPES) {
            double r = radiusFor(type);
            dicePhysics.createShape(type, r);
            logger.fine("[Tray] Shape registriert: " + type + " r=" + r);
        }
    }

    /**
     * Typ-spezifischer Radius. D2 (Cylinder/Coin) ist flach, D20 kompakt und
     * braucht leicht weniger Radius damit er sich nicht wie eine Kugel anfühlt.
     */
    private static double radiusFor(String type) {
        return switch (type) {
            case "d2" -> BASE_RADIUS;
            case "d4" -> BASE_RADIUS;
            case "d6" -> BASE_RADIUS;
            case "d8" -> BASE_RADIUS;
            case "d10" -> BASE_RADIUS;
            case "d12" -> BASE_RADIUS;
            case "d20" -> BASE_RADIUS;
            default -> BASE_RADIUS;
        };
    }

    /**
     * Typ-spezifische Masse (Würfelgröße × Dichte). Schwerere Würfel rollen
     * weniger lang, taumeln aber länger – fühlt sich gut an.
     */
    private static double massFor(String type) {
        return switch (type) {
            case "d2" -> 0.35;
            case "d4" -> 0.55;
            case "d6" -> 1.00;
            case "d8" -> 1.10;
            case "d10" -> 1.15;
            case "d12" -> 1.25;
            case "d20" -> 1.35;
            default -> 1.00;
        };
    }

    private void buildLoop() {
        timer = new AnimationTimer() {
            private long last = -1;
            // Akkumulator für fixed-timestep Physik. Das verhindert, dass
            // Frame-Spikes die Simulation zerreißen (deterministisch + stabil).
            private double accum = 0.0;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    renderAdapter.sync(dicePhysics.getDiceBodies());
                    return;
                }

                double frameDt = (now - last) / 1_000_000_000.0;
                last = now;

                // Safety-Cap gegen Debug-Pauses / GC-Spikes (max 4 Steps Nachholen)
                if (frameDt > STEP_DT * 4) {
                    frameDt = STEP_DT * 4;
                }

                if (running) {
                    accum += frameDt;
                    // Fixed-timestep – viele kleine Steps statt eines großen.
                    // Bei 240Hz: bei 60fps-Frame → 4 Steps, kein Jitter.
                    while (accum >= STEP_DT) {
                        dicePhysics.step(STEP_DT);
                        accum -= STEP_DT;
                    }
                }

                renderAdapter.sync(dicePhysics.getDiceBodies());
            }
        };
    }

    public void attachBottomRight(AnchorPane parent,
                                  double percentSize,
                                  double minW, double minH,
                                  double maxW, double maxH) {
        overlay.attachBottomRight(parent, percentSize, minW, minH, maxW, maxH);
    }

    public void start() {
        logger.info("[Tray] AnimationTimer start");
        timer.start();
    }

    public void stop() {
        logger.info("[Tray] AnimationTimer stop");
        timer.stop();
    }

    public void clearDice() {
        logger.fine("[Tray] clearDice");
        dicePhysics.clearDice();
        renderAdapter.clear();
        nextDieId = 1;
    }

    // --- Public Throw-APIs ---------------------------------------------------
    public void throwStandardSet() {
        throwDice("d20", 1, "d6", 2, "d8", 1);
    }

    public void throwOneD20() {
        throwDice("d20", 1);
    }

    public void throwTwoD6OneD8() {
        throwDice("d6", 2, "d8", 1);
    }

    public void throwOneD2() {
        throwDice("d2", 1);
    }

    /**
     * Generisches Multi-Dice: Paare aus (type, count).
     * Beispiel: throwDice("d6", 2, "d8", 1)  → 2×D6 + 1×D8
     */
    public void throwDice(Object... typeCountPairs) {
        if ((typeCountPairs.length & 1) != 0) {
            throw new IllegalArgumentException("throwDice erwartet (type,count)-Paare");
        }

        clearDice();

        int total = 0;
        for (int i = 0; i < typeCountPairs.length; i += 2) {
            String type = (String) typeCountPairs[i];
            int count = (int) typeCountPairs[i + 1];
            for (int n = 0; n < count; n++) {
                spawnDie(nextDieId++, type);
                total++;
            }
        }

        logger.info("[Tray] throwDice – " + total + " Würfel gespawnt");
        running = true;
    }

    // --- Spawn ---------------------------------------------------------------

    private void spawnDie(int id, String type) {
        VectorData vd = new VectorData();

        // Spawn-Zone: in der oberen Trayhälfte, damit der Wurf einen Bogen macht
        vd.pos = new Vec3(
                250 + Math.random() * 70,
                (Math.random() - 0.5) * 160,
                80 + Math.random() * 60
        );

        // Horizontal mehr Impuls → längeres Rollen, schöneres Taumeln
        //vd.velocity = new Vec3(
        //        -180 - Math.random() * 80,     // kräftig nach links
        //        (Math.random() - 0.5) * 60,    // leichter Seitenversatz
        //        -40 - Math.random() * 40       // moderat nach unten
        //);
        vd.velocity = new Vec3(0,     0, 0       // moderat nach unten
        );

        vd.axis = new AxisData();

        if ("d2".equals(type)) {
            // Cylinder ist per Default entlang Y orientiert → 90° um X drehen,
            // damit die flachen Seiten nach oben/unten zeigen (Welt-Z = Hochachse).
            // Ohne das liegt der D2 auf dem Mantel wie eine gerollte Münze.
            vd.axis.x = 1.0;
            vd.axis.y = 0.0;
            vd.axis.z = 0.0;
            vd.axis.a = 0.25;  // 0.25 × 2π = 90° (in deiner Spawn-API: axis.a * 2π)

            // Weniger Spin – sonst fliegt die Münze quer weg
            vd.angle = new Vec3(
                    (Math.random() - 0.5) * 10,
                    (Math.random() - 0.5) * 10,
                    (Math.random() - 0.5) * 10
            );
        } else {
            vd.axis.x = Math.random();
            vd.axis.y = Math.random();
            vd.axis.z = Math.random();
            vd.axis.a = Math.random();

            vd.angle = new Vec3(
                    (Math.random() - 0.5) * 28,
                    (Math.random() - 0.5) * 28,
                    (Math.random() - 0.5) * 28
            );
        }

        vd.type = type;

        DiceOptions opts = new DiceOptions();
        opts.secret = false;

        double mass = massFor(type);

        dicePhysics.createDice(id, type, "default", vd, mass, opts);
        dicePhysics.addDice(id);

        logger.fine("[Tray] spawnDie id=" + id + " type=" + type + " mass=" + mass);
    }
}