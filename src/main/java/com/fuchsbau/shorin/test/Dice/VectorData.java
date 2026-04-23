package com.fuchsbau.shorin.test.Dice;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class VectorData {
    public String type;     // Würfeltyp z.B. "d6"
    public Vec3 pos;        // Startposition
    public Vec3 velocity;   // Startgeschwindigkeit
    public Vec3 angle;      // Startwinkel (angularVelocity)
    public AxisData axis;   // Rotationsachse + Winkel
}
