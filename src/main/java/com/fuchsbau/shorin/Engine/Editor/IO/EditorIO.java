package com.fuchsbau.shorin.Engine.Editor.IO;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;

public class EditorIO {
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final File BASE = new File("Engine-Data");


    public static <T> void save(String filename, T data) throws IOException {
        BASE.mkdirs();
        mapper.writeValue(new File(BASE, filename), data);
    }


    public static <T> T load(String filename, TypeReference<T> type, T fallback) {
        File file = new File(BASE, filename);
        if (!file.exists()) return fallback;
        try {
            return mapper.readValue(file, type);
        } catch (IOException ex) {
            return fallback;
        }
    }
}