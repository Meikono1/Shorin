package com.fuchsbau.shorin.Engine;

import java.util.ArrayList;
import java.util.List;

/***
 * Don't make me regret using java .class
 */
public class PerformanceTimer {
    private final long start = System.nanoTime();
    private long last = start;
    private final List<String> lines = new ArrayList<>();

    public void mark(String label) {
        long now = System.nanoTime();
        long deltaMs = (now - last) / 1_000_000;
        long totalMs = (now - start) / 1_000_000;
        lines.add(String.format("%-28s +%4d ms   (%4d ms)", label, deltaMs, totalMs));
        last = now;
    }

    public String report() {
        return String.join("\n", lines);
    }
}
