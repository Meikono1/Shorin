package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

import com.fuchsbau.shorin.Engine.Map.Core.Tiles.GameMap;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.Tile;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallSegment;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallType;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;

import static com.fuchsbau.shorin.Engine.Options.GameOptions.BASE_TILE;
import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class LightMask {

    private final Canvas canvas;
    private final GraphicsContext gc;

    public LightMask(double w, double h) {
        canvas = new Canvas(w, h);
        gc = canvas.getGraphicsContext2D();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void resize(double w, double h) {
        canvas.setWidth(w);
        canvas.setHeight(h);
    }

    public void update(List<LightSource> lights, GameMap gameMap,
                       float sunDeg, double camX, double camY, double zoom,
                       List<WallSegment> walls) {
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        // Alles transparent → colorView komplett unsichtbar (nur grayView sichtbar)
        gc.clearRect(0, 0, w, h);

        // --- Tageslicht: OUTSIDE-Tiles aufhellen ---
        if (sunDeg > 0f) {
            double worldLeft = camX;
            double worldTop = camY;
            double worldRight = camX + w / zoom;
            double worldBottom = camY + h / zoom;

            int colMin = clamp((int) Math.floor(worldLeft / BASE_TILE) - 1, 0, gameMap.getCols() - 1);
            int colMax = clamp((int) Math.ceil(worldRight / BASE_TILE) + 1, 0, gameMap.getCols() - 1);
            int rowMin = clamp((int) Math.floor(worldTop / BASE_TILE) - 1, 0, gameMap.getRows() - 1);
            int rowMax = clamp((int) Math.ceil(worldBottom / BASE_TILE) + 1, 0, gameMap.getRows() - 1);

            double step = BASE_TILE * zoom;
            double startX = Math.floor(((colMin * BASE_TILE) - camX) * zoom);
            double startY = Math.floor(((rowMin * BASE_TILE) - camY) * zoom);

            for (int r = rowMin; r <= rowMax; r++) {
                double ty = startY + (r - rowMin) * step;
                for (int c = colMin; c <= colMax; c++) {
                    double tx = startX + (c - colMin) * step;

                    Tile tile = gameMap.getTile(r, c);
                    if (!tile.has(Tile.OUTSIDE)) continue;
                    if (gameMap.isIndoor(r, c, BASE_TILE)) continue;

                    gc.setFill(Color.color(1, 1, 1, clamp(sunDeg, 0.0, 1.0)));
                    gc.fillRect(tx, ty, step + 1, step + 1);
                }
            }
        }

        // --- Lichtquellen ---
        for (LightSource ls : lights) {
            if (ls.sunlight && sunDeg <= 0f) continue;
            float effectiveIntensity = ls.sunlight ? ls.intensity * sunDeg : ls.intensity;

            double sx = (ls.x - camX) * zoom;
            double sy = (ls.y - camY) * zoom;
            double dimR = ls.dimTiles * BASE_TILE * zoom;

            if (dimR <= 0) continue;
            if (sx + dimR < 0 || sx - dimR > w || sy + dimR < 0 || sy - dimR > h) continue;

            double brightR = ls.brightTiles * BASE_TILE * zoom;
            double brightFraction = clamp(brightR / dimR, 0.0, 1.0);

            // Sichtbarkeits-Polygon berechnen
            double[] polygon = buildVisibilityPolygon(
                    ls.x, ls.y, ls.dimTiles * BASE_TILE,
                    walls, camX, camY, zoom);

            if (polygon.length < 6) {
                // Fallback: kein Wandeinfluss → einfacher Kreis

                double dimStart = clamp(brightFraction + 0.001, 0.0, 1.0);

                RadialGradient light = new RadialGradient(
                        0, 0, sx, sy, dimR, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.color(1, 1, 1, effectiveIntensity * 1.0)),
                        new Stop(brightFraction, Color.color(1, 1, 1, effectiveIntensity * 0.8)),
                        new Stop(dimStart, Color.color(1, 1, 1, effectiveIntensity * 0.8)),
                        new Stop(1.0, Color.color(1, 1, 1, effectiveIntensity * 0.2))
                );
                gc.setFill(light);
                gc.fillOval(sx - dimR, sy - dimR, dimR * 2, dimR * 2);
                continue;
            }

            // Polygon als Clip setzen, dann Gradient drüber
            gc.save();

            gc.beginPath();
            gc.moveTo(polygon[0], polygon[1]);
            for (int i = 2; i < polygon.length; i += 2) {
                gc.lineTo(polygon[i], polygon[i + 1]);
            }
            gc.closePath();
            gc.clip();


            double dimStart = clamp(brightFraction + 0.001, 0.0, 1.0);

            RadialGradient light = new RadialGradient(
                    0, 0, sx, sy, dimR, false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(1, 1, 1, effectiveIntensity * 1.0)),
                    new Stop(brightFraction, Color.color(1, 1, 1, effectiveIntensity * 0.8)),
                    new Stop(dimStart, Color.color(1, 1, 1, effectiveIntensity * 0.8)),
                    new Stop(1.0, Color.color(1, 1, 1, effectiveIntensity * 0.2))
            );
            gc.setFill(light);
            gc.fillOval(sx - dimR, sy - dimR, dimR * 2, dimR * 2);

            gc.restore();
        }
    }

    private double[] buildVisibilityPolygon(
            double lx, double ly, double radiusPx,
            List<WallSegment> walls,
            double camX, double camY, double zoom) {

        // Strahlen-Winkel: alle Wall-Endpunkte + Kreis-Randsektoren
        List<Double> angles = new ArrayList<>();

        // 32 gleichmäßige Winkel als Basis (Kreis-Rand)
        for (int i = 0; i < 32; i++) {
            angles.add(i * 2 * Math.PI / 32);
        }

        // Winkel zu jedem Wall-Endpunkt im Radius
        for (WallSegment wall : walls) {
            if (wall.type == WallType.INVISIBLE || wall.type == WallType.ETHEREAL) continue;
            if (wall.type == WallType.DOOR && wall.open) continue;

            for (double[] pt : new double[][]{{wall.x1, wall.y1}, {wall.x2, wall.y2}}) {
                double dx = pt[0] - lx;
                double dy = pt[1] - ly;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > radiusPx * 1.5) continue; // außerhalb Radius — skip

                double angle = Math.atan2(dy, dx);

                if (angle < 0) angle += 2 * Math.PI;

                angles.add(angle - 0.0001);
                angles.add(angle);
                angles.add(angle + 0.0001);
            }
        }

        // Sortieren
        angles.sort(Double::compareTo);

        // Pro Winkel: nächster Schnittpunkt
        List<double[]> points = new ArrayList<>();

        for (double angle : angles) {
            double rayDx = Math.cos(angle);
            double rayDy = Math.sin(angle);

            // Strahl-Endpunkt am Radius
            double rayEndX = lx + rayDx * radiusPx;
            double rayEndY = ly + rayDy * radiusPx;

            // Nächsten Wall-Schnittpunkt finden
            double closestT = 1.0;
            for (WallSegment wall : walls) {
                if (wall.type == WallType.INVISIBLE || wall.type == WallType.ETHEREAL) continue;
                if (wall.type == WallType.DOOR && wall.open) continue;

                Double t = raySegmentIntersectT(
                        lx, ly, rayEndX, rayEndY,
                        wall.x1, wall.y1, wall.x2, wall.y2);

                if (t != null && t < closestT) {
                    closestT = t;
                }
            }

            // Trefferpunkt in Screen-Koordinaten
            double hitX = lx + rayDx * radiusPx * closestT;
            double hitY = ly + rayDy * radiusPx * closestT;

            points.add(new double[]{
                    (hitX - camX) * zoom,
                    (hitY - camY) * zoom
            });
        }

        // Flach in double[] umwandeln
        double[] result = new double[points.size() * 2];
        for (int i = 0; i < points.size(); i++) {
            result[i * 2] = points.get(i)[0];
            result[i * 2 + 1] = points.get(i)[1];
        }
        return result;
    }

    // Gibt t zurück (0..1 auf dem Strahl) oder null wenn kein Schnitt
    private Double raySegmentIntersectT(
            double ax, double ay, double bx, double by,
            double cx, double cy, double dx, double dy) {

        double r_dx = bx - ax, r_dy = by - ay;
        double s_dx = dx - cx, s_dy = dy - cy;

        double denom = r_dx * s_dy - r_dy * s_dx;
        if (Math.abs(denom) < 1e-10) return null; // parallel

        double t = ((cx - ax) * s_dy - (cy - ay) * s_dx) / denom;
        double u = ((cx - ax) * r_dy - (cy - ay) * r_dx) / denom;

        if (t > 1e-6 && t <= 1.0 && u >= 0.0 && u <= 1.0) {
            return t;
        }
        return null;
    }
}