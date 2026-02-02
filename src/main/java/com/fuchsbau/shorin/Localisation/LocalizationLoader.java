package com.fuchsbau.shorin.Localisation;

import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class LocalizationLoader {
    private final Logger logger = FileLogger.getLogger();
    private static String fileNameDevider = "#";

    public enum TextKey {
        UI_MAIN_START("main#intro"),
        UI_MAIN_OPTIONS("main#options_button");

        private final String key;
        private final String translation;

        TextKey(String text) {
            this.key = text.split(fileNameDevider)[1];
            this.translation = "Not loaded: " + text;
        }

        public String getKey() {
            return key;
        }

        public String getTranslation() {
            return translation;
        }
    }

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String get(TextKey key) {
        if (this.cache.isEmpty()) {
            reloadCache();
        }

        String text = cache.get(key.getKey());

        if (text == null) {
            logger.warning("Text nicht geladen: " + key);
        }

        return text;
    }

    private void reloadCache() {





    }
}
