package com.fuchsbau.shorin.Races;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Logger.FileLogger;
import com.fuchsbau.shorin.Main;
import com.fuchsbau.shorin.Engine.Race.Size;
import com.fuchsbau.shorin.Races.Base.*;
import javafx.animation.AnimationTimer;

import java.lang.reflect.Constructor;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public final class RaceLoader {

    private final static ArrayDeque<Path> queue = new ArrayDeque<>();
    private final static Map<String, Race> cachedRaces = new HashMap<>();
    private static boolean running;
    private static final Logger logger = FileLogger.getLogger();

    public static Race getCachedRace(String race) {
        return cachedRaces.get(race);
    }

    public static List<Race> getCachedRaces(Set<String> races) {
        List<Race> ret = new ArrayList<>(races.size());
        for (String race : races) {
            if (cachedRaces.get(race) != null) {
                ret.add(cachedRaces.get(race));
            } else {
                RacesEnumToClass saveParse = RacesEnumToClass.safeParse(race);
                if (cachedRaces.get(saveParse.name()) != null) {
                    ret.add(cachedRaces.get(saveParse.name()));
                }
            }

        }
        return ret;
    }

    private static Race loadSingleRace(Path file) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(Files.newBufferedReader(file));

        String id = text(root, "id", file.getFileName().toString());
        String key = text(root, "class", id).toUpperCase(Locale.ROOT);
        String name = text(root, "name", id);
        String size = text(root, "size", "medium");
        byte speed = jsonByte(root, "speed", (byte) 20);
        int health = jsonInt(root, "health", 6);
        int adultAge = jsonInt(root, "adultAge", 18);
        int grownAge = jsonInt(root, "grownAge", 30);
        if (grownAge < adultAge) {
            grownAge = adultAge;
        }

        int lifeExpectancy = jsonInt(root, "lifeExpectancy", 50);
        if (lifeExpectancy < grownAge) {
            lifeExpectancy = grownAge;
        }

        String grownSize = text(root, "grownSize", "SMALL");

        Age ageModel = new Age(0, adultAge, grownAge, lifeExpectancy);
        Attributes attrs = readAttributes(root.path("attributes"));

        RacesEnumToClass e = RacesEnumToClass.valueOf(key);
        Class<? extends Race> type = e.raceClass;

        Constructor<? extends Race> ctor = getCtor(type);

        return ctor.newInstance(
                id,
                name,
                speed,
                attrs,
                health,
                Size.valueOf(size.toUpperCase()),
                ageModel,
                null,
                null
        );
    }

    private static Constructor<? extends Race> getCtor(Class<? extends Race> type) {
        try {
            return type.getConstructor(
                    String.class,
                    String.class,
                    byte.class,
                    Attributes.class,
                    int.class,
                    Size.class,
                    LifeStages.class,
                    Reproduction.class,
                    Appearance.class
            );
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Missing Race ctor: " + type.getName(), e);
        }
    }

    private static String text(JsonNode node, String field, String def) {
        JsonNode v = node.get(field);
        return (v != null && v.isTextual()) ? v.asText() : def;
    }

    private static byte jsonByte(JsonNode node, String field, byte def) {
        JsonNode v = node.get(field);
        if (v != null && v.isNumber()) {
            return (byte) v.asInt();
        }
        if (v != null && v.isTextual()) {
            try {
                return Byte.parseByte(v.asText());
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static int jsonInt(JsonNode node, String field, int def) {
        JsonNode v = node.get(field);
        if (v != null && v.isNumber()) {
            return v.asInt();
        }
        if (v != null && v.isTextual()) {
            try {
                return Integer.parseInt(v.asText());
            } catch (NumberFormatException ignored) {
            }
        }
        return def;
    }

    private static Attributes readAttributes(JsonNode node) {
        // minimal & robust: fehlende Felder = 0
        int str = node.path("str").asInt(0);
        int dex = node.path("dex").asInt(0);
        int con = node.path("con").asInt(0);
        int intel = node.path("int").asInt(0);
        int wis = node.path("wis").asInt(0);
        int cha = node.path("cha").asInt(0);

        return new Attributes(str, dex, con, intel, wis, cha);
    }

    public static Path baseDir() {
        Path wd = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        if (Files.isDirectory(wd.resolve("races"))) {
            return wd;
        }

        try {
            Path p = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            return Files.isDirectory(p) ? p : p.getParent();
        } catch (Exception e) {
            logger.warning("Bild nicht gefunden");
            return wd;
        }
    }

    private static Path racesFolder() {
        Path base = baseDir();

        Path external = base.resolve("races");
        if (Files.isDirectory(external)) return external;

        Path dev = Paths.get("src", "main", "resources", "races");
        if (Files.isDirectory(dev)) return dev;

        return external;
    }

    public static void warmUpAll() {
        if (running) return;

        queue.clear();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(racesFolder(), "*.json")) {
            for (Path p : ds) {
                if (Files.isRegularFile(p)) {
                    queue.addLast(p);
                }
            }
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }

        running = true;

        // startet pro Frame maximal 1 import, wenn FPS ok sind
        new AnimationTimer() {
            long last = 0;

            @Override
            public void handle(long now) {
                if (last != 0) {
                    double dtMs = (now - last) / 1_000_000.0;

                    if (dtMs > 18) { // ~55 FPS Grenze
                        last = now;
                        return;
                    }
                }
                last = now;

                Path next = queue.pollFirst();
                if (next == null) {
                    stop();
                    running = false;
                    return;
                }
                try {
                    Race race = loadSingleRace(next);
                    cachedRaces.put(race.raceName(), race);
                } catch (Exception e) {
                    logger.severe(e.getMessage());
                }

            }
        }.start();
    }
}