package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Material.Material;

public class CollisionShapeOptions {
    public int collisionFilterGroup = 1;
    public int collisionFilterMask = -1;
    public boolean collisionResponse = true;
    public ShapeType type = ShapeType.SPHERE;
    public Material material;

    public CollisionShapeOptions() {

    }

    public CollisionShapeOptions(int collisionFilterGroup, int collisionFilterMask, boolean collisionResponse, ShapeType shapeTypes) {
        this.collisionFilterGroup = collisionFilterGroup;
        this.collisionFilterMask = collisionFilterMask;
        this.collisionResponse = collisionResponse;
        this.type = shapeTypes;
    }


}