package com.fuchsbau.shorin.Engine.Dice;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

import java.util.List;

/**
 * @param vertices       Geometrie
 * @param faceValues     Face → Wert
 * @param boundingRadius Bounding (für einfache Kollision / Skalierung)
 * @param specialCase    Sonderfälle (z. B. D2)
 */
public record DiceDefinition(DiceType type, List<Vec3> vertices, List<int[]> faces, int[] faceValues,
                             List<Vec3> faceNormals, double boundingRadius, boolean specialCase) {

    public int getFaceValue(int faceIndex) {
        return faceValues[faceIndex];
    }

    public int getFaceCount() {
        return faces.size();
    }
}