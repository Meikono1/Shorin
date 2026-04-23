package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Material.Material;
import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class CollisionShape {
    private static int idCounter = 0;

    public int id;

    public double boundingSphereRadius;
    public int collisionFilterGroup = 1;
    public int collisionFilterMask = -1;
    public boolean collisionResponse = true;
    public ShapeType type;
    public PhysicsBody body;

    public Material material;

    CollisionShape(CollisionShapeOptions options) {
        this.id = idCounter++;
        this.type = options.type;
        this.boundingSphereRadius = 0.0;
        this.collisionResponse = options.collisionResponse;
        this.collisionFilterGroup = options.collisionFilterGroup;
        this.collisionFilterMask = options.collisionFilterMask;
        this.material = options.material;
        this.body = null;
    }

    public void updateBoundingSphereRadius() {
        throw new UnsupportedOperationException("updateBoundingSphereRadius() nicht implementiert für: " + getClass().getSimpleName());
    }

    public double volume() {
        throw new UnsupportedOperationException("volume() nicht implementiert für: " + getClass().getSimpleName());
    }

    public void calculateLocalInertia(double mass, Vec3 target) {
        throw new UnsupportedOperationException("calculateLocalInertia() nicht implementiert für: " + getClass().getSimpleName());
    }

    public void calculateWorldAABB(Vec3 pos, Quaternion quat, Vec3 min, Vec3 max) {
        throw new UnsupportedOperationException("calculateWorldAABB() nicht implementiert für: " + getClass().getSimpleName());
    }
}
