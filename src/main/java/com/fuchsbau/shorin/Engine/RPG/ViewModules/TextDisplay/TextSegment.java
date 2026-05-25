package com.fuchsbau.shorin.Engine.RPG.ViewModules.TextDisplay;

import com.fuchsbau.shorin.Engine.Images.ImagePaths;

/**
 * Ein einzelner anzuzeigender Block.
 * speakerLeft steuert Ausrichtung — null bei Narration/System.
 */
public class TextSegment {

    public enum Style {
        NARRATION,  // volle Breite, kursiv, zentriert, kein Rahmen
        DIALOG,     // Rahmen, Portrait, links oder rechts je nach speakerLeft
        SYSTEM      // volle Breite, gedimmt, klein
    }

    public final String text;
    public final Style style;
    public final String speakerName;
    public final ImagePaths speakerImage;
    public final Boolean speakerLeft;  // null = kein Speaker (Narration/System)
    public final long delayMs;

    TextSegment(TextBuilder b) {
        this.text = b.text;
        this.style = b.style;
        this.speakerName = b.speakerName;
        this.speakerImage = b.speakerImage;
        this.speakerLeft = b.speakerLeft;
        this.delayMs = b.delayMs;
    }

    public static TextBuilder narration(String text) {
        return new TextBuilder(text, Style.NARRATION);
    }

    public static TextBuilder dialog(String speaker, String text, boolean speakerLeft) {
        return new TextBuilder(text, Style.DIALOG)
                .speaker(speaker)
                .side(speakerLeft);
    }

    // NPC spricht — rechtsbündig
    public static TextBuilder npc(String speaker, String text) {
        return dialog(speaker, text, false);
    }

    // Spieler spricht — linksbündig
    public static TextBuilder player(String speaker, String text) {
        return dialog(speaker, text, true);
    }

    public static TextBuilder system(String text) {
        return new TextBuilder(text, Style.SYSTEM);
    }
}