package com.fuchsbau.shorin.Engine.Map.Core.Sound;

public class SoundPoint {
    public double x, y;
    public int radiusTiles = 5;
    public String soundPath = "";
    public boolean loop = true;
    public float volume = 1.0f;
    public float easing = 0.0f;
    public boolean constrainByWall = false;
    public boolean requiresLight = false;
    public float minLightLevel = 0.0f; // 0.0 - 1.0

    public SoundPoint() {
    }
}