package com.fuchsbau.shorin.Engine.Images;

import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

import java.util.*;
import java.util.logging.Logger;

public class ImagePreLoader {
    protected static final String BASE64_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAFUlEQVR4Xu3BAQ0AAADCIPunNsN+YAAAAABJRU5ErkJggg==";
    private static final Map<String, CacheEntry> CACHE = new HashMap<>();
    private static final Deque<ImagePaths> QUEUE = new ArrayDeque<>();
    private static boolean running;
    private static final Logger logger = FileLogger.getLogger();

    private static void getOrRequest(String key, String url) {
        CACHE.computeIfAbsent(key, k -> CacheEntry.ok(createImage(url)));
    }

    private static Image createImage(String url) {
        return new Image(url, true);
    }

    private static Image createImage(ImagePaths path, String url) {
        ImageConfig c = path.getConfig();

        if (c != null && (c.fixedWidth() > 0 || c.fixedHeight() > 0)) {
            return new Image(
                    url,
                    c.fixedWidth() > 0 ? c.fixedWidth() : 0,
                    c.fixedHeight() > 0 ? c.fixedHeight() : 0,
                    true,
                    true,
                    true
            );
        }

        return new Image(url, true);
    }

    public static void warmUpAll() {
        if (running) return;

        QUEUE.clear();
        QUEUE.addAll(Arrays.asList(ImagePaths.values()));
        running = true;

        // startet pro Frame maximal 1 neues Image, wenn FPS ok sind
        new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last != 0) {
                    double dtMs = (now - last) / 1_000_000.0;

                    if (dtMs > 18) { // ~55 FPS Grenze
                        last = now;
                        return;
                    }
                }
                last = now;

                ImagePaths next = QUEUE.pollFirst();
                if (next == null) {
                    stop();
                    running = false;
                    return;
                }

                CacheEntry e = CACHE.get(next);
                if (e != null) return;

                String url = next.resolveUrlOrNull();
                if (url == null) {
                    CACHE.put(next.relative(), CacheEntry.missing());
                    return;
                }

                getOrRequest(next.relative(), url);
            }
        }.start();
    }

    public static Image getCached(ImagePaths path) {
        CacheEntry cached = CACHE.get(path);
        if (cached != null) {
            return cached.missing ? null : cached.image;
        }

        String url = path.resolveUrlOrNull();
        if (url == null) {
            logger.warning("Bild fehlt: " + path.relative());
            CACHE.put(path.relative(), CacheEntry.missing());
            return fallBackImage();
        }
        Image img;
        if (path.getConfig() != null) {
            img = new Image(url, path.getConfig().fixedHeight(), path.getConfig().fixedWidth(), true, true);
        } else {
            img = new Image(url, false);
        }

        if (img.isError()) {
            logger.warning("Bild konnte nicht geladen werden: " + path.relative());
            CACHE.put(path.relative(), CacheEntry.missing());
            return fallBackImage();
        }

        CACHE.put(path.relative(), CacheEntry.ok(img));
        return img;
    }

    public static Image getCached(String relativePath) {
        CacheEntry cached = CACHE.get(relativePath);
        if (cached != null) return cached.missing ? fallBackImage() : cached.image;

        String url = PathResolver.resolveString(relativePath);
        if (url == null) {
            logger.warning("Bild fehlt: " + relativePath);
            CACHE.put(relativePath, CacheEntry.missing());
            return fallBackImage();
        }

        getOrRequest(relativePath, url);
        return CACHE.get(relativePath).image;
    }

    private static Image fallBackImage() {
        CacheEntry entry = CACHE.get(ImagePaths.MISSING);

        if (entry.missing) {
            logger.severe("Keine Bilder? Infrasturktor oder User Error?");
            return new Image(BASE64_PNG);
        } else {
            return entry.image;
        }
    }

    public static boolean isLoaded(ImagePaths key) {
        CacheEntry entry = CACHE.get(key);
        // nie angefragt
        if (entry == null) return false;
        // fehlend/kaputt
        if (entry.missing) return false;

        Image img = entry.image;
        return img != null && img.getProgress() >= 1.0 && !img.isError();
    }
}
