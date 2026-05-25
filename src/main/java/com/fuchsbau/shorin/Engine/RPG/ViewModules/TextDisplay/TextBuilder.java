package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

public class TextBuilder {
    final String text;
    final SegmentStyle style;
    String speakerName = null;
    String speakerImage = null;
    boolean speakerLeft = true;
    long delayMs = 0;

    TextBuilder(String text, SegmentStyle style) {
        this.text = text;
        this.style = style;
    }

    public TextBuilder speaker(String name) {
        this.speakerName = name;
        return this;
    }

    public TextBuilder image(String path) {
        this.speakerImage = path;
        return this;
    }

    public TextBuilder right() {
        this.speakerLeft = false;
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
