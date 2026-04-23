package com.fuchsbau.shorin.Engine.Physics.Util;

import com.fuchsbau.shorin.Engine.Dice.DiceType;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class DiceSpawnData {
    public Vec3 position;
    public Vec3 velocity;
    public Vec3 angularVelocity;

    public Vec3 axis;
    public double angle;

    public DiceType type;
    public double mass;

    public DiceSpawnData(){

    }

}