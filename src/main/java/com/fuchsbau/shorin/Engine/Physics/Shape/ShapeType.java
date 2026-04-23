package com.fuchsbau.shorin.Engine.Physics.Shape;

public enum ShapeType {
    SPHERE(1),
    PLANE(2),
    BOX(4),
    COMPOUND(8),
    CONVEXPOLYHEDRON(16),
    HEIGHTFIELD(32),
    PARTICLE(64),
    CYLINDER(128),
    TRIMESH(256);


    public int index;

    ShapeType(int i) {
        this.index = i;
    }
}