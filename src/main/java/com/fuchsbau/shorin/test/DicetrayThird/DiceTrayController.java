package com.fuchsbau.shorin.test.DicetrayThird;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.test.Dice.AxisData;
import com.fuchsbau.shorin.test.Dice.Margin;
import com.fuchsbau.shorin.test.Dice.RealtimeDicePhysics;
import com.fuchsbau.shorin.test.Dice.VectorData;
import com.fuchsbau.shorin.test.Dicetray.DiceRenderAdapter;
import com.fuchsbau.shorin.test.Dicetray.DiceTrayOverlayBuilder;
import javafx.animation.AnimationTimer;
import javafx.scene.layout.AnchorPane;

public class DiceTrayController {

    private static final double STEP_DT = 1.0 / 200.0;
    private static final double DICE_RADIUS = 35.0;

    private final DiceTrayOverlayBuilder overlay;
    private final DiceRenderAdapter renderAdapter;
    private final RealtimeDicePhysics dicePhysics;

    private AnimationTimer timer;
    private boolean running = false;
    private int nextDieId = 1;

    public DiceTrayController() {
        this.overlay = new DiceTrayOverlayBuilder();
        this.renderAdapter = new DiceRenderAdapter(overlay.getWorld(), 35f);
        this.dicePhysics = new RealtimeDicePhysics();

        initPhysics();
        buildLoop();
    }

    private void initPhysics() {
        Margin margin = new Margin(10, 10, 10, 10);

        // Erstmal feste Testgröße.
        // Später kann das an die Overlay-Größe gebunden werden.
        double trayHalfHeight = 120;
        double trayHalfWidth = 180;

        dicePhysics.init(trayHalfHeight, trayHalfWidth, margin);
        dicePhysics.createShape("d6", DICE_RADIUS);
    }

    private void buildLoop() {
        timer = new AnimationTimer() {
            private long last = -1;

            @Override
            public void handle(long now) {
                if (last < 0) {
                    last = now;
                    renderAdapter.sync(dicePhysics.getDiceBodies());
                    return;
                }

                last = now;

                if (running) {
                    dicePhysics.step(STEP_DT);
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
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void clearDice() {
        dicePhysics.clearDice();
        renderAdapter.clear();
        nextDieId = 1;
    }

    public void throwTwoD6() {
        clearDice();

        spawnD6(nextDieId++);
        spawnD6(nextDieId++);

        running = true;
    }

    public void throwOneD6() {
        clearDice();

        spawnD6(nextDieId++);

        running = true;
    }

    private void spawnD6(int id) {
        VectorData vd = new VectorData();
        vd.pos = new Vec3(
                (Math.random() - 0.5) * 120,
                (Math.random() - 0.5) * 80,
                180 + Math.random() * 80
        );

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
}