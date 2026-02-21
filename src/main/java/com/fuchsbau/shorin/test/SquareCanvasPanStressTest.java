package com.fuchsbau.shorin.test;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.PriorityQueue;

public class SquareCanvasPanStressTest extends Application {

    // Grid
    private static final int COLS = 400;
    private static final int ROWS = 400;

    // Squares
    private static final double BASE_TILE = 16.0; // tile size in world px at zoom=1

    // Range rules
    private static final int FEET_PER_TILE = 5;
    private static final int RANGE_FT = 30;
    private static final int BUDGET = RANGE_FT / FEET_PER_TILE; // 6 "normal tiles"

    // Camera
    private double camX = 0;
    private double camY = 0;
    private double zoom = 1.0;

    // Drag
    private boolean dragging = false;
    private double lastMouseX, lastMouseY;

    // HUD
    private long lastFpsTime = 0;
    private int frames = 0;
    private double fps = 0;
    private int lastDrawn = 0;

    // State
    private final boolean[] selected = new boolean[COLS * ROWS];
    private final boolean[] inRange = new boolean[COLS * ROWS];

    // Dijkstra buffers (reused)
    private final int[] dist = new int[COLS * ROWS];

    // Search
    private int startIdx = -1;
    private int endIdx = -1;

    private final boolean[] inPath = new boolean[COLS * ROWS];

    private final AStarGrid aStarGrid = new AStarGrid(COLS, ROWS);


    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(1200, 800);
        GraphicsContext g = canvas.getGraphicsContext2D();

        StackPane root = new StackPane(canvas);
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // Pan (drag)
        canvas.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                dragging = true;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });
        canvas.setOnMouseReleased(e -> dragging = false);
        canvas.setOnMouseDragged(e -> {
            if (!dragging) return;
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            camX -= dx / zoom;
            camY -= dy / zoom;
        });

        // Zoom around cursor
        canvas.addEventFilter(ScrollEvent.SCROLL, e -> {
            double oldZoom = zoom;
            double factor = Math.pow(1.0015, e.getDeltaY());
            zoom = clamp(zoom * factor, 0.2, 6.0);

            double mx = e.getX();
            double my = e.getY();

            double worldXBefore = screenToWorldX(mx, oldZoom);
            double worldYBefore = screenToWorldY(my, oldZoom);
            double worldXAfter = screenToWorldX(mx, zoom);
            double worldYAfter = screenToWorldY(my, zoom);

            camX += (worldXBefore - worldXAfter);
            camY += (worldYBefore - worldYAfter);
            e.consume();
        });

        canvas.setOnMouseClicked(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            if (dragging) return;

            double wx = screenToWorldX(e.getX(), zoom);
            double wy = screenToWorldY(e.getY(), zoom);

            int col = (int) Math.floor(wx / BASE_TILE);
            int row = (int) Math.floor(wy / BASE_TILE);
            if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;

            int idx = row * COLS + col;

            // 3rd click: reset
            if (startIdx != -1 && endIdx != -1) {
                startIdx = -1;
                endIdx = -1;
                Arrays.fill(inPath, false);
                // optional: Arrays.fill(inRange,false);
                // optional: Arrays.fill(selected,false);
            }

            if (startIdx == -1) {
                startIdx = idx;
            } else if (endIdx == -1) {
                endIdx = idx;

                int startRow = startIdx / COLS;
                int startCol = startIdx % COLS;

                int endRow = endIdx / COLS;
                int endCol = endIdx % COLS;

                int[] path = aStarGrid.findPathDiagAlternating(
                        startRow, startCol,
                        endRow, endCol,
                        (r, c) -> true   // oder deine Terrain-Logik
                );

                Arrays.fill(inPath, false);
                for (int p : path) {
                    inPath[p] = true;
                }
            }

            // visual markers (optional)
            Arrays.fill(selected, false);
            if (startIdx != -1) selected[startIdx] = true;
            if (endIdx != -1) selected[endIdx] = true;
        });


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render(g, canvas.getWidth(), canvas.getHeight());
                updateFps(now);
            }
        };
        timer.start();

        stage.setTitle("Square Canvas Stress Test (400x400) - Pan/Zoom/Click/30ft");
        stage.setScene(new Scene(root, 1200, 800, Color.rgb(10, 10, 16)));
        stage.show();
    }

    // state = idx*2 + diagParity (0/1)
    private final int[] dist2 = new int[COLS * ROWS * 2];
    private final int[] prev2 = new int[COLS * ROWS * 2];

    private void computeBestPathPFDiag(int startIdx, int goalIdx) {
        Arrays.fill(inPath, false);
        Arrays.fill(dist2, Integer.MAX_VALUE);
        Arrays.fill(prev2, -1);

        int startState = startIdx * 2; // parity 0
        dist2[startState] = 0;

        PriorityQueue<StateNode> pq = new PriorityQueue<>();
        pq.add(new StateNode(startState, 0));

        while (!pq.isEmpty()) {
            StateNode cur = pq.poll();
            int s = cur.state;
            int d = cur.dist;
            if (d != dist2[s]) continue;

            int idx = s / 2;
            int parity = s & 1;

            if (idx == goalIdx) {
                // optional: early-exit only if this is minimal among both parities
                // (pq order guarantees it)
                break;
            }

            int row = idx / COLS;
            int col = idx - row * COLS;

            // 8 neighbors
            relaxState(pq, s, d, row - 1, col, false); // N
            relaxState(pq, s, d, row - 1, col + 1, true); // NE
            relaxState(pq, s, d, row, col + 1, false); // E
            relaxState(pq, s, d, row + 1, col + 1, true); // SE
            relaxState(pq, s, d, row + 1, col, false); // S
            relaxState(pq, s, d, row + 1, col - 1, true); // SW
            relaxState(pq, s, d, row, col - 1, false); // W
            relaxState(pq, s, d, row - 1, col - 1, true); // NW
        }

        // pick best goal state (parity 0 or 1)
        int goalState0 = goalIdx * 2;
        int goalState1 = goalIdx * 2 + 1;
        int bestGoalState = (dist2[goalState1] < dist2[goalState0]) ? goalState1 : goalState0;

        if (dist2[bestGoalState] == Integer.MAX_VALUE) return;

        // reconstruct
        int s = bestGoalState;
        while (s != -1) {
            int idx = s / 2;
            inPath[idx] = true;
            if (idx == startIdx) break;
            s = prev2[s];
        }
    }

    private void relaxState(PriorityQueue<StateNode> pq, int fromState, int fromDist, int nr, int nc, boolean diagonal) {
        if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) return;

        int fromParity = fromState & 1;
        int nIdx = nr * COLS + nc;

        // PF-style diagonal alternation: 1,2,1,2,... based on parity
        int diagCost = diagonal ? (fromParity == 0 ? 1 : 2) : 1;

        // "every second field costs double" on ENTER
        int enterMult = tileEnterCost(nr, nc); // 1 or 2

        int stepCost = diagCost * enterMult;

        int nParity = diagonal ? (fromParity ^ 1) : fromParity;
        int nState = nIdx * 2 + nParity;

        int nd = fromDist + stepCost;

        if (nd < dist2[nState]) {
            dist2[nState] = nd;
            prev2[nState] = fromState;
            pq.add(new StateNode(nState, nd));
        }
    }

    private static final class StateNode implements Comparable<StateNode> {
        final int state, dist;

        StateNode(int state, int dist) {
            this.state = state;
            this.dist = dist;
        }

        @Override
        public int compareTo(StateNode o) {
            return Integer.compare(this.dist, o.dist);
        }
    }


    private void render(GraphicsContext g, double w, double h) {
        g.setFill(Color.rgb(10, 10, 16));
        g.fillRect(0, 0, w, h);

        double tileScreen = BASE_TILE * zoom;

        // Viewport in world coords
        double worldLeft = camX;
        double worldTop = camY;
        double worldRight = camX + w / zoom;
        double worldBottom = camY + h / zoom;

        // Culling in tile coords (add a small margin)
        int colMin = clamp((int) Math.floor(worldLeft / BASE_TILE) - 2, 0, COLS - 1);
        int colMax = clamp((int) Math.ceil(worldRight / BASE_TILE) + 2, 0, COLS - 1);
        int rowMin = clamp((int) Math.floor(worldTop / BASE_TILE) - 2, 0, ROWS - 1);
        int rowMax = clamp((int) Math.ceil(worldBottom / BASE_TILE) + 2, 0, ROWS - 1);

        // Styles
        g.setLineWidth(1.0);
        g.setStroke(Color.rgb(160, 160, 255, 0.20));

        int drawn = 0;

        for (int row = rowMin; row <= rowMax; row++) {
            double yWorld = row * BASE_TILE;
            double y = (yWorld - camY) * zoom;

            for (int col = colMin; col <= colMax; col++) {
                int idx = row * COLS + col;

                double xWorld = col * BASE_TILE;
                double x = (xWorld - camX) * zoom;

                if (idx == startIdx) {
                    g.setFill(Color.rgb(60, 180, 90, 0.95));     // start
                } else if (idx == endIdx) {
                    g.setFill(Color.rgb(200, 70, 70, 0.95));     // end
                } else if (inPath[idx]) {
                    g.setFill(Color.rgb(200, 200, 80, 0.85));    // path
                } else if (inRange[idx]) {
                    g.setFill(Color.rgb(120, 80, 40, 0.85));     // range
                } else {
                    g.setFill(Color.rgb(28, 28, 40, 0.85));
                }


                // --- HIER rein: Pixel-Snapping + Overdraw gegen "Borders" ---
                double od = 1.0; // 1..2 testen

                double xs = Math.floor(x);
                double ys = Math.floor(y);
                double ts = Math.ceil(tileScreen); // optional, stabilisiert bei Zoom

                g.fillRect(xs, ys, ts + od, ts + od);
                // ------------------------------------------------------------

                drawn++;
            }
        }

        // --- GRID BORDER (breit) ---
        double borderW = 1.5;
        g.setLineWidth(borderW);
        g.setStroke(Color.rgb(160, 160, 255, 0.50));

        // Screen-Koordinaten der sichtbaren Grid-Grenzen
        double x0 = Math.floor(((colMin * BASE_TILE) - camX) * zoom);
        double x1 = Math.floor((((colMax + 1) * BASE_TILE) - camX) * zoom);
        double y0 = Math.floor(((rowMin * BASE_TILE) - camY) * zoom);
        double y1 = Math.floor((((rowMax + 1) * BASE_TILE) - camY) * zoom);

        // Vertikale Linien
        for (int c = colMin; c <= colMax + 1; c++) {
            double xLine = Math.floor(((c * BASE_TILE) - camX) * zoom);
            g.strokeLine(xLine, y0, xLine, y1);
        }

        // Horizontale Linien
        for (int r = rowMin; r <= rowMax + 1; r++) {
            double yLine = Math.floor(((r * BASE_TILE) - camY) * zoom);
            g.strokeLine(x0, yLine, x1, yLine);
        }


        lastDrawn = drawn;

        g.setFill(Color.rgb(230, 230, 255, 0.85));
        g.setFont(Font.font(14));
        g.fillText(
                "FPS: " + String.format("%.1f", fps)
                        + " | Drawn: " + lastDrawn
                        + " | Zoom: " + String.format("%.2f", zoom)
                        + " | Budget: " + BUDGET + " (every 2nd tile costs 2)",
                12, 22
        );
    }

    /**
     * Mark all tiles reachable with movement-cost budget.
     * Rule: entering a tile costs either 1 or 2 depending on parity.
     * Here: (row+col)%2==1 costs 2, else 1.
     */
    private void markRangeBudgeted(int startRow, int startCol, int budget) {
        Arrays.fill(inRange, false);
        Arrays.fill(dist, Integer.MAX_VALUE);

        int startIdx = startRow * COLS + startCol;
        dist[startIdx] = 0;

        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(startIdx, 0, 0, 0));

        while (!pq.isEmpty()) {
            Node cur = pq.poll();
            int idx = cur.idx;
            int d = cur.dist;

            if (d != dist[idx]) continue;     // stale
            if (d > budget) continue;         // over budget, no need to expand

            inRange[idx] = true;

            int row = idx / COLS;
            int col = idx - row * COLS;

            // 4-neighbors (N,E,S,W). If you want diagonals: add them, but then decide how costs work.
            relax(pq, row - 1, col, d, budget);
            relax(pq, row + 1, col, d, budget);
            relax(pq, row, col - 1, d, budget);
            relax(pq, row, col + 1, d, budget);
        }
    }

    private void relax(PriorityQueue<Node> pq, int nr, int nc, int fromDist, int budget) {
        if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) return;

        int nIdx = nr * COLS + nc;

        int stepCost = tileEnterCost(nr, nc); // 1 or 2
        int nd = fromDist + stepCost;
        if (nd > budget) return;

        if (nd < dist[nIdx]) {
            dist[nIdx] = nd;
            pq.add(new Node(nIdx, nd, 0, 0));
        }
    }

    // "Every second tile counts double"
    private int tileEnterCost(int row, int col) {
        return ((row + col) & 1) == 1 ? 2 : 1;
    }

    private double screenToWorldX(double sx, double z) {
        return camX + sx / z;
    }

    private double screenToWorldY(double sy, double z) {
        return camY + sy / z;
    }

    private void updateFps(long now) {
        frames++;
        if (lastFpsTime == 0) lastFpsTime = now;
        long elapsed = now - lastFpsTime;
        if (elapsed >= 1_000_000_000L) {
            fps = frames * 1_000_000_000.0 / elapsed;
            frames = 0;
            lastFpsTime = now;
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static final class Node implements Comparable<Node> {
        final int idx, dist, turns, steps;

        Node(int idx, int dist, int turns, int steps) {
            this.idx = idx;
            this.dist = dist;
            this.turns = turns;
            this.steps = steps;
        }

        @Override
        public int compareTo(Node o) {
            int c = Integer.compare(this.dist, o.dist);
            if (c != 0) return c;
            c = Integer.compare(this.turns, o.turns);
            if (c != 0) return c;
            return Integer.compare(this.steps, o.steps);
        }
    }


    public static void main(String[] args) {
        launch(args);
    }


}
