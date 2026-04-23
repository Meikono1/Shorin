package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class ConvexPolyhedronContactPoint {
    public Vec3 point;
    public Vec3 normal;
    public double depth;

    public ConvexPolyhedronContactPoint(Vec3 point, Vec3 normal, double depth) {
        this.point = point;
        this.normal = normal;
        this.depth = depth;
    }
}
