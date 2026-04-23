package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class Sphere extends CollisionShape {
    public double radius;

    public Sphere(double radius){
        super(new CollisionShapeOptions(1, -1, true,ShapeType.SPHERE));
        this.radius=radius;

        if (this.radius < 0) {
            throw new Error("The sphere radius cannot be negative.");
        }

        this.updateBoundingSphereRadius();
    }

    @Override
    public void calculateLocalInertia(double mass, Vec3 target) {
        double I = (2.0 * mass * radius * radius) / 5.0;
        target.x = I;
        target.y = I;
        target.z = I;
    }

    @Override
    public double volume() {
        return (4.0 * Math.PI * Math.pow(radius, 3)) / 3.0;
    }

    @Override
    public void updateBoundingSphereRadius() {
        boundingSphereRadius = radius;
    }

    @Override
    public void calculateWorldAABB(Vec3 pos, Quaternion quat, Vec3 min, Vec3 max) {
        min.x = pos.x - radius;
        min.y = pos.y - radius;
        min.z = pos.z - radius;

        max.x = pos.x + radius;
        max.y = pos.y + radius;
        max.z = pos.z + radius;
    }
}