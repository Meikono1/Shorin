package com.fuchsbau.shorin.Engine.Images;

import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;

import java.util.*;

public class ImagePreLoader {
    private static final Map<ImagePaths, Image> CACHE = new EnumMap<>(ImagePaths.class);
    private static final Deque<ImagePaths> QUEUE = new ArrayDeque<>();
    private static boolean running;

    public static Image getOrRequest(ImagePaths type, String url, Runnable onLoaded) {
        Image img = CACHE.computeIfAbsent(type, t -> new Image(url, true));

        if (onLoaded != null && (img.getProgress() < 1.0 || img.isError())) {
            attachLoadListener(img, onLoaded);
        }

        return img;
    }

    private static void attachLoadListener(Image img, Runnable onLoaded) {
        if (onLoaded == null) return;

        img.progressProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() >= 1.0 && !img.isError()) onLoaded.run();
        });
    }

    public static Image getOrRequest(ImagePaths type, String url) {
        return getOrRequest(type, url, null);
    }

    public static void warmUpAll() {
        if (running) return;

        QUEUE.clear();
        QUEUE.addAll(Arrays.asList(ImagePaths.values()));
        running = true;

        // startet pro Frame maximal 1 neues Image, nur wenn FPS ok ist
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
