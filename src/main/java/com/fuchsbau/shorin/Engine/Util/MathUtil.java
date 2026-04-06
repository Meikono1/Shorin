package com.fuchsbau.shorin.Engine.Util;

public class MathUtil {

    public static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static double dist2(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return dx * dx + dy * dy;
    }
}
