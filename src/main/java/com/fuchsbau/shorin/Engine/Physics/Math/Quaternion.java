package com.fuchsbau.shorin.Engine.Physics.Math;

public class Quaternion {
    // DONE

    private static final Vec3 sfv_t1 = new Vec3();
    private static final Vec3 sfv_t2 = new Vec3();

    public double x, y, z, w;

    public Quaternion() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 1;
    }

    public Quaternion(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Quaternion(Quaternion quaternion) {
        this.x = quaternion.x;
        this.y = quaternion.y;
        this.z = quaternion.z;
        this.w = quaternion.w;
    }


    public Quaternion setAll(double x, double y, double z, double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Vec3 rotate(Vec3 v) {
        Vec3 u = new Vec3(x, y, z);
        Vec3 res = u.scale(2.0 * u.dot(v))
                .add(v.scale(w * w - u.dot(u)))
                .add(u.cross(v).scale(2.0 * w));
        return res;
    }

    // normalize()
    public Quaternion normalize() {
        double l = Math.sqrt(x * x + y * y + z * z + w * w);
        if (l == 0) {
            x = 0;
            y = 0;
            z = 0;
            w = 0;
        } else {
            l = 1.0 / l;
            x *= l;
            y *= l;
            z *= l;
            w *= l;
        }
        return this;
    }

    // multiply
    public Quaternion mult(Quaternion q, Quaternion ret) {
        Quaternion zwi = mult(q);
        ret.copy(zwi);
        return ret;
    }

    public Quaternion mult(Quaternion q) {
        return new Quaternion(
                x * q.w + w * q.x + y * q.z - z * q.y,
                y * q.w + w * q.y + z * q.x - x * q.z,
                z * q.w + w * q.z + x * q.y - y * q.x,
                w * q.w - x * q.x - y * q.y - z * q.z
        );
    }

    public void vmult(Vec3 v, Vec3 target) {
        Vec3 result = vmult(v);
        target.x = result.x;
        target.y = result.y;
        target.z = result.z;
    }

    // Quaternion auf Vec3 anwenden
    public Vec3 vmult(Vec3 v) {
        double ix = w * v.x + y * v.z - z * v.y;
        double iy = w * v.y + z * v.x - x * v.z;
        double iz = w * v.z + x * v.y - y * v.x;
        double iw = -x * v.x - y * v.y - z * v.z;

        return new Vec3(
                ix * w + iw * -x + iy * -z - iz * -y,
                iy * w + iw * -y + iz * -x - ix * -z,
                iz * w + iw * -z + ix * -y - iy * -x
        );
    }

    public Quaternion integrate(Vec3 angularVelocity, double dt, Vec3 angularFactor, Quaternion target) {
        double ax = angularVelocity.x * angularFactor.x;
        double ay = angularVelocity.y * angularFactor.y;
        double az = angularVelocity.z * angularFactor.z;
        double bx = this.x;
        double by = this.y;
        double bz = this.z;
        double bw = this.w;

        double half_dt = dt * 0.5;

        target.x += half_dt * (ax * bw + ay * bz - az * by);
        target.y += half_dt * (ay * bw + az * bx - ax * bz);
        target.z += half_dt * (az * bw + ax * by - ay * bx);
        target.w += half_dt * (-ax * bx - ay * by - az * bz);

        return target;
    }

    public Object[] toAxisAngle() {
        Vec3 targetAxis = new Vec3();
        normalize();
        double angle = 2 * Math.acos(w);
        double s = Math.sqrt(1 - this.w * this.w);

        if (s < 0.001) {
            targetAxis.x = this.x;
            targetAxis.y = this.y;
            targetAxis.z = this.z;
        } else {
            targetAxis.x = this.x / s;
            targetAxis.y = this.y / s;
            targetAxis.z = this.z / s;
        }
        return new Object[]{targetAxis, angle};
    }

    public Quaternion setFromVectors(Vec3 u, Vec3 v) {
        if (u.isAntiparallelTo(v)) {
            u.tangents(sfv_t1, sfv_t2);
            this.setFromAxisAngle(sfv_t1, Math.PI);
        } else {
            Vec3 a = u.cross(v);
            x = a.x;
            y = a.y;
            z = a.z;
            w = u.length() * v.length() + u.dot(v);
            this.normalize();
        }
        return this;
    }

    public Quaternion setFromAxisAngle(Vec3 vector, Double angle) {
        double s = Math.sin(angle * 0.5);
        x = vector.x * s;
        y = vector.y * s;
        z = vector.z * s;
        w = Math.cos(angle * 0.5);
        return this;
    }

    public Quaternion inverse() {
        Quaternion ret = new Quaternion();

        conjugate(ret);
        double inorm2 = 1 / (x * x + y * y + z * z + w * w);
        ret.x *= inorm2;
        ret.y *= inorm2;
        ret.z *= inorm2;
        ret.w *= inorm2;

        return ret;
    }

    public Quaternion conjugate(Quaternion quaternion) {
        quaternion.x = -this.x;
        quaternion.y = -this.y;
        quaternion.z = -this.z;
        quaternion.w = this.w;

        return quaternion;
    }

    public Quaternion slerp(Quaternion toQuat, double t, Quaternion target) {
        double bx = toQuat.x;
        double by = toQuat.y;
        double bz = toQuat.z;
        double bw = toQuat.w;
        double omega;
        double cosom;
        double sinom;
        double scale0;
        double scale1;

        // calc cosine
        cosom = x * bx + y * by + z * bz + w * bw;

        // adjust signs (if necessary)
        if (cosom < 0.0) {
            cosom = -cosom;
            bx = -bx;
            by = -by;
            bz = -bz;
            bw = -bw;
        }

        // calculate coefficients
        if (1.0 - cosom > 0.000001) {
            omega = Math.acos(cosom);
            sinom = Math.sin(omega);
            scale0 = Math.sin((1.0 - t) * omega) / sinom;
            scale1 = Math.sin(t * omega) / sinom;
        } else {
            scale0 = 1.0 - t;
            scale1 = t;
        }

        // calculate final values
        target.x = scale0 * x + scale1 * bx;
        target.y = scale0 * y + scale1 * by;
        target.z = scale0 * z + scale1 * bz;
        target.w = scale0 * w + scale1 * bw;

        return target;
    }

    public Quaternion setFromEuler(double x, double y, double z, String order) {
        if (order == null) {
            order = "";
        }

        double c1 = Math.cos(x / 2);
        double c2 = Math.cos(y / 2);
        double c3 = Math.cos(z / 2);
        double s1 = Math.sin(x / 2);
        double s2 = Math.sin(y / 2);
        double s3 = Math.sin(z / 2);

        switch (order) {
            case "YXZ": {
                this.x = s1 * c2 * c3 + c1 * s2 * s3;
                this.y = c1 * s2 * c3 - s1 * c2 * s3;
                this.z = c1 * c2 * s3 - s1 * s2 * c3;
                this.w = c1 * c2 * c3 + s1 * s2 * s3;
                break;
            }
            case "ZXY": {
                this.x = s1 * c2 * c3 - c1 * s2 * s3;
                this.y = c1 * s2 * c3 + s1 * c2 * s3;
                this.z = c1 * c2 * s3 + s1 * s2 * c3;
                this.w = c1 * c2 * c3 - s1 * s2 * s3;
                break;
            }
            case "ZYX": {
                this.x = s1 * c2 * c3 - c1 * s2 * s3;
                this.y = c1 * s2 * c3 + s1 * c2 * s3;
                this.z = c1 * c2 * s3 - s1 * s2 * c3;
                this.w = c1 * c2 * c3 + s1 * s2 * s3;
                break;
            }
            case "YZX": {
                this.x = s1 * c2 * c3 + c1 * s2 * s3;
                this.y = c1 * s2 * c3 + s1 * c2 * s3;
                this.z = c1 * c2 * s3 - s1 * s2 * c3;
                this.w = c1 * c2 * c3 - s1 * s2 * s3;
                break;
            }
            case "XZY": {
                this.x = s1 * c2 * c3 - c1 * s2 * s3;
                this.y = c1 * s2 * c3 - s1 * c2 * s3;
                this.z = c1 * c2 * s3 + s1 * s2 * c3;
                this.w = c1 * c2 * c3 + s1 * s2 * s3;
                break;
            }
            case "XYZ":
            default: {
                this.x = s1 * c2 * c3 + c1 * s2 * s3;
                this.y = c1 * s2 * c3 - s1 * c2 * s3;
                this.z = c1 * c2 * s3 + s1 * s2 * c3;
                this.w = c1 * c2 * c3 - s1 * s2 * s3;
                break;
            }
        }

        return this;
    }

    public void toEuler(Vec3 target) {
        double heading = 0, attitude = 0, bank = 0;

        double test = x * y + z * w;

        if (test > 0.499) {
            // singularity at north pole
            heading = 2 * Math.atan2(x, w);
            attitude = Math.PI / 2;
            bank = 0;
        }
        if (test < -0.499) {
            // singularity at south pole
            heading = -2 * Math.atan2(x, w);
            attitude = -Math.PI / 2;
            bank = 0;
        }
        if (heading == 0) {
            double sqx = x * x;
            double sqy = y * y;
            double sqz = z * z;
            heading = Math.atan2(2 * y * w - 2 * x * z, 1 - 2 * sqy - 2 * sqz);
            attitude = Math.asin(2 * test);
            bank = Math.atan2(2 * x * w - 2 * y * z, 1 - 2 * sqx - 2 * sqz);
        }

        target.y = heading;
        target.z = attitude;
        target.x = bank;
    }

    /***
     * @return Array als X,Y,Z,W
     */
    public double[] toArray() {
        double[] ret = new double[4];
        ret[0] = x;
        ret[1] = y;
        ret[2] = z;
        ret[3] = w;
        return ret;
    }

    public void copy(Quaternion orientation) {
        this.x = orientation.x;
        this.y = orientation.y;
        this.z = orientation.z;
        this.w = orientation.w;
    }
}
