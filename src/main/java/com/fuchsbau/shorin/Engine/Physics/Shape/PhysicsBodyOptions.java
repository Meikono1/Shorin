package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Material.Material;
import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

import java.util.ArrayList;

public class PhysicsBodyOptions {
    //DONE

    public boolean allowSleep = true;

    public double mass = 0;
    public BodyType type;
    public float sleepSpeedLimit = 0.1f;
    public double sleepTimeLimit = 1;

    public int collisionFilterGroup = 1;
    public int collisionFilterMask = -1;
    public boolean collisionResponse = true;
    public boolean fixedRotation = false;
    public boolean isTrigger = false;

    public ArrayList<CollisionShape> shapes = new ArrayList<>();

    public Material material;

    public Vec3 position;

    public Vec3 linearFactor;
    public Vec3 angularFactor;

    public Vec3 velocity;

    public Vec3 angularVelocity;
    public float angularDamping = 0.01f;

    public Quaternion quaternion;

    public float linearDamping = 0.01f;
}