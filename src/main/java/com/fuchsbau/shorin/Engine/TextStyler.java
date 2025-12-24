package com.fuchsbau.shorin.Engine;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextStyler {
    private static final Set<String> GAME_NAMES = Set.of("shorin");

    private static final Set<String> KEYWORDS = Set.of("spirit", "vessel", "dominion", "mortal", "coasts", "ruin");
    private static final Set<String> ADJ_SCARY = Set.of("forsaken", "broken", "uncaring", "ageless", "unseen", "vast");
    private static final Set<String> ADJ_FLUFFY = Set.of("gentle", "warm", "kind", "bright");

    // alle Wörter in ein Pattern: \b(word1|word2|...)\b (case-insensitive)
    private static final Pattern HIGHLIGHT = Pattern.compile(
            "\\b(" + joinAllWords() + ")\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static String joinAllWords() {
        List<String> all = new ArrayList<>(25);
        all.addAll(GAME_NAMES);
        all.addAll(KEYWORDS);
        all.addAll(ADJ_SCARY);
        all.addAll(ADJ_FLUFFY);

        return all.stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
    }

    public static TextFlow buildFast(String text) {
        TextFlow flow = new TextFlow();

        int last = 0;
        Matcher m = HIGHLIGHT.matcher(text);

        while (m.find()) {
            if (m.start() > last) {
                flow.getChildren().add(base(text.substring(last, m.start())));
            }

            String hit = m.group(1);
            flow.getChildren().add(styled(hit));

            last = m.end();
        }

        if (last < text.length()) {
            flow.getChildren().add(base(text.substring(last)));
        }

        return flow;
    }

    private static Text base(String s) {
        Text t = new Text(s);
        t.getStyleClass().add("KEYWORD-base");
        return t;
    }

    private static Text styled(String s) {
        Text t = new Text(s);

        String n = s.toLowerCase(Locale.ROOT).replace("’", "'");

        if (GAME_NAMES.contains(n)) t.getStyleClass().add("KEYWORD-game");
        else if (KEYWORDS.contains(n)) t.getStyleClass().add("KEYWORD-keyword");
        else if (ADJ_SCARY.contains(n)) t.getStyleClass().add("KEYWORD-scary");
        else if (ADJ_FLUFFY.contains(n)) t.getStyleClass().add("KEYWORD-fluffy");
        else t.getStyleClass().add("KEYWORD-base");

        return t;
    }

}
