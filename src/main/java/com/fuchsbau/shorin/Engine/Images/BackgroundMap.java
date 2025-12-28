package com.fuchsbau.shorin.Engine.Images;

import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.beans.value.ChangeListener;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static com.fuchsbau.shorin.Engine.Images.ImagePreLoader.BASE64_PNG;


public class BackgroundMap {
    public ImageView getBackgroundImage(ImagePaths path, double factor, double darkness) {
        if (!ImagePreLoader.isLoaded(path)) {
            return getDirectImage(path, factor, darkness);
        }

        ImageView bg = new ImageView(ImagePreLoader.getCached(path));
        bg.setPreserveRatio(true);
        bg.setSmooth(true);

        bg.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ChangeListener<Number> resizeListener =
                        (o, oldVal, newVal) -> resizeBackground(bg, newScene.getWidth(), newScene.getHeight(), factor);
                newScene.widthProperty().addListener(resizeListener);
                newScene.heightProperty().addListener(resizeListener);
                resizeBackground(bg, newScene.getWidth(), newScene.getHeight(), factor);
            }
        });

        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-darkness);
        bg.setEffect(adjust);

        return bg;
    }

    public ImageView getDirectImage(ImagePaths path, double factor, double darkness) {
        FileLogger.getLogger().info("Direct Image ?");
        String urlOrNull = path.resolveUrlOrNull();
        if (urlOrNull==null){
            return new ImageView(new Image(BASE64_PNG));
        }

        ImageView bg = new ImageView(new Image(urlOrNull));
        bg.setPreserveRatio(true);
        bg.setSmooth(true);

        bg.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                ChangeListener<Number> resizeListener =
                        (o, oldVal, newVal) -> resizeBackground(bg, newScene.getWidth(), newScene.getHeight(), factor);
                newScene.widthProperty().addListener(resizeListener);
                newScene.heightProperty().addListener(resizeListener);
                resizeBackground(bg, newScene.getWidth(), newScene.getHeight(), factor);
            }
        });

        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(-darkness);
        bg.setEffect(adjust);
        return bg;
    }

    private void resizeBackground(ImageView bg, double sceneW, double sceneH, double factor) {
        Image img = bg.getImage();
        if (img == null || sceneW <= 0 || sceneH <= 0) return;

        double imgW = img.getWidth();
        double imgH = img.getHeight();

        double scale = Math.max(sceneW / imgW, sceneH / imgH);
        scale *= factor;

        bg.setFitWidth(imgW * scale);
        bg.setFitHeight(imgH * scale);
    }
}
