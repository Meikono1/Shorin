package com.fuchsbau.shorin.test;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Stresstest: Bézier-Kurven Performance
 * <p>
 * Tasten:
 * [+]  mehr Figuren
 * [-]  weniger Figuren
 * [F]  fill an/aus
 * [S]  stroke an/aus
 * [L]  Layer-Modus (2x fill pass) an/aus
 */
public class BezierStressTest extends Application {

    // --- Konfiguration ---
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int STEP = 10;   // figuren pro +/- schritt
    private static final int CURVES_PER_FIG = 50;  // bezierCurveTo calls pro figur

    // --- State ---
    private int figureCount = 10;
    private boolean doFill = true;
    private boolean doStroke = true;
    private boolean doLayer = false;

    // --- FPS Messung ---
    private long lastTime = 0;
    private double fps = 0;
    private double fpsSmooth = 0; // geglättet

    // --- Farben ---
    private final Color COL_BG = Color.rgb(18, 18, 24);
    private final Color COL_FILL = Color.rgb(180, 210, 255, 0.7);
    private final Color COL_STROKE = Color.rgb(80, 140, 220);
    private final Color COL_LAYER = Color.rgb(255, 160, 60, 0.35); // farb-layer
    private final Color COL_TEXT = Color.rgb(220, 220, 230);
    private final Color COL_WARN = Color.rgb(255, 80, 80);

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Tastatur
        Scene scene = new Scene(new StackPane(canvas));
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case PLUS, ADD -> figureCount = Math.min(figureCount + STEP, 5000);
                case MINUS, SUBTRACT -> figureCount = Math.max(figureCount - STEP, 0);
                case F -> doFill = !doFill;
                case S -> doStroke = !doStroke;
                case L -> doLayer = !doLayer;
                default -> {
                }
            }
        });

        // Render Loop
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // FPS berechnen
                if (lastTime != 0) {
                    double delta = (now - lastTime) / 1_000_000_000.0;
                    fps = 1.0 / delta;
                    // exponentieller glättungs-filter
                    fpsSmooth = fpsSmooth * 0.85 + fps * 0.15;
                }
                lastTime = now;

                render(gc, now);
            }
        };
        timer.start();

        stage.setTitle("Bézier Stresstest");
        stage.setScene(scene);
        stage.show();
    }

    private void render(GraphicsContext gc, long now) {
        // Hintergrund
        gc.setFill(COL_BG);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        double t = now / 1_000_000_000.0; // sekunden, für animation

        // --- Figuren zeichnen ---
        for (int i = 0; i < figureCount; i++) {
            drawFigure(gc, i, t);
        }

        // --- HUD ---
        drawHud(gc);
    }

    /**
     * Zeichnet eine animierte Figur mit CURVES_PER_FIG bezierCurveTo-Calls.
     * Simuliert eine realistische Menge an Kurven für ein Papermodel-Körperteil.
     */
    private void drawFigure(GraphicsContext gc, int index, double t) {
        // Position über den Canvas verteilen
        double col = (index % 40) * (WIDTH / 40.0) + 16;
        double row = (index / 40) * (HEIGHT / 13.0) + 16;

        // kleine Wackel-Animation – simuliert Bone-Movement
        double wobble = Math.sin(t * 2.0 + index * 0.3) * 4.0;
        double cx = col + wobble;
        double cy = row;
        double scale = 14.0; // figurgrösse

        // --- Pass 1: Basis-Fill ---
        if (doFill) {
            gc.setFill(COL_FILL);
            buildFigurePath(gc, cx, cy, scale, t, index);
            gc.fill();
        }

        // --- Pass 2: Farb-Layer (simuliert Layer-System) ---
        if (doLayer) {
            gc.setFill(COL_LAYER);
            buildFigurePath(gc, cx, cy, scale, t, index);
            gc.fill();
        }

        // --- Stroke ---
        if (doStroke) {
            /*gc.setStroke(COL_STROKE);
            gc.setLineWidth(1.2);
            buildFigurePath(gc, cx, cy, scale, t, index);
            gc.stroke();*/
            gc.setFill(COL_STROKE);
            buildFigurePath(gc, cx, cy, scale * 1.04, t, index); // 4% grösser
            gc.fill();

            // Pass 2 – normaler Fill drüber
            gc.setFill(COL_FILL);
            buildFigurePath(gc, cx, cy, scale, t, index);
            gc.fill();
        }
    }

    /**
     * Baut den Pfad einer Figur – CURVES_PER_FIG bezierCurveTo Calls.
     * Der Pfad ist bewusst komplex um ein echtes Papermodel zu simulieren.
     */
    private void buildFigurePath(GraphicsContext gc, double cx, double cy,
                                 double s, double t, int idx) {
        // Wir bauen ~50 Kurven-Calls durch mehrere geschlossene Teilpfade.
        // Jeder Teilpfad = ein "Körperteil" (torso, arm, bein, kopf...)
        gc.beginPath();

        // Torso (10 Kurven)
        buildClosedCurves(gc, cx, cy - s * 0.5, s * 0.6, s * 0.9, 10, t + idx);

        // Kopf (8 Kurven)
        buildClosedCurves(gc, cx, cy - s * 1.6, s * 0.45, s * 0.45, 8, t + idx + 1);

        // Linker Arm (8 Kurven)
        buildClosedCurves(gc, cx - s * 0.85, cy - s * 0.3, s * 0.22, s * 0.65, 8, t + idx + 2);

        // Rechter Arm (8 Kurven)
        buildClosedCurves(gc, cx + s * 0.85, cy - s * 0.3, s * 0.22, s * 0.65, 8, t + idx + 3);

        // Linkes Bein (8 Kurven)
        buildClosedCurves(gc, cx - s * 0.3, cy + s * 0.9, s * 0.24, s * 0.8, 8, t + idx + 4);

        // Rechtes Bein (8 Kurven)
        buildClosedCurves(gc, cx + s * 0.3, cy + s * 0.9, s * 0.24, s * 0.8, 8, t + idx + 5);
    }

    /**
     * Zeichnet einen geschlossenen elliptischen Bézier-Pfad mit n Kurven.
     * offset sorgt dafür dass jede Figur leicht anders aussieht.
     */
    private void buildClosedCurves(GraphicsContext gc,
                                   double cx, double cy,
                                   double rx, double ry,
                                   int n, double offset) {
        double step = Math.PI * 2.0 / n;

        double startX = cx + Math.cos(offset) * rx;
        double startY = cy + Math.sin(offset) * ry;
        gc.moveTo(startX, startY);

        for (int i = 0; i < n; i++) {
            double a0 = offset + i * step;
            double a1 = offset + (i + 1) * step;
            double am = (a0 + a1) / 2.0;

            // Kontrollpunkte leicht nach aussen versetzt – typischer Bézier-Look
            double k = 0.35;
            double cp1x = cx + Math.cos(a0 + step * k) * rx * 1.15;
            double cp1y = cy + Math.sin(a0 + step * k) * ry * 1.15;
            double cp2x = cx + Math.cos(a1 - step * k) * rx * 1.15;
            double cp2y = cy + Math.sin(a1 - step * k) * ry * 1.15;
            double ex = cx + Math.cos(a1) * rx;
            double ey = cy + Math.sin(a1) * ry;

            gc.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, ex, ey);
        }
        gc.closePath();
    }

    // --- HUD ---
    private void drawHud(GraphicsContext gc) {
        gc.setFont(Font.font("Monospaced", 14));

        // FPS Farbe: grün / gelb / rot
        Color fpsColor;
        if (fpsSmooth >= 60) fpsColor = Color.rgb(80, 220, 120);
        else if (fpsSmooth >= 30) fpsColor = Color.rgb(240, 200, 60);
        else fpsColor = COL_WARN;

        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRoundRect(10, 10, 330, 160, 8, 8);

        int curveTotal = figureCount * CURVES_PER_FIG;

        gc.setFill(fpsColor);
        gc.fillText(String.format("FPS:     %.1f", fpsSmooth), 24, 36);

        gc.setFill(COL_TEXT);
        gc.fillText(String.format("Figuren: %d", figureCount), 24, 56);
        gc.fillText(String.format("Kurven:  %d total", curveTotal), 24, 76);
        gc.fillText(String.format("Fill:    %s", doFill ? "AN" : "AUS"), 24, 100);
        gc.fillText(String.format("Stroke:  %s", doStroke ? "AN" : "AUS"), 24, 118);
        gc.fillText(String.format("Layer:   %s", doLayer ? "AN" : "AUS"), 24, 136);

        gc.setFill(Color.rgb(150, 150, 160));
        gc.fillText("[+][-] Figuren   [F] Fill   [S] Stroke   [L] Layer", 24, 158);
    }

    public static void main(String[] args) {
        launch(args);
    }
}