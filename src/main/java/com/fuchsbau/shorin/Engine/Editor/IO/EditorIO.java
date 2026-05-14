package com.fuchsbau.shorin.Engine.Editor.IO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fuchsbau.shorin.Engine.Util.PathResolver;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class EditorIO {
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static File dataDir(String filename) {
        Path file = PathResolver.resolveWritable( filename);
        file.getParent().toFile().mkdirs();
        return file.toFile();
    }

    public static <T> void save(String filename, T data) throws IOException {
        mapper.writeValue(dataDir(filename), data);
    }

    public static <T> T load(String filename, TypeReference<T> type, T fallback) {
        File file = dataDir(filename);
        if (!file.exists()) return fallback;
        try {
            return mapper.readValue(file, type);
        } catch (IOException ex) {
            FileLogger.getLogger().warning("Fehler beim Laden: " + filename + " — " + ex.getMessage());
            return fallback;
        }
    }
}