package com.fuchsbau.shorin.Engine.Physics.World;

import com.fuchsbau.shorin.Engine.Physics.Material.ContactMaterial;
import com.fuchsbau.shorin.Engine.Physics.Material.Material;
import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.*;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.ContactEquation;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.FrictionEquation;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Narrowphase {

    private static final Logger logger = FileLogger.getLogger();

    private static final Vec3 averageNormal = new Vec3();
    private static final Vec3 averageContactPointA = new Vec3();
    private static final Vec3 averageContactPointB = new Vec3();

    private static final Vec3 point_on_plane_to_sphere = new Vec3();
    private static final Vec3 plane_to_sphere_ortho = new Vec3();

    private static final Vec3[] sphereBox_sides = {new Vec3(), new Vec3(), new Vec3(), new Vec3(), new Vec3(), new Vec3()};
    private static final Vec3 box_to_sphere = new Vec3();
    private static final Vec3 sphereBox_ns = new Vec3();
    private static final Vec3 sphereBox_ns1 = new Vec3();
    private static final Vec3 sphereBox_ns2 = new Vec3();
    private static final Vec3 sphereBox_side_ns = new Vec3();
    private static final Vec3 sphereBox_side_ns1 = new Vec3();
    private static final Vec3 sphereBox_side_ns2 = new Vec3();
    private static final Vec3 sphereBox_sphere_to_corner = new Vec3();
    private static final Vec3 convexConvex_sepAxis = new Vec3();
    private static final Vec3 convexConvex_q = new Vec3();
    private static final Vec3 convex_to_sphere = new Vec3();
    private static final Vec3 sphereConvex_worldCorner = new Vec3();
    private static final Vec3 sphereConvex_sphereToCorner = new Vec3();
    private static final Vec3 sphereConvex_worldNormal = new Vec3();
    private static final Vec3 sphereConvex_worldPoint = new Vec3();
    private static final Vec3 sphereConvex_worldSpherePointClosestToPlane = new Vec3();
    private static final Vec3 sphereConvex_penetrationVec = new Vec3();
    private static final Vec3 sphereConvex_sphereToWorldPoint = new Vec3();
    private static final Vec3 sphereConvex_edge = new Vec3();
    private static final Vec3 sphereConvex_edgeUnit = new Vec3();
    private static final Vec3 planeConvex_v = new Vec3();
    private static final Vec3 planeConvex_normal = new Vec3();
    private static final Vec3 planeConvex_relpos = new Vec3();
    private static final Vec3 planeConvex_projected = new Vec3();
    private static final Vec3 particleSphere_normal = new Vec3();
    private static final Vec3 particlePlane_normal = new Vec3();
    private static final Vec3 particlePlane_relpos = new Vec3();
    private static final Vec3 particlePlane_projected = new Vec3();
    private static final Quaternion cqj = new Quaternion(0, 0, 0, 1);
    private static final Vec3 convexParticle_local = new Vec3();
    private static final Vec3 convexParticle_penetratedFaceNormal = new Vec3();
    private static final Vec3 convexParticle_worldPenetrationVec = new Vec3();
    private static final Vec3 convexParticle_vertexToParticle = new Vec3();

    private static final Quaternion tmpQuat1 = new Quaternion(0, 0, 0, 1);
    private static final Quaternion tmpQuat2 = new Quaternion(0, 0, 0, 1);
    private static final Vec3 tmpVec1 = new Vec3();
    private static final Vec3 tmpVec2 = new Vec3();

    // Shape-Typ Konstanten – wie COLLISION_TYPES
    public static final int SPHERE = 1;
    public static final int PLANE = 2;
    public static final int BOX = 4;
    public static final int CONVEXPOLYHEDRON = 16;
    public static final int CYLINDER = 128;

    public List<ContactEquation> contactPointPool = new ArrayList<>();
    public List<FrictionEquation> frictionEquationPool = new ArrayList<>();
    public List<ContactEquation> result = new ArrayList<>();
    public List<FrictionEquation> frictionResult = new ArrayList<>();

    public World world;
    public ContactMaterial currentContactMaterial;
    public boolean enableFrictionReduction = false;

    public Narrowphase(World world) {
        this.contactPointPool = new ArrayList<>();
        this.frictionEquationPool = new ArrayList<>();
        this.result = new ArrayList<>();
        this.frictionResult = new ArrayList<>();
        this.world = world;
        this.currentContactMaterial = world.defaultContactMaterial;
        this.enableFrictionReduction = false;
    }

    public ContactEquation createContactEquation(
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape si, CollisionShape sj,
            CollisionShape overrideShapeA, CollisionShape overrideShapeB) {

        ContactEquation c;
        if (!contactPointPool.isEmpty()) {
            c = contactPointPool.remove(contactPointPool.size() - 1);
            c.bi = bi;
            c.bj = bj;
        } else {
            c = new ContactEquation(bi, bj);
        }

        c.enabled = bi.collisionResponse && bj.collisionResponse
                && si.collisionResponse && sj.collisionResponse;

        ContactMaterial cm = currentContactMaterial;
        c.restitution = cm.restitution;
        c.setSpookParams(cm.contactEquationStiffness, cm.contactEquationRelaxation, world.dt);

        // Material-Restitution überschreiben wenn vorhanden
        Material matA = si.material != null ? si.material : bi.material;
        Material matB = sj.material != null ? sj.material : bj.material;
        if (matA != null && matB != null && matA.restitution >= 0 && matB.restitution >= 0) {
            c.restitution = matA.restitution * matB.restitution;
        }

        c.si = overrideShapeA != null ? overrideShapeA : si;
        c.sj = overrideShapeB != null ? overrideShapeB : sj;

        return c;
    }

    public boolean createFrictionEquationsFromContact(ContactEquation contactEquation, List<FrictionEquation> outArray) {
        PhysicsBody bodyA = contactEquation.bi;
        PhysicsBody bodyB = contactEquation.bj;
        CollisionShape shapeA = contactEquation.si;
        CollisionShape shapeB = contactEquation.sj;

        ContactMaterial cm = currentContactMaterial;

        double friction = cm.friction;
        Material matA = shapeA.material != null ? shapeA.material : bodyA.material;
        Material matB = shapeB.material != null ? shapeB.material : bodyB.material;
        if (matA != null && matB != null && matA.friction >= 0 && matB.friction >= 0) {
            friction = matA.friction * matB.friction;
        }

        if (friction > 0) {
            Vec3 gravVec = world.frictionGravity != null ? world.frictionGravity : world.gravity;
            double mug = friction * gravVec.length();

            double reducedMass = bodyA.invMass + bodyB.invMass;
            if (reducedMass > 0) reducedMass = 1.0 / reducedMass;

            FrictionEquation c1 = frictionEquationPool.isEmpty()
                    ? new FrictionEquation(bodyA, bodyB, mug * reducedMass)
                    : frictionEquationPool.remove(frictionEquationPool.size() - 1);
            FrictionEquation c2 = frictionEquationPool.isEmpty()
                    ? new FrictionEquation(bodyA, bodyB, mug * reducedMass)
                    : frictionEquationPool.remove(frictionEquationPool.size() - 1);

            c1.bi = c2.bi = bodyA;
            c1.bj = c2.bj = bodyB;
            c1.minForce = c2.minForce = -mug * reducedMass;
            c1.maxForce = c2.maxForce = mug * reducedMass;

            c1.ri.copy(contactEquation.ri);
            c1.rj.copy(contactEquation.rj);
            c2.ri.copy(contactEquation.ri);
            c2.rj.copy(contactEquation.rj);

            contactEquation.ni.tangents(c1.t, c2.t);

            c1.setSpookParams(cm.frictionEquationStiffness, cm.frictionEquationRelaxation, world.dt);
            c2.setSpookParams(cm.frictionEquationStiffness, cm.frictionEquationRelaxation, world.dt);

            c1.enabled = c2.enabled = contactEquation.enabled;

            outArray.add(c1);
            outArray.add(c2);

            return true;
        }

        return false;
    }

    public void createFrictionFromAverage(int numContacts) {
        ContactEquation c = result.get(result.size() - 1);

        if (!createFrictionEquationsFromContact(c, frictionResult) || numContacts == 1) {
            return;
        }

        FrictionEquation f1 = frictionResult.get(frictionResult.size() - 2);
        FrictionEquation f2 = frictionResult.get(frictionResult.size() - 1);

        averageNormal.setZero();
        averageContactPointA.setZero();
        averageContactPointB.setZero();

        PhysicsBody bodyA = c.bi;

        for (int i = 0; i < numContacts; i++) {
            c = result.get(result.size() - 1 - i);
            if (c.bi != bodyA) {
                c.ni.add(averageNormal, averageNormal);
                c.ri.add(averageContactPointA, averageContactPointA);
                c.rj.add(averageContactPointB, averageContactPointB);
            } else {
                c.ni.sub(averageNormal, averageNormal);
                c.rj.add(averageContactPointA, averageContactPointA);
                c.ri.add(averageContactPointB, averageContactPointB);
            }
        }

        double invNumContacts = 1.0 / numContacts;
        averageContactPointA.scale(invNumContacts, f1.ri);
        averageContactPointB.scale(invNumContacts, f1.rj);
        f2.ri.copy(f1.ri);
        f2.rj.copy(f1.rj);
        averageNormal.normalize();
        averageNormal.tangents(f1.t, f2.t);
    }

    public void getContacts(
            List<PhysicsBody> p1, List<PhysicsBody> p2,
            World world,
            List<ContactEquation> result,
            List<ContactEquation> oldcontacts,
            List<FrictionEquation> frictionResult,
            List<FrictionEquation> frictionPool) {

        this.contactPointPool = oldcontacts;
        this.frictionEquationPool = frictionPool;
        this.result = result;
        this.frictionResult = frictionResult;

        for (int k = 0; k < p1.size(); k++) {
            PhysicsBody bi = p1.get(k);
            PhysicsBody bj = p2.get(k);

            // Contact Material bestimmen
            ContactMaterial bodyContactMaterial = null;
            if (bi.material != null && bj.material != null) {
                bodyContactMaterial = world.getContactMaterial(bi.material, bj.material);
            }

            boolean justTest = (bi.type == BodyType.KINEMATIC && bj.type == BodyType.STATIC)
                    || (bi.type == BodyType.STATIC && bj.type == BodyType.KINEMATIC)
                    || (bi.type == BodyType.KINEMATIC && bj.type == BodyType.KINEMATIC);

            for (int i = 0; i < bi.shapes.size(); i++) {
                bi.quaternion.mult(bi.shapeOrientations.get(i), tmpQuat1);
                bi.quaternion.vmult(bi.shapeOffsets.get(i), tmpVec1);
                tmpVec1.add(bi.position, tmpVec1);
                CollisionShape si = bi.shapes.get(i);

                for (int j = 0; j < bj.shapes.size(); j++) {
                    bj.quaternion.mult(bj.shapeOrientations.get(j), tmpQuat2);
                    bj.quaternion.vmult(bj.shapeOffsets.get(j), tmpVec2);
                    tmpVec2.add(bj.position, tmpVec2);
                    CollisionShape sj = bj.shapes.get(j);

                    // Kollisionsfilter prüfen
                    if ((si.collisionFilterMask & sj.collisionFilterGroup) == 0 ||
                            (sj.collisionFilterMask & si.collisionFilterGroup) == 0) {
                        continue;
                    }

                    // Bounding Sphere Vorprüfung
                    if (tmpVec1.distanceTo(tmpVec2) > si.boundingSphereRadius + sj.boundingSphereRadius) {
                        continue;
                    }

                    // Shape Contact Material
                    ContactMaterial shapeContactMaterial = null;
                    if (si.material != null && sj.material != null) {
                        shapeContactMaterial = world.getContactMaterial(si.material, sj.material);
                    }

                    currentContactMaterial = shapeContactMaterial != null ? shapeContactMaterial
                            : bodyContactMaterial != null ? bodyContactMaterial
                            : world.defaultContactMaterial;

                    // Resolver aufrufen
                    int resolverIndex = si.type.index | sj.type.index;
                    boolean retval;

                    if (si.type.index < sj.type.index) {
                        retval = resolve(resolverIndex, si, sj, tmpVec1, tmpVec2, tmpQuat1, tmpQuat2, bi, bj, si, sj, justTest);
                    } else {
                        retval = resolve(resolverIndex, sj, si, tmpVec2, tmpVec1, tmpQuat2, tmpQuat1, bj, bi, si, sj, justTest);
                    }

                    if (retval && justTest) {
                        //world.shapeOverlapKeeper.set(si.id, sj.id);
                        //world.bodyOverlapKeeper.set(bi.id, bj.id);
                    }
                }
            }
        }
    }

    // Dispatcher – wählt die richtige Kollisionsmethode anhand der Shape-Typen
    private boolean resolve(int resolverIndex,
                            CollisionShape si, CollisionShape sj,
                            Vec3 xi, Vec3 xj,
                            Quaternion qi, Quaternion qj,
                            PhysicsBody bi, PhysicsBody bj,
                            CollisionShape rsi, CollisionShape rsj,
                            boolean justTest) {

        switch (resolverIndex) {
            case PLANE | CONVEXPOLYHEDRON -> { // planeConvex = 18
                if (si instanceof Plane && sj instanceof ConvexPolyhedron) {
                    return planeConvex((Plane) si, (ConvexPolyhedron) sj, xi, xj, qi, qj, bi, bj, rsi, rsj, justTest);
                }
            }
            case CONVEXPOLYHEDRON -> { // convexConvex = 16
                if (si instanceof ConvexPolyhedron && sj instanceof ConvexPolyhedron) {
                    return convexConvex((ConvexPolyhedron) si, (ConvexPolyhedron) sj, xi, xj, qi, qj, bi, bj, rsi, rsj, justTest, null, null);
                }
            }
            case BOX | CONVEXPOLYHEDRON -> { // boxConvex = 20
                if (si instanceof Box && sj instanceof ConvexPolyhedron) {
                    return boxConvex((Box) si, (ConvexPolyhedron) sj, xi, xj, qi, qj, bi, bj, rsi, rsj, justTest);
                }
            }
            case PLANE | BOX -> { // planeBox = 6
                if (si instanceof Plane && sj instanceof Box) {
                    return planeBox((Plane) si, (Box) sj, xi, xj, qi, qj, bi, bj, rsi, rsj, justTest);
                }
            }
        }

        logger.warning("resolve – kein Resolver für Typ: " + resolverIndex);
        return false;
    }

    public boolean sphereSphere(
            Sphere si, Sphere sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        if (justTest) {
            return xi.distanceSquared(xj) < Math.pow(si.radius + sj.radius, 2);
        }

        ContactEquation contactEq = createContactEquation(bi, bj, si, sj, rsi, rsj);

        // Kontaktnormale
        xj.sub(xi, contactEq.ni);
        contactEq.ni.normalize();

        // Kontaktpunkte
        contactEq.ri.copy(contactEq.ni);
        contactEq.rj.copy(contactEq.ni);
        contactEq.ri.scale(si.radius, contactEq.ri);
        contactEq.rj.scale(-sj.radius, contactEq.rj);

        contactEq.ri.add(xi, contactEq.ri);
        contactEq.ri.sub(bi.position, contactEq.ri);

        contactEq.rj.add(xj, contactEq.rj);
        contactEq.rj.sub(bj.position, contactEq.rj);

        result.add(contactEq);
        createFrictionEquationsFromContact(contactEq, frictionResult);

        return false;
    }

    public boolean spherePlane(
            Sphere si, Plane sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);

        // Kontaktnormale
        r.ni.set(0, 0, 1);
        qj.vmult(r.ni, r.ni);
        r.ni.negate(r.ni);
        r.ni.normalize();

        // Vektor vom Kugelzentrum zum Kontaktpunkt
        r.ni.scale(si.radius, r.ri);

        // Kugel auf Ebene projizieren
        xi.sub(xj, point_on_plane_to_sphere);
        r.ni.scale(r.ni.dot(point_on_plane_to_sphere), plane_to_sphere_ortho);
        point_on_plane_to_sphere.sub(plane_to_sphere_ortho, r.rj);

        if (-point_on_plane_to_sphere.dot(r.ni) <= si.radius) {
            if (justTest) return true;

            r.ri.add(xi, r.ri);
            r.ri.sub(bi.position, r.ri);
            r.rj.add(xj, r.rj);
            r.rj.sub(bj.position, r.rj);

            result.add(r);
            createFrictionEquationsFromContact(r, frictionResult);
        }

        return false;
    }

    public boolean boxBox(
            Box si, Box sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        si.convexPolyhedronRepresentation.material = si.material;
        sj.convexPolyhedronRepresentation.material = sj.material;
        si.convexPolyhedronRepresentation.collisionResponse = si.collisionResponse;
        sj.convexPolyhedronRepresentation.collisionResponse = sj.collisionResponse;

        return convexConvex(
                si.convexPolyhedronRepresentation,
                sj.convexPolyhedronRepresentation,
                xi, xj, qi, qj, bi, bj, si, sj, justTest,
                null,null
        );
    }

    public boolean sphereBox(
            Sphere si, Box sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        xi.sub(xj, box_to_sphere);
        sj.getSideNormals(List.of(sphereBox_sides), qj);
        double R = si.radius;

        boolean found = false;
        Double side_distance = null;
        double side_dot1 = 0, side_dot2 = 0;
        double side_h = 0;
        int side_penetrations = 0;

        // Seiten prüfen
        for (int idx = 0; idx < sphereBox_sides.length && !found; idx++) {
            Vec3 ns = sphereBox_ns;
            ns.copy(sphereBox_sides[idx]);
            double h = ns.length();
            ns.normalize();
            double dot = box_to_sphere.dot(ns);

            if (dot < h + R && dot > 0) {
                Vec3 ns1 = sphereBox_ns1;
                Vec3 ns2 = sphereBox_ns2;
                ns1.copy(sphereBox_sides[(idx + 1) % 3]);
                ns2.copy(sphereBox_sides[(idx + 2) % 3]);
                double h1 = ns1.length(), h2 = ns2.length();
                ns1.normalize();
                ns2.normalize();
                double dot1 = box_to_sphere.dot(ns1);
                double dot2 = box_to_sphere.dot(ns2);

                if (dot1 < h1 && dot1 > -h1 && dot2 < h2 && dot2 > -h2) {
                    double dist = Math.abs(dot - h - R);
                    if (side_distance == null || dist < side_distance) {
                        side_distance = dist;
                        side_dot1 = dot1;
                        side_dot2 = dot2;
                        side_h = h;
                        sphereBox_side_ns.copy(ns);
                        sphereBox_side_ns1.copy(ns1);
                        sphereBox_side_ns2.copy(ns2);
                        side_penetrations++;
                        if (justTest) return true;
                    }
                }
            }
        }

        if (side_penetrations > 0) {
            found = true;
            ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
            sphereBox_side_ns.scale(-R, r.ri);
            r.ni.copy(sphereBox_side_ns);
            r.ni.negate(r.ni);
            sphereBox_side_ns.scale(side_h, sphereBox_side_ns);
            sphereBox_side_ns1.scale(side_dot1, sphereBox_side_ns1);
            sphereBox_side_ns.add(sphereBox_side_ns1, sphereBox_side_ns);
            sphereBox_side_ns2.scale(side_dot2, sphereBox_side_ns2);
            sphereBox_side_ns.add(sphereBox_side_ns2, r.rj);

            r.ri.add(xi, r.ri);
            r.ri.sub(bi.position, r.ri);
            r.rj.add(xj, r.rj);
            r.rj.sub(bj.position, r.rj);

            result.add(r);
            createFrictionEquationsFromContact(r, frictionResult);
        }

        // Ecken prüfen
        Vec3 rj = new Vec3();
        for (int j = 0; j < 2 && !found; j++) {
            for (int k = 0; k < 2 && !found; k++) {
                for (int l = 0; l < 2 && !found; l++) {
                    rj.set(0, 0, 0);
                    if (j > 0) rj.add(sphereBox_sides[0], rj);
                    else rj.sub(sphereBox_sides[0], rj);
                    if (k > 0) rj.add(sphereBox_sides[1], rj);
                    else rj.sub(sphereBox_sides[1], rj);
                    if (l > 0) rj.add(sphereBox_sides[2], rj);
                    else rj.sub(sphereBox_sides[2], rj);

                    xj.add(rj, sphereBox_sphere_to_corner);
                    sphereBox_sphere_to_corner.sub(xi, sphereBox_sphere_to_corner);

                    if (sphereBox_sphere_to_corner.lengthSquared() < R * R) {
                        if (justTest) return true;
                        found = true;
                        ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
                        r.ri.copy(sphereBox_sphere_to_corner);
                        r.ri.normalize();
                        r.ni.copy(r.ri);
                        r.ri.scale(R, r.ri);
                        r.rj.copy(rj);

                        r.ri.add(xi, r.ri);
                        r.ri.sub(bi.position, r.ri);
                        r.rj.add(xj, r.rj);
                        r.rj.sub(bj.position, r.rj);

                        result.add(r);
                        createFrictionEquationsFromContact(r, frictionResult);
                    }
                }
            }
        }

        // Kanten prüfen
        Vec3 edgeTangent = new Vec3();
        Vec3 edgeCenter = new Vec3();
        Vec3 rv = new Vec3();
        Vec3 orthogonal = new Vec3();
        Vec3 dist = new Vec3();

        for (int j = 0; j < sphereBox_sides.length && !found; j++) {
            for (int k = 0; k < sphereBox_sides.length && !found; k++) {
                if (j % 3 != k % 3) {
                    sphereBox_sides[k].cross(sphereBox_sides[j], edgeTangent);
                    edgeTangent.normalize();
                    sphereBox_sides[j].add(sphereBox_sides[k], edgeCenter);
                    rv.copy(xi);
                    rv.sub(edgeCenter, rv);
                    rv.sub(xj, rv);
                    double orthonorm = rv.dot(edgeTangent);
                    edgeTangent.scale(orthonorm, orthogonal);

                    int lIdx = 0;
                    while (lIdx == j % 3 || lIdx == k % 3) lIdx++;

                    dist.copy(xi);
                    dist.sub(orthogonal, dist);
                    dist.sub(edgeCenter, dist);
                    dist.sub(xj, dist);

                    double tdist = Math.abs(orthonorm);
                    double ndist = dist.length();

                    if (tdist < sphereBox_sides[lIdx].length() && ndist < R) {
                        if (justTest) return true;
                        found = true;
                        ContactEquation res = createContactEquation(bi, bj, si, sj, rsi, rsj);
                        edgeCenter.add(orthogonal, res.rj);
                        dist.negate(res.ni);
                        res.ni.normalize();
                        res.ri.copy(res.rj);
                        res.ri.add(xj, res.ri);
                        res.ri.sub(xi, res.ri);
                        res.ri.normalize();
                        res.ri.scale(R, res.ri);

                        res.ri.add(xi, res.ri);
                        res.ri.sub(bi.position, res.ri);
                        res.rj.add(xj, res.rj);
                        res.rj.sub(bj.position, res.rj);

                        result.add(res);
                        createFrictionEquationsFromContact(res, frictionResult);
                    }
                }
            }
        }

        return false;
    }

    public boolean planeBox(
            Plane si, Box sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        sj.convexPolyhedronRepresentation.material = sj.material;
        sj.convexPolyhedronRepresentation.collisionResponse = sj.collisionResponse;
        sj.convexPolyhedronRepresentation.id = sj.id;

        return planeConvex(si, sj.convexPolyhedronRepresentation, xi, xj, qi, qj, bi, bj, si, sj, justTest);
    }

    public boolean convexConvex(
            ConvexPolyhedron si, ConvexPolyhedron sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest,
            int[] faceListA, int[] faceListB) {

        if (xi.distanceTo(xj) > si.boundingSphereRadius + sj.boundingSphereRadius) {
            return false;
        }

        if (si.findSeparatingAxis(sj, xi, qi, xj, qj, convexConvex_sepAxis, faceListA, faceListB)) {
            List<ConvexPolyhedronContactPoint> res = new ArrayList<>();
            si.clipAgainstHull(xi, qi, sj, xj, qj, convexConvex_sepAxis, -100, 100, res);

            int numContacts = 0;
            for (ConvexPolyhedronContactPoint cp : res) {
                if (justTest) return true;

                ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
                Vec3 ri = r.ri;
                Vec3 rj = r.rj;

                convexConvex_sepAxis.negate(r.ni);
                cp.normal.negate(convexConvex_q);
                convexConvex_q.scale(cp.depth, convexConvex_q);
                cp.point.add(convexConvex_q, ri);
                rj.copy(cp.point);

                ri.sub(xi, ri);
                rj.sub(xj, rj);

                ri.add(xi, ri);
                ri.sub(bi.position, ri);
                rj.add(xj, rj);
                rj.sub(bj.position, rj);

                result.add(r);
                numContacts++;

                if (!enableFrictionReduction) {
                    createFrictionEquationsFromContact(r, frictionResult);
                }
            }

            if (enableFrictionReduction && numContacts > 0) {
                createFrictionFromAverage(numContacts);
            }
        }

        return false;
    }

    public boolean sphereConvex(
            Sphere si, ConvexPolyhedron sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        xi.sub(xj, convex_to_sphere);
        double R = si.radius;
        boolean found = false;

        // Ecken prüfen
        for (Vec3 v : sj.vertices) {
            qj.vmult(v, sphereConvex_worldCorner);
            xj.add(sphereConvex_worldCorner, sphereConvex_worldCorner);
            sphereConvex_worldCorner.sub(xi, sphereConvex_sphereToCorner);

            if (sphereConvex_sphereToCorner.lengthSquared() < R * R) {
                if (justTest) return true;
                found = true;
                ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
                r.ri.copy(sphereConvex_sphereToCorner);
                r.ri.normalize();
                r.ni.copy(r.ri);
                r.ri.scale(R, r.ri);
                sphereConvex_worldCorner.sub(xj, r.rj);

                r.ri.add(xi, r.ri);
                r.ri.sub(bi.position, r.ri);
                r.rj.add(xj, r.rj);
                r.rj.sub(bj.position, r.rj);

                result.add(r);
                createFrictionEquationsFromContact(r, frictionResult);
                return false;
            }
        }

        // Seiten prüfen
        for (int i = 0; i < sj.faces.size() && !found; i++) {
            Vec3 normal = sj.faceNormals.get(i);
            int[] face = sj.faces.get(i);

            qj.vmult(normal, sphereConvex_worldNormal);
            qj.vmult(sj.vertices.get(face[0]), sphereConvex_worldPoint);
            sphereConvex_worldPoint.add(xj, sphereConvex_worldPoint);

            sphereConvex_worldNormal.scale(-R, sphereConvex_worldSpherePointClosestToPlane);
            xi.add(sphereConvex_worldSpherePointClosestToPlane, sphereConvex_worldSpherePointClosestToPlane);

            sphereConvex_worldSpherePointClosestToPlane.sub(sphereConvex_worldPoint, sphereConvex_penetrationVec);
            double penetration = sphereConvex_penetrationVec.dot(sphereConvex_worldNormal);

            xi.sub(sphereConvex_worldPoint, sphereConvex_sphereToWorldPoint);

            if (penetration < 0 && sphereConvex_sphereToWorldPoint.dot(sphereConvex_worldNormal) > 0) {
                List<Vec3> faceVerts = new ArrayList<>();
                for (int fv : face) {
                    Vec3 worldVertex = new Vec3();
                    qj.vmult(sj.vertices.get(fv), worldVertex);
                    xj.add(worldVertex, worldVertex);
                    faceVerts.add(worldVertex);
                }

                if (pointInPolygon(faceVerts, sphereConvex_worldNormal, xi)) {
                    if (justTest) return true;
                    found = true;
                    ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
                    sphereConvex_worldNormal.scale(-R, r.ri);
                    sphereConvex_worldNormal.negate(r.ni);

                    Vec3 penetrationVec2 = new Vec3();
                    Vec3 penetrationSpherePoint = new Vec3();
                    sphereConvex_worldNormal.scale(-penetration, penetrationVec2);
                    sphereConvex_worldNormal.scale(-R, penetrationSpherePoint);

                    xi.sub(xj, r.rj);
                    r.rj.add(penetrationSpherePoint, r.rj);
                    r.rj.add(penetrationVec2, r.rj);

                    r.rj.add(xj, r.rj);
                    r.rj.sub(bj.position, r.rj);
                    r.ri.add(xi, r.ri);
                    r.ri.sub(bi.position, r.ri);

                    result.add(r);
                    createFrictionEquationsFromContact(r, frictionResult);
                    return false;
                } else {
                    // Kanten prüfen
                    for (int j = 0; j < face.length; j++) {
                        Vec3 v1 = new Vec3(), v2 = new Vec3();
                        qj.vmult(sj.vertices.get(face[(j + 1) % face.length]), v1);
                        qj.vmult(sj.vertices.get(face[(j + 2) % face.length]), v2);
                        xj.add(v1, v1);
                        xj.add(v2, v2);

                        v2.sub(v1, sphereConvex_edge);
                        sphereConvex_edge.unit(sphereConvex_edgeUnit);

                        Vec3 p = new Vec3();
                        Vec3 v1_to_xi = new Vec3();
                        xi.sub(v1, v1_to_xi);
                        double dot = v1_to_xi.dot(sphereConvex_edgeUnit);
                        sphereConvex_edgeUnit.scale(dot, p);
                        p.add(v1, p);

                        Vec3 xi_to_p = new Vec3();
                        p.sub(xi, xi_to_p);

                        if (dot > 0 && dot * dot < sphereConvex_edge.lengthSquared() && xi_to_p.lengthSquared() < R * R) {
                            if (justTest) return true;
                            ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
                            p.sub(xj, r.rj);
                            p.sub(xi, r.ni);
                            r.ni.normalize();
                            r.ni.scale(R, r.ri);

                            r.rj.add(xj, r.rj);
                            r.rj.sub(bj.position, r.rj);
                            r.ri.add(xi, r.ri);
                            r.ri.sub(bi.position, r.ri);

                            result.add(r);
                            createFrictionEquationsFromContact(r, frictionResult);
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean planeConvex(
            Plane planeShape, ConvexPolyhedron convexShape,
            Vec3 planePosition, Vec3 convexPosition,
            Quaternion planeQuat, Quaternion convexQuat,
            PhysicsBody planeBody, PhysicsBody convexBody,
            CollisionShape si, CollisionShape sj,
            boolean justTest) {

        Vec3 worldVertex = planeConvex_v;
        Vec3 worldNormal = planeConvex_normal;

        worldNormal.set(0, 0, 1);
        planeQuat.vmult(worldNormal, worldNormal);

        int numContacts = 0;

        for (Vec3 vertex : convexShape.vertices) {
            worldVertex.copy(vertex);
            convexQuat.vmult(worldVertex, worldVertex);
            convexPosition.add(worldVertex, worldVertex);
            worldVertex.sub(planePosition, planeConvex_relpos);

            double dot = worldNormal.dot(planeConvex_relpos);
            if (dot <= 0.0) {
                if (justTest) return true;

                ContactEquation r = createContactEquation(planeBody, convexBody, planeShape, convexShape, si, sj);

                // Vertex auf Ebene projizieren
                worldNormal.scale(worldNormal.dot(planeConvex_relpos), planeConvex_projected);
                worldVertex.sub(planeConvex_projected, planeConvex_projected);
                planeConvex_projected.sub(planePosition, r.ri);

                r.ni.copy(worldNormal);

                // rj = Vektor vom Konvex-Zentrum zum Vertex
                worldVertex.sub(convexPosition, r.rj);

                // Relativ zu Bodies machen
                r.ri.add(planePosition, r.ri);
                r.ri.sub(planeBody.position, r.ri);
                r.rj.add(convexPosition, r.rj);
                r.rj.sub(convexBody.position, r.rj);

                result.add(r);
                numContacts++;

                if (!enableFrictionReduction) {
                    createFrictionEquationsFromContact(r, frictionResult);
                }
            }
        }

        if (enableFrictionReduction && numContacts > 0) {
            createFrictionFromAverage(numContacts);
        }

        return false;
    }

    public boolean boxConvex(
            Box si, ConvexPolyhedron sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        si.convexPolyhedronRepresentation.material = si.material;
        si.convexPolyhedronRepresentation.collisionResponse = si.collisionResponse;

        return convexConvex(si.convexPolyhedronRepresentation, sj, xi, xj, qi, qj, bi, bj, si, sj, justTest, null, null);
    }

    public boolean sphereHeightfield(
            Sphere sphereShape, Heightfield hfShape,
            Vec3 spherePos, Vec3 hfPos,
            Quaternion sphereQuat, Quaternion hfQuat,
            PhysicsBody sphereBody, PhysicsBody hfBody,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {
        // Heightfield nicht implementiert – für Würfel nicht benötigt
        logger.warning("sphereHeightfield – nicht implementiert");
        return false;
    }

    public boolean boxHeightfield(
            Box si, Heightfield sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {
        // Heightfield nicht implementiert – für Würfel nicht benötigt
        logger.warning("boxHeightfield – nicht implementiert");
        return false;
    }

    public boolean convexHeightfield(
            ConvexPolyhedron convexShape, Heightfield hfShape,
            Vec3 convexPos, Vec3 hfPos,
            Quaternion convexQuat, Quaternion hfQuat,
            PhysicsBody convexBody, PhysicsBody hfBody,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {
        // Heightfield nicht implementiert – für Würfel nicht benötigt
        logger.warning("convexHeightfield – nicht implementiert");
        return false;
    }

    public boolean sphereParticle(
            Sphere sj, Particle si,
            Vec3 xj, Vec3 xi,
            Quaternion qj, Quaternion qi,
            PhysicsBody bj, PhysicsBody bi,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        Vec3 normal = particleSphere_normal;
        normal.set(0, 0, 1);
        xi.sub(xj, normal);
        double lengthSquared = normal.lengthSquared();

        if (lengthSquared <= sj.radius * sj.radius) {
            if (justTest) return true;

            ContactEquation r = createContactEquation(bi, bj, (CollisionShape) si, sj, rsi, rsj);
            normal.normalize();
            r.rj.copy(normal);
            r.rj.scale(sj.radius, r.rj);
            r.ni.copy(normal);
            r.ni.negate(r.ni);
            r.ri.set(0, 0, 0);

            result.add(r);
            createFrictionEquationsFromContact(r, frictionResult);
        }

        return false;
    }

    public boolean planeParticle(
            Plane sj, Particle si,
            Vec3 xj, Vec3 xi,
            Quaternion qj, Quaternion qi,
            PhysicsBody bj, PhysicsBody bi,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        particlePlane_normal.set(0, 0, 1);
        bj.quaternion.vmult(particlePlane_normal, particlePlane_normal);
        xi.sub(bj.position, particlePlane_relpos);
        double dot = particlePlane_normal.dot(particlePlane_relpos);

        if (dot <= 0.0) {
            if (justTest) return true;

            ContactEquation r = createContactEquation(bi, bj, si, sj, rsi, rsj);
            r.ni.copy(particlePlane_normal);
            r.ni.negate(r.ni);
            r.ri.set(0, 0, 0);

            particlePlane_normal.scale(particlePlane_normal.dot(xi), particlePlane_projected);
            xi.sub(particlePlane_projected, particlePlane_projected);
            r.rj.copy(particlePlane_projected);

            result.add(r);
            createFrictionEquationsFromContact(r, frictionResult);
        }

        return false;
    }

    public boolean boxParticle(
            Box si, Particle sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        si.convexPolyhedronRepresentation.material = si.material;
        si.convexPolyhedronRepresentation.collisionResponse = si.collisionResponse;

        return convexParticle(si.convexPolyhedronRepresentation, sj, xi, xj, qi, qj, bi, bj, si, sj, justTest);
    }

    public boolean convexParticle(
            ConvexPolyhedron sj, Object si,
            Vec3 xj, Vec3 xi,
            Quaternion qj, Quaternion qi,
            PhysicsBody bj, PhysicsBody bi,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        int penetratedFaceIndex = -1;
        Double minPenetration = null;

        // Partikelposition in lokale Konvex-Koordinaten umrechnen
        convexParticle_local.copy(xi);
        convexParticle_local.sub(xj, convexParticle_local);
        qj.conjugate(cqj);
        cqj.vmult(convexParticle_local, convexParticle_local);

        if (sj.pointIsInside(convexParticle_local) != 0) {
            if (sj.worldVerticesNeedsUpdate) sj.computeWorldVertices(xj, qj);
            if (sj.worldFaceNormalsNeedsUpdate) sj.computeWorldFaceNormals(qj);

            for (int i = 0; i < sj.faces.size(); i++) {
                Vec3 vert = sj.worldVertices.get(sj.faces.get(i)[0]);
                Vec3 normal = sj.worldFaceNormals.get(i);

                xi.sub(vert, convexParticle_vertexToParticle);
                double penetration = -normal.dot(convexParticle_vertexToParticle);

                if (minPenetration == null || Math.abs(penetration) < Math.abs(minPenetration)) {
                    if (justTest) return true;
                    minPenetration = penetration;
                    penetratedFaceIndex = i;
                    convexParticle_penetratedFaceNormal.copy(normal);
                }
            }

            if (penetratedFaceIndex != -1) {
                ContactEquation r = createContactEquation(bi, bj, (CollisionShape) si, sj, rsi, rsj);

                convexParticle_penetratedFaceNormal.scale(minPenetration, convexParticle_worldPenetrationVec);
                convexParticle_worldPenetrationVec.add(xi, convexParticle_worldPenetrationVec);
                convexParticle_worldPenetrationVec.sub(xj, convexParticle_worldPenetrationVec);
                r.rj.copy(convexParticle_worldPenetrationVec);

                convexParticle_penetratedFaceNormal.negate(r.ni);
                r.ri.set(0, 0, 0);

                r.ri.add(xi, r.ri);
                r.ri.sub(bi.position, r.ri);
                r.rj.add(xj, r.rj);
                r.rj.sub(bj.position, r.rj);

                result.add(r);
                createFrictionEquationsFromContact(r, frictionResult);
            } else {
                logger.warning("convexParticle – Punkt in Konvex, aber keine penetrierende Fläche gefunden!");
            }
        }

        return false;
    }

    public boolean heightfieldCylinder(
            Particle hfShape, Cylinder convexShape,
            Vec3 hfPos, Vec3 convexPos,
            Quaternion hfQuat, Quaternion convexQuat,
            PhysicsBody hfBody, PhysicsBody convexBody,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {
        // Heightfield nicht implementiert – für Würfel nicht benötigt
        logger.warning("heightfieldCylinder – nicht implementiert");
        return false;
    }

    public boolean particleCylinder(
            Particle si, Cylinder sj,
            Vec3 xi, Vec3 xj,
            Quaternion qi, Quaternion qj,
            PhysicsBody bi, PhysicsBody bj,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {

        return convexParticle(sj, si, xj, xi, qj, qi, bj, bi, rsi, rsj, justTest);
    }

    public boolean sphereTrimesh(
            Sphere sphereShape, /*trimeshShape Trimesh,*/
            Vec3 spherePos, Vec3 trimeshPos,
            Quaternion sphereQuat, Quaternion trimeshQuat,
            PhysicsBody sphereBody, PhysicsBody trimeshBody,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {
        // Trimesh nicht implementiert – für Würfel nicht benötigt
        logger.warning("sphereTrimesh – nicht implementiert");
        return false;
    }

    public boolean planeTrimesh(
            Plane planeShape, Trimesh trimeshShape,
            Vec3 planePos, Vec3 trimeshPos,
            Quaternion planeQuat, Quaternion trimeshQuat,
            PhysicsBody planeBody, PhysicsBody trimeshBody,
            CollisionShape rsi, CollisionShape rsj,
            boolean justTest) {
        logger.warning("planeTrimesh – nicht implementiert");
        return false;
    }


    private static int numWarnings = 0;
    private static final int maxWarnings = 10;

    private static final Vec3 pointInPolygon_edge = new Vec3();
    private static final Vec3 pointInPolygon_edge_x_normal = new Vec3();
    private static final Vec3 pointInPolygon_vtp = new Vec3();

    private void warn(String msg) {
        if (numWarnings > maxWarnings) return;
        numWarnings++;
        logger.warning(msg);
    }

    private boolean pointInPolygon(List<Vec3> verts, Vec3 normal, Vec3 p) {
        Boolean positiveResult = null;
        int N = verts.size();

        for (int i = 0; i < N; i++) {
            Vec3 v = verts.get(i);

            verts.get((i + 1) % N).sub(v, pointInPolygon_edge);
            pointInPolygon_edge.cross(normal, pointInPolygon_edge_x_normal);
            p.sub(v, pointInPolygon_vtp);

            double r = pointInPolygon_edge_x_normal.dot(pointInPolygon_vtp);

            if (positiveResult == null || (r > 0 && positiveResult) || (r <= 0 && !positiveResult)) {
                if (positiveResult == null) positiveResult = r > 0;
            } else {
                return false;
            }
        }

        return true;
    }
}