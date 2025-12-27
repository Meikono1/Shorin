package com.fuchsbau.shorin.Engine.Options;

import javafx.scene.paint.Color;

public class StyleOptions {

    // Font Text
    public static String fontFamily = "Eczar";
    public static int baseFontSize = 16;
    public static int largeFontSize = 26;
    public static String largeFontWeight = "bold";
    public static int keywordFontSize = 21;
    public static int menuButtonFontSize = 16;

    // Menu
    public static String menuBg = "rgba(12, 12, 18, 0.6)";
    public static String menuText = "#e0e0e0";
    public static String menuBorder = "#505060";
    public static String menuHoverBg = "rgba(40, 40, 60, 0.9)";
    public static String menuHoverBorder = "#a0a0ff";

    // Text Farben
    public static String keywordBase = "#e0e0e0";
    public static String keywordGame = "#a0a0ff";
    public static String keywordKeyword = "#d8d8ff";
    public static String keywordScary = "#ffb0b0";
    public static String keywordFluffy = "#bfffd7";


    // ===== Menu Buttons =====
    public static class MenuButton {
        public static final Color BG = Color.rgb(12, 12, 18, 0.6);
        public static final Color TEXT = Color.web("#e0e0e0");
        public static final double FONT_SIZE = 16;
        public static final boolean BOLD = true;

        public static final double PAD_TOP = 10;
        public static final double PAD_RIGHT = 18;
        public static final double PAD_BOTTOM = 10;
        public static final double PAD_LEFT = 18;

        public static final double RADIUS = 6;

        public static final Color BORDER = Color.web("#505060");
        public static final double BORDER_WIDTH = 1;

        // Hover
        public static final Color BG_HOVER = Color.rgb(40, 40, 60, 0.9);
        public static final Color BORDER_HOVER = Color.web("#a0a0ff");

        // Pressed
        public static final Color BG_PRESSED = Color.rgb(8, 8, 12, 0.9);
        public static final double PRESSED_TRANSLATE_Y = 1;

        // Focus effect
        public static final Color FOCUS_SHADOW = Color.rgb(160, 160, 255, 0.6);
        public static final int FOCUS_SHADOW_RADIUS = 10;
        public static final double FOCUS_SHADOW_SPREAD = 0.2;
    }

    // ===== ScrollPane =====
    public static final class ScrollPaneStyle {
        public static final String CLASS = "scrollPane";

        public static final double V_SCROLL_WIDTH = 6;

        public static final Color TRACK = Color.rgb(0, 0, 0, 0.25);
        public static final double TRACK_RADIUS = 4;

        public static final Color THUMB = Color.rgb(160, 160, 255, 0.6);
        public static final double THUMB_RADIUS = 4;
    }

    // ===== Keywords (TextFlow/Text) =====
    public static final class KeywordText {
        public static final double FONT_SIZE = 21;

        public static final String BASE = "KEYWORD-base";
        public static final String GAME = "KEYWORD-game";
        public static final String KEYWORD = "KEYWORD-keyword";
        public static final String SCARY = "KEYWORD-scary";
        public static final String FLUFFY = "KEYWORD-fluffy";

        public static final Color BASE_FILL = Color.web("#e0e0e0");
        public static final Color GAME_FILL = Color.web("#a0a0ff");
        public static final Color KEYWORD_FILL = Color.web("#d8d8ff");
        public static final Color SCARY_FILL = Color.web("#ffb0b0");
        public static final Color FLUFFY_FILL = Color.web("#bfffd7");

        public static final Color GAME_SHADOW = Color.rgb(160, 160, 255, 0.55);
        public static final int GAME_SHADOW_RADIUS = 10;
        public static final double GAME_SHADOW_SPREAD = 0.2;

        public static final Color SCARY_SHADOW = Color.rgb(255, 120, 120, 0.35);
        public static final int SCARY_SHADOW_RADIUS = 8;
        public static final double SCARY_SHADOW_SPREAD = 0.2;

        public static final Color FLUFFY_SHADOW = Color.rgb(120, 255, 180, 0.25);
        public static final int FLUFFY_SHADOW_RADIUS = 8;
        public static final double FLUFFY_SHADOW_SPREAD = 0.2;
    }

    public static String buildCss() {
        return """ 
                .root {
                    -fx-font-family: '%s';
                    -fx-font-size: %spx;
                }
                                
                .menu-button {
                    -fx-background-color: %s;
                    -fx-text-fill: %s;
                    -fx-font-size: %spx;
                    -fx-font-weight: bold;
                    -fx-padding: 10 18 10 18;
                    -fx-background-radius: 6;
                    -fx-border-radius: 6;
                    -fx-border-color: %s;
                    -fx-border-width: 1;
                }
                                
                .menu-button:hover {
                    -fx-background-color: %s;
                    -fx-border-color: %s;
                }
                                
                .menu-button:pressed {
                    -fx-background-color: rgba(8, 8, 12, 0.9);
                    -fx-translate-y: 1;
                }
                                
                .menu-button:focused {
                    -fx-effect: dropshadow(gaussian, rgba(160,160,255,0.6), 10, 0.2, 0, 0);
                }
                
                .scrollPane {
                    -fx-background-color: transparent;
                }
                                
                .scrollPane .viewport {
                    -fx-background-color: transparent;
                }
                                
                .scrollPane .scroll-bar:vertical {
                    -fx-pref-width: 6px;
                    -fx-background-color: transparent;
                }
                                
                .scrollPane .scroll-bar:vertical .track {
                    -fx-background-color: rgba(0,0,0,0.25);
                    -fx-background-radius: 4;
                }
                                
                .scrollPane .scroll-bar:vertical .thumb {
                    -fx-background-color: rgba(160,160,255,0.6);
                    -fx-background-radius: 4;
                }
                                
                .scrollPane .scrollPane:vertical .increment-button,
                .scrollPane .scrollPane:vertical .decrement-button {
                    -fx-padding: 0;
                    -fx-opacity: 0;
                }
                   
                .KEYWORD-base {
                    -fx-font-size: %spx;
                    -fx-fill: %s;
                }
                
                .KEYWORD-game {
                    -fx-font-size: %spx;
                    -fx-fill: %s;
                    -fx-font-weight: 700;
                    -fx-effect: dropshadow(gaussian, rgba(160,160,255,0.55), 10, 0.2, 0, 0);
                }
                                
                .KEYWORD-keyword {
                    -fx-font-size: %spx;
                    -fx-fill: %s; 
                    -fx-font-weight: 600;
                }
                                
                .KEYWORD-scary {
                    -fx-font-size: %spx;
                    -fx-fill: %s;
                    -fx-effect: dropshadow(gaussian, rgba(255,120,120,0.35), 8, 0.2, 0, 0);
                }
                                
                .KEYWORD-fluffy {
                    -fx-font-size: %spx;
                    -fx-fill: %s;
                    -fx-effect: dropshadow(gaussian, rgba(120,255,180,0.25), 8, 0.2, 0, 0);
                }
                """.formatted(
                StyleOptions.fontFamily,
                StyleOptions.baseFontSize,
                StyleOptions.menuBg,
                StyleOptions.menuText,
                StyleOptions.menuButtonFontSize,
                StyleOptions.menuBorder,
                StyleOptions.menuHoverBg,
                StyleOptions.menuHoverBorder,
                StyleOptions.keywordFontSize,
                StyleOptions.keywordBase,
                StyleOptions.keywordFontSize,
                StyleOptions.keywordGame,
                StyleOptions.keywordFontSize,
                StyleOptions.keywordKeyword,
                StyleOptions.keywordFontSize,
                StyleOptions.keywordScary,
                StyleOptions.keywordFontSize,
                StyleOptions.keywordFluffy);
    }
}
