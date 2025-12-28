package com.fuchsbau.shorin.Engine;

import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.List;

/***
 * Don't make me regret using java .class
 */
public class PerformanceTimer {
    private static final double FRAME_MS = 16.67;
    private static final double WARN_MS = 3 * FRAME_MS;
    private static final double ERROR_MS = 6 * FRAME_MS;

    private final long start = System.nanoTime();
    private long last = start;
    private final List<String> lines = new ArrayList<>();

    public void mark(String label) {
        long now = System.nanoTime();

        double deltaMs = (now - last) / 1_000_000.0;
        double totalMs = (now - start) / 1_000_000.0;

        String line = String.format(
                "%-28s +%7.2f ms   (%8.5f ms)",
                label, deltaMs, totalMs
        );
        lines.add(line);


        if (deltaMs >= ERROR_MS) {
            FileLogger.getLogger().severe(
                    "[WARNUNG SEHR LANGES LADEN] " + label + " took " + deltaMs + " ms"
            );
        } else if (deltaMs >= WARN_MS) {
            FileLogger.getLogger().warning(
                    "[WARNUNG LANGES LADEN] " + label + " took " + deltaMs + " ms"
            );
        }

        last = now;
    }

    public String report() {
        return "\n" + String.join("\n", lines);
    }
}
