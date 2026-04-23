package com.fuchsbau.shorin.Engine.Physics.Math;

public class Vec3 {
    // DONE

    public double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3() {
        this.x = 0.0;
        this.y = 0.0;
        this.z = 0.0;
    }

    public Vec3(Vec3 toCopy) {
        this.x = toCopy.x;
        this.y = toCopy.y;
        this.z = toCopy.z;
    }

    public Vec3 set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Vec3 add(Vec3 o) {
        return new Vec3(x + o.x, y + o.y, z + o.z);
    }

    public void add(Vec3 o, Vec3 target) {
        target.x = this.x + o.x;
        target.y = this.y + o.y;
        target.z = this.z + o.z;
    }

    public Vec3 addScaledVector(double scalar, Vec3 vector, Vec3 target) {
        target.x = this.x + scalar * vector.x;
        target.y = this.y + scalar * vector.y;
        target.z = this.z + scalar * vector.z;
        return target;
    }

    public Vec3 vmul(Vec3 vector, Vec3 target) {
        target.x = vector.x * this.x;
        target.y = vector.y * this.y;
        target.z = vector.z * this.z;
        return target;
    }

    public Vec3 sub(Vec3 o) {
        return new Vec3(x - o.x, y - o.y, z - o.z);
    }

    public void sub(Vec3 o, Vec3 target) {
        target.x = this.x - o.x;
        target.y = this.y - o.y;
        target.z = this.z - o.z;
    }

    public Vec3 scale(double s) {
        return new Vec3(x * s, y * s, z * s);
    }

    public Vec3 scale(double scalar, Vec3 target) {
        target.x = scalar * x;
        target.y = scalar * y;
        target.z = scalar * z;
        return target;
    }

    public double dot(Vec3 o) {
        return x * o.x + y * o.y + z * o.z;
    }

    public Vec3 cross(Vec3 o) {
        return new Vec3(
                y * o.z - z * o.y,
                z * o.x - x * o.z,
                x * o.y - y * o.x
        );
    }

    public void cross(Vec3 o, Vec3 target) {
        target.x = y * o.z - z * o.y;
        target.y = z * o.x - x * o.z;
        target.z = x * o.y - y * o.x;
    }

    public Matrix3 crossMat() {
        return new Matrix3(new double[]{0, -z, y, z, 0, -x, -y, x, 0});
    }

    public double length() {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public Double normalize() {
        double n = length();

        if (n > 0.0) {
            double invN = 1 / n;
            this.x *= invN;
            this.y *= invN;
            this.z *= invN;
        } else {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }
        return n;
    }

    public Vec3 unit(Vec3 target) {
        double len = length();
        if (len > 0) {
            target.x = x / len;
            target.y = y / len;
            target.z = z / len;
        } else {
            target.x = 0;
            target.y = 0;
            target.z = 0;
        }
        return target;
    }

    public Vec3 unit() {
        Vec3 ret = new Vec3();
        double length = length();
        if (length > 0.0) {
            length = 1.0 / length;
            ret.x = x * length;
            ret.y = y * length;
            ret.z = z * length;
        } else {
            ret.x = 1;
            ret.y = 0;
            ret.z = 0;
        }
        return ret;
    }

    public void tangents(Vec3 vectorA, Vec3 vectorB) {
        double length = length();
        if (length > 0.0) {
            Vec3 n = new Vec3();
            double inorm = 1 / length;
            n.set(this.x * inorm, this.y * inorm, this.z * inorm);
            Vec3 randVec = new Vec3();
            if (Math.abs(n.x) < 0.9) {
                randVec.set(1, 0, 0);
                n.cross(randVec, vectorA);
            } else {
                randVec.set(0, 1, 0);
                n.cross(randVec, vectorA);
            }
            n.cross(vectorA, vectorB);
        } else {
            vectorA.set(1, 0, 0);
            vectorB.set(0, 1, 0);
        }
    }

    public double angleTo(Vec3 v) {
        double denom = length() * v.length();
        if (denom == 0) return 0;
        return Math.acos(Math.max(-1, Math.min(1, dot(v) / denom)));
    }

    public void lerp(Vec3 vector, double t, Vec3 target) {
        target.x = x + (vector.x - x) * t;
        target.y = y + (vector.y - y) * t;
        target.z = z + (vector.z - z) * t;
    }

    public Vec3 negate() {
        return new Vec3(-x, -y, -z);
    }

    public Vec3 negate(Vec3 target) {
        target.x = -this.x;
        target.y = -this.y;
        target.z = -this.z;
        return target;
    }

    public double lengthSquared() {
        return this.dot(this);
    }

    public double distanceTo(Vec3 target) {
        return Math.sqrt((target.x - x) * (target.x - x) + (target.y - y) * (target.y - y) + (target.z - z) * (target.z - z));
    }

    public double distanceSquared(Vec3 target) {
        return (target.x - x) * (target.x - x) + (target.y - y) * (target.y - y) + (target.z - z) * (target.z - z);
    }

    public boolean isAntiparallelTo(Vec3 v) {
        return isAntiparallelTo(v, 1e-6);
    }

    public boolean isAntiparallelTo(Vec3 v, double precision) {
        Vec3 neg = new Vec3(-x, -y, -z);
        return neg.almostEquals(v, precision);
    }

    public boolean almostEquals(Vec3 vector) {
        return almostEquals(vector, 1e-6);
    }

    public boolean almostEquals(Vec3 vector, double precision) {
        return !(Math.abs(this.x - vector.x) > precision) &&
                !(Math.abs(this.y - vector.y) > precision) &&
                !(Math.abs(this.z - vector.z) > precision);
    }

    public boolean almostZero() {
        return almostZero(1e-6);
    }

    public boolean almostZero(double precision) {
        return !(Math.abs(this.x) > precision) && !(Math.abs(this.y) > precision) && !(Math.abs(this.z) > precision);
    }

    public void setZero() {
        this.x = this.y = this.z = 0;
    }

    public boolean isZero() {
        return x == 0 && y == 0 && z == 0;
    }

    public double[] toArray() {
        return new double[]{x, y, z};
    }

    public void copy(Vec3 vec3) {
        this.x = vec3.x;
        this.y = vec3.y;
        this.z = vec3.z;
    }

    public Vec3 clone() {
        return new Vec3(x, y, z);
    }
}