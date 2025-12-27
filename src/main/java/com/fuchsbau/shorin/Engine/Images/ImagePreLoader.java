package com.fuchsbau.shorin.Engine.Images;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

import java.util.*;

public class ImagePreLoader {
    private static final Map<ImagePaths, Image> CACHE = new EnumMap<>(ImagePaths.class);
    private static final Deque<ImagePaths> QUEUE = new ArrayDeque<>();
    private static boolean running;

    private static void getOrRequest(ImagePaths type, String url) {
        CACHE.computeIfAbsent(type, t -> new Image(url, true));
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

                String url = Objects.requireNonNull(
                        ImagePreLoader.class.getResource(next.getImagePath())
                ).toExternalForm();

                getOrRequest(next, url);
            }
        }.start();
    }

    public static Image getCached(ImagePaths path) {
        return CACHE.get(path);
    }

    public static boolean isLoaded(ImagePaths type) {
        Image img = CACHE.get(type);
        return img != null && img.getProgress() >= 1.0;
    }
}
