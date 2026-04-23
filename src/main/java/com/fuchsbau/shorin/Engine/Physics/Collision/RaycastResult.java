package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.CollisionShape;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;

public class RaycastResult {

    public Vec3 rayFromWorld = new Vec3();
    public Vec3 rayToWorld = new Vec3();
    public Vec3 hitNormalWorld = new Vec3();
    public Vec3 hitPointWorld = new Vec3();
    public boolean hasHit = false;
    public CollisionShape shape = null;
    public PhysicsBody body = null;
    public int hitFaceIndex = -1;
    public double distance = -1;
    public boolean shouldStop = false;

    public void reset() {
        rayFromWorld.setZero();
        rayToWorld.setZero();
        hitNormalWorld.setZero();
        hitPointWorld.setZero();
        hasHit = false;
        shape = null;
        body = null;
        hitFaceIndex = -1;
        distance = -1;
        shouldStop = false;
    }

    public void abort() {
        shouldStop = true;
    }

    public void set(Vec3 rayFromWorld, Vec3 rayToWorld, Vec3 hitNormalWorld,
                    Vec3 hitPointWorld, CollisionShape shape, PhysicsBody body, double distance) {
        this.rayFromWorld.copy(rayFromWorld);
        this.rayToWorld.copy(rayToWorld);
        this.hitNormalWorld.copy(hitNormalWorld);
        this.hitPointWorld.copy(hitPointWorld);
        this.shape = shape;
        this.body = body;
        this.distance = distance;
    }
}