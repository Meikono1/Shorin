package com.fuchsbau.shorin.Engine.Images;

import javafx.scene.image.Image;

class CacheEntry {
    final Image image;     // null wenn missing
    final boolean missing;

    private CacheEntry(Image image, boolean missing) {
        this.image = image;
        this.missing = missing;
    }

    static CacheEntry ok(Image img) {
        return new CacheEntry(img, false);
    }

    static CacheEntry missing() {
        return new CacheEntry(null, true);
    }
}
