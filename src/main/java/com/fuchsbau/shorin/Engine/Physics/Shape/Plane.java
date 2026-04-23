package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

public class Plane extends CollisionShape {
    private static final Vec3 tempNormal = new Vec3();

    public Vec3 worldNormal;
    public boolean worldNormalNeedsUpdate;

    public Plane() {
        super(new CollisionShapeOptions(1, -1, true, ShapeType.PLANE));
        this.worldNormal = new Vec3();
        this.worldNormalNeedsUpdate = true;
        this.boundingSphereRadius = Double.MAX_VALUE;
    }

    public void computeWorldNormal(Quaternion quat) {
        worldNormal.set(0, 0, 1);
        quat.vmult(worldNormal, worldNormal);
        worldNormalNeedsUpdate = false;
    }

    @Override
    public void calculateLocalInertia(double mass, Vec3 target) {
        // Ebene hat unendliche Trägheit – nichts zu berechnen
    }

    @Override
    public double volume() {
        return Double.MAX_VALUE;
    }

    @Override
    public void calculateWorldAABB(Vec3 pos, Quaternion quat, Vec3 min, Vec3 max) {
        tempNormal.set(0, 0, 1);
        quat.vmult(tempNormal, tempNormal);

        double maxVal = Double.MAX_VALUE;
        min.set(-maxVal, -maxVal, -maxVal);
        max.set(maxVal, maxVal, maxVal);

        if (tempNormal.x == 1) max.x = pos.x;
        else if (tempNormal.x == -1) min.x = pos.x;

        if (tempNormal.y == 1) max.y = pos.y;
        else if (tempNormal.y == -1) min.y = pos.y;

        if (tempNormal.z == 1) max.z = pos.z;
        else if (tempNormal.z == -1) min.z = pos.z;
    }

    @Override
    public void updateBoundingSphereRadius() {
        boundingSphereRadius = Double.MAX_VALUE;
    }
}