package com.fuchsbau.shorin.Engine.Images;

public enum ImagePaths {
    SHORIN_PAPER_MAP("/images/welcomePage/ShorinMap.png"),
    SHORIN_CLEAN_MAP("/images/world/CleanWorld.png");

    private final String path;

    ImagePaths(String path) {
        this.path = path;
    }

    public String getImagePath() {
        return path;
    }
}
