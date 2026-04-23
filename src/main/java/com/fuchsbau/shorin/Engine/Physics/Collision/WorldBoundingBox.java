package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;

import java.util.Objects;

public class WorldBoundingBox {
    private static final Vec3 tmp = new Vec3();

    public Vec3 lowerBound;
    public Vec3 upperBound;

    public WorldBoundingBox(Vec3 upperBound, Vec3 lowerBound) {
        this.upperBound = Objects.requireNonNullElseGet(upperBound, Vec3::new);
        this.lowerBound = Objects.requireNonNullElseGet(lowerBound, Vec3::new);
    }

    public WorldBoundingBox(WorldBoundingBox worldBoundingBox) {
        this.lowerBound = new Vec3(worldBoundingBox.lowerBound);
        this.upperBound = new Vec3(worldBoundingBox.upperBound);
    }

    public WorldBoundingBox setFromPoints(Vec3[] points, Vec3 position, Quaternion quaternion, double skinSize) {

        // Set to the first point
        lowerBound = new Vec3(points[0]);
        if (quaternion != null) {
            quaternion.vmult(lowerBound, lowerBound);
        }
        upperBound = new Vec3(lowerBound);

        for (int i = 1; i < points.length; i++) {
            Vec3 p = points[i];

            if (quaternion != null) {
                quaternion.vmult(p, tmp);
                p = tmp;
            }

            if (p.x > upperBound.x) {
                upperBound.x = p.x;
            }
            if (p.x < lowerBound.x) {
                lowerBound.x = p.x;
            }
            if (p.y > upperBound.y) {
                upperBound.y = p.y;
            }
            if (p.y < lowerBound.y) {
                lowerBound.y = p.y;
            }
            if (p.z > upperBound.z) {
                upperBound.z = p.z;
            }
            if (p.z < lowerBound.z) {
                lowerBound.z = p.z;
            }
        }

        // Add offset
        if (position != null) {
            position.add(lowerBound, lowerBound);
            position.add(upperBound, upperBound);
        }

        if (skinSize != 0) {
            lowerBound.x -= skinSize;
            lowerBound.y -= skinSize;
            lowerBound.z -= skinSize;
            upperBound.x += skinSize;
            upperBound.y += skinSize;
            upperBound.z += skinSize;
        }

        return this;
    }


    public void getCorners(Vec3 a, Vec3 b, Vec3 c, Vec3 d, Vec3 e, Vec3 f, Vec3 g, Vec3 h) {
        a.set(lowerBound.x, lowerBound.y, lowerBound.z);
        b.set(upperBound.x, lowerBound.y, lowerBound.z);
        c.set(upperBound.x, upperBound.y, lowerBound.z);
        d.set(lowerBound.x, upperBound.y, upperBound.z);
        e.set(upperBound.x, lowerBound.y, upperBound.z);
        f.set(lowerBound.x, upperBound.y, lowerBound.z);
        g.set(lowerBound.x, lowerBound.y, upperBound.z);
        h.set(upperBound.x, upperBound.y, upperBound.z);
    }

    public void extend(WorldBoundingBox boundingBox) {
        this.lowerBound.x = Math.min(this.lowerBound.x, boundingBox.lowerBound.x);
        this.upperBound.x = Math.max(this.upperBound.x, boundingBox.upperBound.x);
        this.lowerBound.y = Math.min(this.lowerBound.y, boundingBox.lowerBound.y);
        this.upperBound.y = Math.max(this.upperBound.y, boundingBox.upperBound.y);
        this.lowerBound.z = Math.min(this.lowerBound.z, boundingBox.lowerBound.z);
        this.upperBound.z = Math.max(this.upperBound.z, boundingBox.upperBound.z);
    }

    public boolean overlaps(WorldBoundingBox boundingBox) {
        boolean overlapsX = (boundingBox.lowerBound.x <= upperBound.x && upperBound.x <= boundingBox.upperBound.x)
                || (lowerBound.x <= boundingBox.upperBound.x && boundingBox.upperBound.x <= upperBound.x);

        boolean overlapsY = (boundingBox.lowerBound.y <= upperBound.y && upperBound.y <= boundingBox.upperBound.y)
                || (lowerBound.y <= boundingBox.upperBound.y && boundingBox.upperBound.y <= upperBound.y);

        boolean overlapsZ = (boundingBox.lowerBound.z <= upperBound.z && upperBound.z <= boundingBox.upperBound.z)
                || (lowerBound.z <= boundingBox.upperBound.z && boundingBox.upperBound.z <= upperBound.z);

        return overlapsX && overlapsY && overlapsZ;
    }

    public boolean contains(WorldBoundingBox boundingBox) {
        return lowerBound.x <= boundingBox.lowerBound.x
                && upperBound.x >= boundingBox.upperBound.x
                && lowerBound.y <= boundingBox.lowerBound.y
                && upperBound.y >= boundingBox.upperBound.y
                && lowerBound.z <= boundingBox.lowerBound.z
                && upperBound.z >= boundingBox.upperBound.z;
    }

    public void copy(WorldBoundingBox updateAABBShapeAABB) {
        this.lowerBound.copy(updateAABBShapeAABB.lowerBound);
        this.upperBound.copy(updateAABBShapeAABB.upperBound);
    }

    //public WorldBoundingBox toLocalFrame (Transform)
    //public WorldBoundingBox toWorldFrame (Transform)

    /*public boolean overlapsRay(Ray ray) {
    const{
            direction, from
        } =ray
        // const t = 0

        // ray.direction is unit direction vector of ray
    const dirFracX = 1 / direction.x
    const dirFracY = 1 / direction.y
    const dirFracZ = 1 / direction.z

        // this.lowerBound is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
    const t1 = (this.lowerBound.x - from.x) * dirFracX
    const t2 = (this.upperBound.x - from.x) * dirFracX
    const t3 = (this.lowerBound.y - from.y) * dirFracY
    const t4 = (this.upperBound.y - from.y) * dirFracY
    const t5 = (this.lowerBound.z - from.z) * dirFracZ
    const t6 = (this.upperBound.z - from.z) * dirFracZ

        // const tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)));
        // const tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)));
    const tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6))
    const tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6))

        // if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
        if (tmax < 0) {
            //t = tmax;
            return false
        }

        // if tmin > tmax, ray doesn't intersect AABB
        if (tmin > tmax) {
            //t = tmax;
            return false
        }

        return true

    }*/

}