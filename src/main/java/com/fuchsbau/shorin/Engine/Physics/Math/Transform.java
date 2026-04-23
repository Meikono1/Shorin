package com.fuchsbau.shorin.Engine.Physics.Math;

public class Transform {

    private static final Quaternion tmpQuat = new Quaternion();

    public Vec3 position;
    public Quaternion quaternion;

    public Transform() {
        this.position = new Vec3();
        this.quaternion = new Quaternion(0, 0, 0, 1);
    }

    public Transform(Vec3 position, Quaternion quaternion) {
        this.position = new Vec3();
        this.quaternion = new Quaternion(0, 0, 0, 1);

        if (position != null) {
            this.position.copy(position);
        }
        if (quaternion != null) {
            this.quaternion.copy(quaternion);
        }
    }

    public Vec3 pointToLocal(Vec3 worldPoint, Vec3 result) {
        return Transform.pointToLocalFrame(this.position, this.quaternion, worldPoint, result);
    }

    public Vec3 pointToWorld(Vec3 localPoint, Vec3 result) {
        return Transform.pointToWorldFrame(this.position, this.quaternion, localPoint, result);
    }

    public Vec3 vectorToWorldFrame(Vec3 localVector, Vec3 result) {
        this.quaternion.vmult(localVector, result);
        return result;
    }

    public static Vec3 pointToLocalFrame(Vec3 position, Quaternion quaternion, Vec3 worldPoint, Vec3 result) {
        if (result == null) result = new Vec3();
        worldPoint.sub(position, result);
        quaternion.conjugate(tmpQuat);
        tmpQuat.vmult(result, result);
        return result;
    }

    public static Vec3 pointToWorldFrame(Vec3 position, Quaternion quaternion, Vec3 localPoint, Vec3 result) {
        if (result == null) result = new Vec3();
        quaternion.vmult(localPoint, result);
        result.add(position, result);
        return result;
    }

    public static Vec3 vectorToWorldFrame(Quaternion quaternion, Vec3 localVector, Vec3 result) {
        if (result == null) result = new Vec3();
        quaternion.vmult(localVector, result);
        return result;
    }

    public static Vec3 vectorToLocalFrame(Vec3 position, Quaternion quaternion, Vec3 worldVector, Vec3 result) {
        if (result == null) result = new Vec3();
        quaternion.w *= -1;
        quaternion.vmult(worldVector, result);
        quaternion.w *= -1;
        return result;
    }
}