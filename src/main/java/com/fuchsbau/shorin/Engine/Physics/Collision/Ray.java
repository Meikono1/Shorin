package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.*;
import com.fuchsbau.shorin.Engine.Physics.World.World;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class Ray {
    private final Logger logger = FileLogger.getLogger();

    private static final WorldBoundingBox tmpAABB = new WorldBoundingBox(null, null);
    private static final List<PhysicsBody> tmpArray = new ArrayList<>();
    private static final Vec3 intersectBody_xi = new Vec3();
    private static final Quaternion intersectBody_qi = new Quaternion(0, 0, 0, 1);

    private static final Vec3 Ray_intersectSphere_intersectionPoint = new Vec3();
    private static final Vec3 Ray_intersectSphere_normal = new Vec3();
    private static final Vec3 intersectConvex_minDistNormal = new Vec3();
    private static final Vec3 intersectConvex_normal = new Vec3();
    private static final Vec3 intersectConvex_vector = new Vec3();
    private static final Vec3 intersectConvex_minDistIntersect = new Vec3();
    private static final Vec3 intersectPoint = new Vec3();
    private static final Vec3 a = new Vec3();
    private static final Vec3 b = new Vec3();
    private static final Vec3 c = new Vec3();
    private static final Vec3 v0 = new Vec3();
    private static final Vec3 v1 = new Vec3();
    private static final Vec3 v2 = new Vec3();

    public Vec3 from;
    public Vec3 to;
    public Vec3 direction;
    public double precision = 0.0001;
    public boolean checkCollisionResponse = true;
    public boolean skipBackfaces = false;
    public int collisionFilterMask = -1;
    public int collisionFilterGroup = -1;
    public RayMode mode = RayMode.ANY;
    public RaycastResult result;
    public boolean hasHit = false;
    public World.RaycastCallback callback = (r) -> {
    };

    public Ray(Vec3 from, Vec3 to) {
        this.from = from.clone();
        this.to = to.clone();
        this.direction = new Vec3();
        this.result = new RaycastResult();
    }

    public Ray() {
        this(new Vec3(), new Vec3());
    }

    public boolean intersectWorld(World world, RayOptions options) {
        this.mode = options.mode;
        this.result = options.result != null ? options.result : new RaycastResult();
        this.skipBackfaces = options.skipBackfaces;
        this.collisionFilterMask = options.collisionFilterMask;
        this.collisionFilterGroup = options.collisionFilterGroup;
        this.checkCollisionResponse = options.checkCollisionResponse;

        if (options.from != null) this.from.copy(options.from);
        if (options.to != null) this.to.copy(options.to);

        this.callback = options.callback != null ? options.callback : (r) -> {
        };
        this.hasHit = false;

        this.result.reset();
        this.updateDirection();

        this.getAABB(tmpAABB);
        tmpArray.clear();
        world.broadphase.aabbQuery(world, tmpAABB, tmpArray);
        this.intersectBodies(tmpArray);

        return this.hasHit;
    }

    public void intersectBody(PhysicsBody body, RaycastResult result) {
        if (result != null) {
            this.result = result;
            updateDirection();
        }

        if (checkCollisionResponse && !body.collisionResponse) return;

        if ((collisionFilterGroup & body.collisionFilterMask) == 0 ||
                (body.collisionFilterGroup & collisionFilterMask) == 0) {
            return;
        }

        for (int i = 0; i < body.shapes.size(); i++) {
            CollisionShape shape = body.shapes.get(i);

            if (checkCollisionResponse && !shape.collisionResponse) continue;

            body.quaternion.mult(body.shapeOrientations.get(i), intersectBody_qi);
            body.quaternion.vmult(body.shapeOffsets.get(i), intersectBody_xi);
            intersectBody_xi.add(body.position, intersectBody_xi);

            intersectShape(shape, intersectBody_qi, intersectBody_xi, body);

            if (this.result.shouldStop) break;
        }
    }

    public void intersectBodies(List<PhysicsBody> bodies, RaycastResult result) {
        if (result != null) {
            this.result = result;
            updateDirection();
        }

        for (int i = 0; !this.result.shouldStop && i < bodies.size(); i++) {
            intersectBody(bodies.get(i), null);
        }
    }

    public void intersectBodies(List<PhysicsBody> bodies) {
        intersectBodies(bodies, null);
    }

    private void updateDirection() {
        to.sub(from, direction);
        direction.normalize();
    }

    private void intersectShape(CollisionShape shape, Quaternion quat, Vec3 position, PhysicsBody body) {
        double distance = distanceFromIntersection(from, direction, position);
        if (distance > shape.boundingSphereRadius) return;

        switch (shape.type) {
            case SPHERE -> _intersectSphere((Sphere) shape, quat, position, body);
            case PLANE -> _intersectPlane((Plane) shape, quat, position, body);
            case BOX -> _intersectBox((Box) shape, quat, position, body);
            case CYLINDER,
                 CONVEXPOLYHEDRON -> _intersectConvex((ConvexPolyhedron) shape, quat, position, body, shape);
            default -> logger.warning("intersectShape – kein Resolver für Typ: " + shape.type);
        }
    }

    private void _intersectBox(Box box, Quaternion quat, Vec3 position, PhysicsBody body) {
        _intersectConvex(box.convexPolyhedronRepresentation, quat, position, body, box);
    }

    private void _intersectPlane(Plane shape, Quaternion quat, Vec3 position, PhysicsBody body) {
        Vec3 worldNormal = new Vec3(0, 0, 1);
        quat.vmult(worldNormal, worldNormal);

        Vec3 len = new Vec3();
        from.sub(position, len);
        double planeToFrom = len.dot(worldNormal);
        to.sub(position, len);
        double planeToTo = len.dot(worldNormal);

        if (planeToFrom * planeToTo > 0) return;
        if (from.distanceTo(to) < planeToFrom) return;

        double n_dot_dir = worldNormal.dot(direction);
        if (Math.abs(n_dot_dir) < precision) return;

        Vec3 planePointToFrom = new Vec3();
        Vec3 dir_scaled_with_t = new Vec3();
        Vec3 hitPointWorld = new Vec3();

        from.sub(position, planePointToFrom);
        double t = -worldNormal.dot(planePointToFrom) / n_dot_dir;
        direction.scale(t, dir_scaled_with_t);
        from.add(dir_scaled_with_t, hitPointWorld);

        reportIntersection(worldNormal, hitPointWorld, shape, body, -1);
    }

    public void getAABB(WorldBoundingBox aabb) {
        aabb.lowerBound.x = Math.min(to.x, from.x);
        aabb.lowerBound.y = Math.min(to.y, from.y);
        aabb.lowerBound.z = Math.min(to.z, from.z);
        aabb.upperBound.x = Math.max(to.x, from.x);
        aabb.upperBound.y = Math.max(to.y, from.y);
        aabb.upperBound.z = Math.max(to.z, from.z);
    }

    private void _intersectHeightfield(Heightfield shape, Quaternion quat, Vec3 position, PhysicsBody body) {
        // Heightfield nicht implementiert – für Würfel nicht benötigt
        logger.warning("_intersectHeightfield – nicht implementiert");
    }

    private void _intersectSphere(Sphere sphere, Quaternion quat, Vec3 position, PhysicsBody body) {
        double r = sphere.radius;

        double a = Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2) + Math.pow(to.z - from.z, 2);
        double b = 2 * ((to.x - from.x) * (from.x - position.x)
                + (to.y - from.y) * (from.y - position.y)
                + (to.z - from.z) * (from.z - position.z));
        double c = Math.pow(from.x - position.x, 2)
                + Math.pow(from.y - position.y, 2)
                + Math.pow(from.z - position.z, 2) - r * r;

        double delta = b * b - 4 * a * c;

        Vec3 intersectionPoint = Ray_intersectSphere_intersectionPoint;
        Vec3 normal = Ray_intersectSphere_normal;

        if (delta < 0) {
            return;
        } else if (delta == 0) {
            from.lerp(to, delta, intersectionPoint);
            intersectionPoint.sub(position, normal);
            normal.normalize();
            reportIntersection(normal, intersectionPoint, sphere, body, -1);
        } else {
            double d1 = (-b - Math.sqrt(delta)) / (2 * a);
            double d2 = (-b + Math.sqrt(delta)) / (2 * a);

            if (d1 >= 0 && d1 <= 1) {
                from.lerp(to, d1, intersectionPoint);
                intersectionPoint.sub(position, normal);
                normal.normalize();
                reportIntersection(normal, intersectionPoint, sphere, body, -1);
            }

            if (result.shouldStop) return;

            if (d2 >= 0 && d2 <= 1) {
                from.lerp(to, d2, intersectionPoint);
                intersectionPoint.sub(position, normal);
                normal.normalize();
                reportIntersection(normal, intersectionPoint, sphere, body, -1);
            }
        }
    }

    private void _intersectConvex(ConvexPolyhedron shape, Quaternion quat, Vec3 position,
                                  PhysicsBody body, CollisionShape reportedShape) {
        _intersectConvex(shape, quat, position, body, reportedShape, null);
    }

    private void _intersectConvex(ConvexPolyhedron shape, Quaternion quat, Vec3 position,
                                  PhysicsBody body, CollisionShape reportedShape, int[] faceList) {
        Vec3 normal = intersectConvex_normal;
        Vec3 vector = intersectConvex_vector;

        double fromToDistance = from.distanceTo(to);
        int Nfaces = faceList != null ? faceList.length : shape.faces.size();

        for (int j = 0; !result.shouldStop && j < Nfaces; j++) {
            int fi = faceList != null ? faceList[j] : j;
            int[] face = shape.faces.get(fi);
            Vec3 faceNormal = shape.faceNormals.get(fi);

            vector.copy(shape.vertices.get(face[0]));
            quat.vmult(vector, vector);
            vector.add(position, vector);
            vector.sub(from, vector);

            quat.vmult(faceNormal, normal);

            double dot = direction.dot(normal);
            if (Math.abs(dot) < precision) continue;

            double scalar = normal.dot(vector) / dot;
            if (scalar < 0) continue;

            direction.scale(scalar, intersectPoint);
            intersectPoint.add(from, intersectPoint);

            a.copy(shape.vertices.get(face[0]));
            quat.vmult(a, a);
            position.add(a, a);

            for (int i = 1; !result.shouldStop && i < face.length - 1; i++) {
                b.copy(shape.vertices.get(face[i]));
                c.copy(shape.vertices.get(face[i + 1]));
                quat.vmult(b, b);
                quat.vmult(c, c);
                position.add(b, b);
                position.add(c, c);

                double distance = intersectPoint.distanceTo(from);

                if (!(pointInTriangle(intersectPoint, a, b, c) || pointInTriangle(intersectPoint, b, a, c))
                        || distance > fromToDistance) {
                    continue;
                }

                reportIntersection(normal, intersectPoint, reportedShape, body, fi);
            }
        }
    }

    private void _intersectTrimesh(Trimesh mesh, Quaternion quat, Vec3 position,
                                   PhysicsBody body, CollisionShape reportedShape) {
        // Trimesh nicht implementiert – für Würfel nicht benötigt
        logger.warning("_intersectTrimesh – nicht implementiert");
    }

    private void reportIntersection(Vec3 normal, Vec3 hitPointWorld, CollisionShape shape,
                                    PhysicsBody body, int hitFaceIndex) {
        double distance = from.distanceTo(hitPointWorld);

        // Rückseiten überspringen
        if (skipBackfaces && normal.dot(direction) > 0) return;

        result.hitFaceIndex = hitFaceIndex;

        switch (mode) {
            case ALL -> {
                hasHit = true;
                result.hasHit = true;
                result.set(from, to, normal, hitPointWorld, shape, body, distance);
                if (callback != null) callback.accept(result);
            }
            case CLOSEST -> {
                if (distance < result.distance || !result.hasHit) {
                    hasHit = true;
                    result.hasHit = true;
                    result.set(from, to, normal, hitPointWorld, shape, body, distance);
                }
            }
            case ANY -> {
                hasHit = true;
                result.hasHit = true;
                result.set(from, to, normal, hitPointWorld, shape, body, distance);
                result.shouldStop = true;
            }
        }
    }

    public static boolean pointInTriangle(Vec3 p, Vec3 a, Vec3 b, Vec3 c) {
        c.sub(a, v0);
        b.sub(a, v1);
        p.sub(a, v2);

        double dot00 = v0.dot(v0);
        double dot01 = v0.dot(v1);
        double dot02 = v0.dot(v2);
        double dot11 = v1.dot(v1);
        double dot12 = v1.dot(v2);

        double u = dot11 * dot02 - dot01 * dot12;
        double v = dot00 * dot12 - dot01 * dot02;

        return u >= 0 && v >= 0 && u + v < dot00 * dot11 - dot01 * dot01;
    }

    private static final Vec3 intersect = new Vec3();

    private static double distanceFromIntersection(Vec3 from, Vec3 direction, Vec3 position) {
        position.sub(from, v0);
        double dot = v0.dot(direction);

        direction.scale(dot, intersect);
        intersect.add(from, intersect);

        return position.distanceTo(intersect);
    }
}