package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

public class TextSegment {

    public final String text;
    public final SegmentStyle style;
    public final String speakerName;
    public final String speakerImage;
    public final boolean speakerLeft;
    public final long delayMs;

    private TextSegment(TextBuilder b) {
        this.text = b.text;
        this.style = b.style;
        this.speakerName = b.speakerName;
        this.speakerImage = b.speakerImage;
        this.speakerLeft = b.speakerLeft;
        this.delayMs = b.delayMs;
    }

    public static TextBuilder narration(String text) {
        return new TextBuilder(text, SegmentStyle.NARRATION);
    }

    public static TextBuilder dialog(String speaker, String text) {
        return new TextBuilder(text, SegmentStyle.DIALOG).speaker(speaker);
    }

    public static TextBuilder system(String text) {
        return new TextBuilder(text, SegmentStyle.SYSTEM);
    }
}
