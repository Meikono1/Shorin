package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class Particle extends CollisionShape {

    public Particle() {
        super(new CollisionShapeOptions(1, -1, true, ShapeType.PARTICLE));
    }

    @Override
    public void calculateLocalInertia(double mass, Vec3 target) {
        target.set(0, 0, 0);
    }

    @Override
    public double volume() {
        return 0;
    }

    @Override
    public void updateBoundingSphereRadius() {
        this.boundingSphereRadius = 0;
    }

    @Override
    public void calculateWorldAABB(Vec3 pos, Quaternion quat, Vec3 min, Vec3 max) {
        // Get each axis max
        min.copy(pos);
        max.copy(pos);
    }
}