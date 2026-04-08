package com.fuchsbau.shorin.Engine.Map.Core.Lighting;

import com.fuchsbau.shorin.Engine.Map.Core.Tiles.GameMap;
import com.fuchsbau.shorin.Engine.Map.Core.Tiles.Tile;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallSegment;
import com.fuchsbau.shorin.Engine.Map.Core.Walls.WallType;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.fuchsbau.shorin.Engine.Options.GameOptions.BASE_TILE;
import static com.fuchsbau.shorin.Engine.Util.MathUtil.clamp;

public class LightMask {

    private Logger logger = FileLogger.getLogger();

    // --- Licht ---
    private final Canvas lightCanvas;
    // --- Farben ---
    private Canvas tintCanvas;

    public LightMask(double w, double h) {
        lightCanvas = new Canvas(w, h);

        tintCanvas = new Canvas(w, h);
        tintCanvas.setMouseTransparent(true);
        tintCanvas.setPickOnBounds(false);
        tintCanvas.setBlendMode(BlendMode.MULTIPLY);
    }

    public Canvas getLightCanvas() {
        return lightCanvas;
    }

    public void resize(double w, double h) {
        lightCanvas.setWidth(w);
        lightCanvas.setHeight(h);

        tintCanvas.setHeight(h);
        tintCanvas.setWidth(w);
    }

    private record LightPolygonData(LightSource light, double[] polygon,
                                    double sx, double sy, double dimR,
                                    double brightR, float effectiveIntensity) {
    }

    public void update(List<LightSource> lights, GameMap gameMap,
                       float sunDeg, double camX, double camY, double zoom,
                       List<WallSegment> walls) {
        double w = lightCanvas.getWidth();
        double h = lightCanvas.getHeight();

        GraphicsContext lightContext = lightCanvas.getGraphicsContext2D();
        lightContext.clearRect(0, 0, w, h);

        GraphicsContext tintContext = tintCanvas.getGraphicsContext2D();
        tintContext.clearRect(0, 0, w, h);

        // --- Tageslicht bleibt unverändert ---

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

                    lightContext.setFill(Color.color(1, 1, 1, clamp(sunDeg, 0.0, 1.0)));
                    lightContext.fillRect(tx, ty, step + 1, step + 1);
                }
            }
        }

        // --- Polygone einmal berechnen ---
        List<LightPolygonData> computed = new ArrayList<>();

        for (LightSource ls : lights) {
            if (ls.sunlight && sunDeg <= 0f) continue;
            float effectiveIntensity = ls.sunlight ? ls.intensity * sunDeg : ls.intensity;

            double sx = (ls.x - camX) * zoom;
            double sy = (ls.y - camY) * zoom;
            double dimR = ls.dimTiles * BASE_TILE * zoom;
            double brightR = ls.brightTiles * BASE_TILE * zoom;

            if (dimR <= 0) continue;
            if (sx + dimR < 0 || sx - dimR > w || sy + dimR < 0 || sy - dimR > h) continue;

            double[] polygon = buildVisibilityPolygon(
                    ls.x, ls.y, ls.dimTiles * BASE_TILE,
                    walls, camX, camY, zoom);

            computed.add(new LightPolygonData(ls, polygon, sx, sy, dimR, brightR, effectiveIntensity));
            logger.finest("Polygon berechnet: " + ls.label + " — " + (polygon.length / 2) + " Punkte");
        }

        logger.fine("Polygone berechnet: " + computed.size() + " Lichtquellen");

        // --- Light-Layer zeichnen ---
        renderLightLayer(lightContext, computed);

        // --- Tint-Layer zeichnen ---
        renderTintLayer(tintContext, computed);
    }

    private void renderLightLayer(GraphicsContext g, java.util.List<LightPolygonData> computed) {
        for (LightPolygonData data : computed) {
            double brightFraction = clamp(data.brightR() / data.dimR(), 0.0, 1.0);
            double dimStart = clamp(brightFraction + 0.001, 0.0, 1.0);

            RadialGradient light = new RadialGradient(
                    0, 0, data.sx(), data.sy(), data.dimR(), false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(1, 1, 1, data.effectiveIntensity() * 1.0)),
                    new Stop(brightFraction, Color.color(1, 1, 1, data.effectiveIntensity() * 0.8)),
                    new Stop(dimStart, Color.color(1, 1, 1, data.effectiveIntensity() * 0.8)),
                    new Stop(1.0, Color.color(1, 1, 1, data.effectiveIntensity() * 0.2))
            );

            g.save();
            applyPolygonClip(g, data.polygon());
            g.setFill(light);
            g.fillOval(data.sx() - data.dimR(), data.sy() - data.dimR(),
                    data.dimR() * 2, data.dimR() * 2);
            g.restore();
        }
    }

    private void renderTintLayer(GraphicsContext g, java.util.List<LightPolygonData> computed) {
        for (LightPolygonData data : computed) {
            LightSource ls = data.light();
            if (ls.colorR >= 0.99 && ls.colorG >= 0.99 && ls.colorB >= 0.99) continue;

            double brightFraction = clamp(data.brightR() / data.dimR(), 0.0, 1.0);
            double dimStart = clamp(brightFraction + 0.001, 0.0, 1.0);

            RadialGradient tint = new RadialGradient(
                    0, 0, data.sx(), data.sy(), data.dimR(), false, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.color(ls.colorR, ls.colorG, ls.colorB, data.effectiveIntensity())),
                    new Stop(brightFraction, Color.color(ls.colorR, ls.colorG, ls.colorB, data.effectiveIntensity())),
                    new Stop(dimStart, Color.color(ls.colorR, ls.colorG, ls.colorB, data.effectiveIntensity())),
                    new Stop(1.0, Color.color(ls.colorR, ls.colorG, ls.colorB, 0.0))
            );

            g.save();
            applyPolygonClip(g, data.polygon());
            g.setFill(tint);
            g.fillOval(data.sx() - data.dimR(), data.sy() - data.dimR(),
                    data.dimR() * 2, data.dimR() * 2);
            g.restore();
        }
    }

    private void applyPolygonClip(GraphicsContext g, double[] polygon) {
        if (polygon == null || polygon.length < 6) return;
        g.beginPath();
        g.moveTo(polygon[0], polygon[1]);
        for (int i = 2; i < polygon.length; i += 2) {
            g.lineTo(polygon[i], polygon[i + 1]);
        }
        g.closePath();
        g.clip();
    }

    public double[] buildVisibilityPolygon(
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

    public Canvas getTintCanvas() {
        return tintCanvas;
    }
}