package com.fuchsbau.shorin.Engine.Dice;

import com.fuchsbau.shorin.Engine.CustomMesh.Vec3;
import com.fuchsbau.shorin.Engine.Util.FloorContact;

import java.util.ArrayList;
import java.util.List;

public final class DicePhysics {

    private DicePhysics() {
    }

    public static FloorContact resolveFloorCollision(
            DiceObject die,
            double dt,
            double linearFriction,
            double angularFriction,
            double floorBounce
    ) {
        List<Vec3> worldVertices = getWorldVertices(die);

        double minZ = Double.POSITIVE_INFINITY;
        int contactCount = 0;

        Vec3 deepestVertex = null;
        double sumX = 0.0;
        double sumY = 0.0;
        double sumZ = 0.0;

        for (Vec3 v : worldVertices) {
            if (v.z() < minZ) {
                minZ = v.z();
                deepestVertex = v;
            }

            if (v.z() <= 0.0) {
                contactCount++;
                sumX += v.x();
                sumY += v.y();
                sumZ += v.z();
            }
        }

        if (minZ >= 0.0 || deepestVertex == null) {
            return new FloorContact(false, 0, 0.0);
        }

        double penetration = -minZ;

        // Positionskorrektur
        double slop = 0.5;
        double correction = Math.max(0.0, penetration - slop);
        die.setCenterZ(die.getCenterZ() + correction);

        Vec3 center = new Vec3(die.getCenterX(), die.getCenterY(), die.getCenterZ());

        Vec3 contactPoint;
        if (contactCount > 0) {
            contactPoint = new Vec3(
                    sumX / contactCount,
                    sumY / contactCount,
                    sumZ / contactCount + penetration
            );
        } else {
            contactPoint = new Vec3(
                    deepestVertex.x(),
                    deepestVertex.y(),
                    deepestVertex.z() + penetration
            );
        }

        Vec3 r = contactPoint.sub(center);
        Vec3 n = new Vec3(0, 0, 1);

        Vec3 vLinear = new Vec3(
                die.getVelocityX(),
                die.getVelocityY(),
                die.getVelocityZ()
        );

        Vec3 omega = new Vec3(
                Math.toRadians(die.getAngularVelocityX()),
                Math.toRadians(die.getAngularVelocityY()),
                Math.toRadians(die.getAngularVelocityZ())
        );

        // Geschwindigkeit am Kontaktpunkt
        Vec3 vContact = vLinear.add(omega.cross(r));

        double vn = vContact.dot(n);

        // Nur reagieren, wenn der Kontaktpunkt in den Boden hinein läuft
        if (vn >= 0.0) {
            applyGroundDamping(die, dt, linearFriction, angularFriction, contactCount);
            return new FloorContact(true, contactCount, penetration);
        }

        Vec3 vNormal = n.scale(vn);
        Vec3 vTangent = vContact.sub(vNormal);

        // Einfache Masse/Inertia-Näherung
        double mass = 1.0;
        double radius = Math.max(1.0, die.getRadius() * die.getScale());
        double inertia = 0.4 * mass * radius * radius; // Kugel-Näherung als einfacher Start

        Vec3 rn = r.cross(n);
        double invMass = 1.0 / mass;
        double angularTerm = rn.dot(rn) / inertia;

        double impactSpeed = -vn;
        boolean restingContact =
                impactSpeed < 35.0 &&
                        contactCount >= 3 &&
                        Math.abs(die.getVelocityZ()) < 25.0 &&
                        Math.abs(die.getAngularVelocityX()) < 120.0 &&
                        Math.abs(die.getAngularVelocityY()) < 120.0 &&
                        Math.abs(die.getAngularVelocityZ()) < 120.0;

        double restitution;
        if (restingContact) {
            restitution = 0.0;
        } else if (impactSpeed < 80.0) {
            restitution = floorBounce * 0.25;
        } else {
            restitution = floorBounce / (1.0 + Math.max(0, contactCount - 1) * 0.20);
        }

        // Normalimpuls
        double jn = -(1.0 + restitution) * vn;
        jn /= (invMass + angularTerm);

        Vec3 impulseN = n.scale(jn);

        // Reibung: Coulomb-artig
        Vec3 tangentDir = vTangent.length() > 1e-6 ? vTangent.normalize() : new Vec3(0, 0, 0);

        double frictionCoeff = Math.min(1.2, 0.18 + linearFriction * 0.25 + contactCount * 0.08);

        Vec3 rt = r.cross(tangentDir);
        double tangentAngularTerm = rt.dot(rt) / inertia;
        double jt = 0.0;

        if (vTangent.length() > 1e-6) {
            jt = -vTangent.length();
            jt /= (invMass + tangentAngularTerm);

            double maxFrictionImpulse = frictionCoeff * jn;
            if (jt < -maxFrictionImpulse) jt = -maxFrictionImpulse;
            if (jt > maxFrictionImpulse) jt = maxFrictionImpulse;
        }

        if (restingContact) {
            jt *= 0.35;
        }

        Vec3 impulseT = tangentDir.scale(jt);
        Vec3 impulse = impulseN.add(impulseT);

        // Linear velocity updaten
        Vec3 newLinear = vLinear.add(impulse.scale(invMass));

        die.setVelocityX(newLinear.x());
        die.setVelocityY(newLinear.y());
        die.setVelocityZ(newLinear.z());

        // Angular velocity updaten
        Vec3 deltaOmega = r.cross(impulse).scale(1.0 / inertia);
        Vec3 newOmega = omega.add(deltaOmega);

        die.setAngularVelocityX(Math.toDegrees(newOmega.x()));
        die.setAngularVelocityY(Math.toDegrees(newOmega.y()));
        die.setAngularVelocityZ(Math.toDegrees(newOmega.z()));

        if (restingContact) {
            die.setVelocityZ(0.0);

            die.setAngularVelocityX(applyDecay(die.getAngularVelocityX(), angularFriction * 4.0, dt));
            die.setAngularVelocityY(applyDecay(die.getAngularVelocityY(), angularFriction * 4.0, dt));
            die.setAngularVelocityZ(applyDecay(die.getAngularVelocityZ(), angularFriction * 4.5, dt));

            die.setVelocityX(applyDecay(die.getVelocityX(), linearFriction * 2.8, dt));
            die.setVelocityY(applyDecay(die.getVelocityY(), linearFriction * 2.8, dt));
        }

        // Zusatzdämpfung bei Flächenkontakt, damit er eher auf einer Fläche endet
        applyGroundDamping(die, dt, linearFriction, angularFriction, contactCount);

        // Wenn er fast ruhig ist und mehrere Punkte aufliegen, vertikale Energie hart beenden
        if (contactCount >= 3 && Math.abs(die.getVelocityZ()) < 10.0) {
            die.setVelocityZ(0.0);
            die.setAngularVelocityX(applyDecay(die.getAngularVelocityX(), angularFriction * 2.2, dt));
            die.setAngularVelocityY(applyDecay(die.getAngularVelocityY(), angularFriction * 2.2, dt));
            die.setAngularVelocityZ(applyDecay(die.getAngularVelocityZ(), angularFriction * 2.4, dt));
        }

        return new FloorContact(true, contactCount, penetration);
    }

    private static void applyGroundDamping(
            DiceObject die,
            double dt,
            double linearFriction,
            double angularFriction,
            int contactCount
    ) {
        double linear = linearFriction * (1.0 + Math.max(0, contactCount - 1) * 0.35);
        double angular = angularFriction * (1.0 + Math.max(0, contactCount - 1) * 0.70);

        die.setVelocityX(applyDecay(die.getVelocityX(), linear, dt));
        die.setVelocityY(applyDecay(die.getVelocityY(), linear, dt));

        die.setAngularVelocityX(applyDecay(die.getAngularVelocityX(), angular, dt));
        die.setAngularVelocityY(applyDecay(die.getAngularVelocityY(), angular, dt));
        die.setAngularVelocityZ(applyDecay(die.getAngularVelocityZ(), angular * 1.15, dt));
    }

    public static boolean isGroundedByVertices(DiceObject die) {
        for (Vec3 v : getWorldVertices(die)) {
            if (v.z() <= 0.001) {
                return true;
            }
        }
        return false;
    }

    public static List<Vec3> getWorldVertices(DiceObject die) {
        List<Vec3> localVertices = die.getDefinition().vertices();
        List<Vec3> worldVertices = new ArrayList<>(localVertices.size());

        double scale = die.getScale() * die.getRadius();

        for (Vec3 local : localVertices) {
            Vec3 scaled = new Vec3(
                    local.x() * scale,
                    local.y() * scale,
                    local.z() * scale
            );

            Vec3 rotated = rotateVertex(
                    scaled,
                    die.getAngleX(),
                    die.getAngleY(),
                    die.getAngleZ()
            );

            worldVertices.add(new Vec3(
                    rotated.x() + die.getCenterX(),
                    rotated.y() + die.getCenterY(),
                    rotated.z() + die.getCenterZ()
            ));
        }

        return worldVertices;
    }

    public static int determineD2Value(DiceObject die) {
        Vec3 up = DicePhysics.rotateVertex(
                new Vec3(0, 0, 1),
                die.getAngleX(),
                die.getAngleY(),
                die.getAngleZ()
        ).normalize();

        return up.z() >= 0 ? 1 : 2;
    }

    private static Vec3 rotateVertex(Vec3 v, double axDeg, double ayDeg, double azDeg) {
        double ax = Math.toRadians(axDeg);
        double ay = Math.toRadians(ayDeg);
        double az = Math.toRadians(azDeg);

        double x1 = v.x();
        double y1 = v.y() * Math.cos(ax) - v.z() * Math.sin(ax);
        double z1 = v.y() * Math.sin(ax) + v.z() * Math.cos(ax);

        double x2 = x1 * Math.cos(ay) + z1 * Math.sin(ay);
        double y2 = y1;
        double z2 = -x1 * Math.sin(ay) + z1 * Math.cos(ay);

        double x3 = x2 * Math.cos(az) - y2 * Math.sin(az);
        double y3 = x2 * Math.sin(az) + y2 * Math.cos(az);
        double z3 = z2;

        return new Vec3(x3, y3, z3);
    }

    private static double applyDecay(double value, double frictionPerSecond, double dt) {
        return value * Math.exp(-frictionPerSecond * dt);
    }

    public static int determineTopFaceValue(DiceObject die) {
        DiceDefinition def = die.getDefinition();

        List<Vec3> faceNormals = def.faceNormals();
        int[] faceValues = def.faceValues();

        double bestDot = -Double.MAX_VALUE;
        int bestValue = faceValues[0];

        for (int i = 0; i < faceNormals.size(); i++) {
            Vec3 rotatedNormal = rotateVertex(
                    faceNormals.get(i),
                    die.getAngleX(),
                    die.getAngleY(),
                    die.getAngleZ()
            ).normalize();

            double dotUp = rotatedNormal.dot(new Vec3(0, 0, 1));

            if (dotUp > bestDot) {
                bestDot = dotUp;
                bestValue = faceValues[i];
            }
        }

        return bestValue;
    }
}