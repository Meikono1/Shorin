package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;

public class TextBuilder {
    final String text;
    final TextSegment.Style style;
    String speakerName = null;
    ImagePaths speakerImage = null;
    Boolean speakerLeft = null;
    long delayMs = 0;

    TextBuilder(String text, TextSegment.Style style) {
        this.text = text;
        this.style = style;
    }

    public TextBuilder speaker(String name) {
        this.speakerName = name;
        return this;
    }

    public TextBuilder image(ImagePaths path) {
        this.speakerImage = path;
        return this;
    }

    public TextBuilder side(boolean left) {
        this.speakerLeft = left;
        return this;
    }

    public TextBuilder delay(long ms) {
        this.delayMs = ms;
        return this;
    }

    public TextSegment build() {
        return new TextSegment(this);
    }
}
