package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

/**
 * Konfiguration für eine Szene oder einen Abschnitt.
 * Wird an TextAdventureDisplayView übergeben bevor Segmente kommen.
 */
public class TextDisplayConfig {

    public enum ImageMode {
        NONE,        // kein Szenenbild
        BACKGROUND,  // Bild hinter dem Text
        ABOVE        // Bild über dem Text, Text darunter
        // @TODO WRAP — Text fließt um Bild (erfordert Custom-Layout)
    }

    public final double typewriterSpeed; // Zeichen pro Sekunde
    public final String sceneImagePath;  // null = kein Szenenbild
    public final ImageMode imageMode;
    public final boolean showSpeakerImages;

    private TextDisplayConfig(Builder b) {
        this.typewriterSpeed = b.typewriterSpeed;
        this.sceneImagePath = b.sceneImagePath;
        this.imageMode = b.imageMode;
        this.showSpeakerImages = b.showSpeakerImages;
    }

    public static TextDisplayConfig defaults() {
        return new Builder().build();
    }

    public static final class Builder {
        private double typewriterSpeed = 40.0;
        private String sceneImagePath = null;
        private ImageMode imageMode = ImageMode.NONE;
        private boolean showSpeakerImages = false;

        public Builder speed(double cps) {
            this.typewriterSpeed = cps;
            return this;
        }

        public Builder sceneImage(String path) {
            this.sceneImagePath = path;
            return this;
        }

        public Builder imageMode(ImageMode mode) {
            this.imageMode = mode;
            return this;
        }

        public Builder speakerImages() {
            this.showSpeakerImages = true;
            return this;
        }

        public TextDisplayConfig build() {
            return new TextDisplayConfig(this);
        }
    }
}
