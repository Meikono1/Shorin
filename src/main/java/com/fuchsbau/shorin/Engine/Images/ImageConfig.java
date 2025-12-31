package com.fuchsbau.shorin.Engine.Images;

public record ImageConfig(
        double widthRatio,
        double heightRatio,
        double fixedWidth,
        double fixedHeight,
        double translateX,
        double translateY
) {
    public static ImageConfig ratio(double w, double h) {
        return new ImageConfig(w, h, 0, 0, 0, 0);
    }

    public static ImageConfig fixed(double w, double h) {
        return new ImageConfig(0, 0, w, h, 0, 0);
    }

    public ImageConfig move(double x, double y) {
        return new ImageConfig(widthRatio, heightRatio, fixedWidth, fixedHeight, x, y);
    }
}
