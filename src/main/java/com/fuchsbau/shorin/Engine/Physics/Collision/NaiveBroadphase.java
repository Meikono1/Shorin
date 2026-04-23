package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.World.World;

import java.util.ArrayList;
import java.util.List;

public class NaiveBroadphase extends Broadphase {

    public NaiveBroadphase() {
        super();
    }

    // Naive N² Kollisionsprüfung
    @Override
    public void collisionPairs(World world, List<PhysicsBody> pairs1, List<PhysicsBody> pairs2) {
        List<PhysicsBody> bodies = world.bodies;
        int n = bodies.size();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < i; j++) {
                PhysicsBody bi = bodies.get(i);
                PhysicsBody bj = bodies.get(j);

                if (!needBroadphaseCollision(bi, bj)) continue;

                intersectionTest(bi, bj, pairs1, pairs2);
            }
        }
    }

    @Override
    public List<PhysicsBody> aabbQuery(World world, WorldBoundingBox aabb, List<PhysicsBody> result) {
        if (result == null) result = new ArrayList<>();

        for (PhysicsBody b : world.bodies) {
            if (b.aabbNeedsUpdate) b.updateAABB();
            if (b.aabb.overlaps(aabb)) result.add(b);
        }

        return result;
    }
}