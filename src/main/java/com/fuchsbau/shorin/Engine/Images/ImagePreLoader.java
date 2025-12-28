package com.fuchsbau.shorin.Engine.Images;

import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

import java.util.*;
import java.util.logging.Logger;

public class ImagePreLoader {
    protected static final String BASE64_PNG =
            "iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAAAFUlEQVR4Xu3BAQ0AAADCIPunNsN+YAAAAABJRU5ErkJggg==";
    private static final Map<ImagePaths, CacheEntry> CACHE = new EnumMap<>(ImagePaths.class);
    private static final Deque<ImagePaths> QUEUE = new ArrayDeque<>();
    private static boolean running;
    private static final Logger logger = FileLogger.getLogger();

    private static void getOrRequest(ImagePaths type, String url) {
        CACHE.computeIfAbsent(type, t -> CacheEntry.ok(new Image(url, true)));
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
                    CACHE.put(next, CacheEntry.missing());
                    return;
                }

                getOrRequest(next, url);
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
            CACHE.put(path, CacheEntry.missing());
            return new Image(BASE64_PNG);
        }

        Image img = new Image(url, false);

        if (img.isError()) {
            logger.warning("Bild konnte nicht geladen werden: " + path.relative());
            CACHE.put(path, CacheEntry.missing());
            return new Image(BASE64_PNG);
        }

        CACHE.put(path, CacheEntry.ok(img));
        return img;
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
