package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.BodyType;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Shape.SleepState;
import com.fuchsbau.shorin.Engine.Physics.World.World;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class Broadphase {
    private final Logger logger = FileLogger.getLogger();

    private static final Vec3 Broadphase_collisionPairs_r = new Vec3();
    private static final Map<String, Integer> makePairsUnique_temp = new HashMap<>();

    public World world;
    public boolean useBoundingBoxes = false;
    public boolean dirty = true;

    public Broadphase() {
        this.world = null;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void collisionPairs(World world, List<PhysicsBody> p1, List<PhysicsBody> p2) {
        throw new UnsupportedOperationException("collisionPairs() nicht implementiert für: " + getClass().getSimpleName());
    }

    public boolean needBroadphaseCollision(PhysicsBody bodyA, PhysicsBody bodyB) {
        // Kollisionsfilter prüfen
        if ((bodyA.collisionFilterGroup & bodyB.collisionFilterMask) == 0 ||
                (bodyB.collisionFilterGroup & bodyA.collisionFilterMask) == 0) {
            return false;
        }

        // Beide static oder sleeping – überspringen
        if (((bodyA.type == BodyType.STATIC) || bodyA.sleepState == SleepState.SLEEPING) &&
                ((bodyB.type == BodyType.STATIC) || bodyB.sleepState == SleepState.SLEEPING)) {
            return false;
        }

        return true;
    }

    public void intersectionTest(PhysicsBody bodyA, PhysicsBody bodyB,
                                 List<PhysicsBody> pairs1, List<PhysicsBody> pairs2) {
        if (useBoundingBoxes) {
            doBoundingBoxBroadphase(bodyA, bodyB, pairs1, pairs2);
        } else {
            doBoundingSphereBroadphase(bodyA, bodyB, pairs1, pairs2);
        }
    }

    public void doBoundingSphereBroadphase(PhysicsBody bodyA, PhysicsBody bodyB,
                                           List<PhysicsBody> pairs1, List<PhysicsBody> pairs2) {
        bodyB.position.sub(bodyA.position, Broadphase_collisionPairs_r);
        double boundingRadiusSum2 = Math.pow(bodyA.boundingRadius + bodyB.boundingRadius, 2);
        double norm2 = Broadphase_collisionPairs_r.lengthSquared();
        if (norm2 < boundingRadiusSum2) {
            pairs1.add(bodyA);
            pairs2.add(bodyB);
        }
    }

    public void doBoundingBoxBroadphase(PhysicsBody bodyA, PhysicsBody bodyB,
                                        List<PhysicsBody> pairs1, List<PhysicsBody> pairs2) {
        if (bodyA.aabbNeedsUpdate) bodyA.updateAABB();
        if (bodyB.aabbNeedsUpdate) bodyB.updateAABB();

        if (bodyA.aabb.overlaps(bodyB.aabb)) {
            pairs1.add(bodyA);
            pairs2.add(bodyB);
        }
    }

    public void makePairsUnique(List<PhysicsBody> pairs1, List<PhysicsBody> pairs2) {
        int N = pairs1.size();

        List<PhysicsBody> p1 = new ArrayList<>(pairs1);
        List<PhysicsBody> p2 = new ArrayList<>(pairs2);

        pairs1.clear();
        pairs2.clear();

        Map<String, Integer> t = makePairsUnique_temp;
        t.clear();

        for (int i = 0; i < N; i++) {
            int id1 = p1.get(i).id;
            int id2 = p2.get(i).id;
            String key = id1 < id2 ? id1 + "," + id2 : id2 + "," + id1;
            t.put(key, i);
        }

        for (Map.Entry<String, Integer> entry : t.entrySet()) {
            int pairIndex = entry.getValue();
            pairs1.add(p1.get(pairIndex));
            pairs2.add(p2.get(pairIndex));
        }
    }

    public static boolean boundingSphereCheck(PhysicsBody bodyA, PhysicsBody bodyB) {
        Vec3 dist = new Vec3();
        bodyA.position.sub(bodyB.position, dist);
        double sa = bodyA.shapes.get(0).boundingSphereRadius;
        double sb = bodyB.shapes.get(0).boundingSphereRadius;
        return Math.pow(sa + sb, 2) > dist.lengthSquared();
    }

    public List<PhysicsBody> aabbQuery(World world, WorldBoundingBox aabb, List<PhysicsBody> result) {
        logger.warning("aabbQuery() nicht implementiert in: " + getClass().getSimpleName());
        return new ArrayList<>();
    }
}