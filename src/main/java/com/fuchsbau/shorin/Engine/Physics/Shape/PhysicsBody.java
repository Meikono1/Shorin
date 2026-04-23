package com.fuchsbau.shorin.Engine.Physics.Shape;

import com.fuchsbau.shorin.Engine.Physics.Collision.WorldBoundingBox;
import com.fuchsbau.shorin.Engine.Physics.Material.Material;
import com.fuchsbau.shorin.Engine.Physics.Math.Matrix3;
import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.World.World;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.logging.Logger;

public class PhysicsBody {
    private static final Vec3 tmpVec = new Vec3();
    private static final Quaternion tmpQuat = new Quaternion(0, 0, 0, 1);
    private static final WorldBoundingBox updateAABB_shapeAABB = new WorldBoundingBox(null, null);
    private static final Matrix3 uiw_m1 = new Matrix3();
    private static final Matrix3 uiw_m2 = new Matrix3();
    private static final Vec3 applyForce_rotForce = new Vec3();
    private static final Vec3 applyLocalForce_worldForce = new Vec3();
    private static final Vec3 applyLocalForce_relativePointWorld = new Vec3();
    private static final Vec3 applyImpulse_velo = new Vec3();
    private static final Vec3 applyImpulse_rotVelo = new Vec3();
    private static final Vec3 updateMassProperties_halfExtents = new Vec3();


    private static int idCounter = 0;
    private final Logger logger = FileLogger.getLogger();

    public int id;
    public int index;
    public boolean allowSleep;
    public boolean wakeUpAfterNarrowphase;

    public double mass = 0;
    public double invMass = 0;
    public BodyType type;
    public SleepState sleepState;
    public float sleepSpeedLimit;
    public double sleepTimeLimit;
    public double timeLastSleepy;

    public int collisionFilterGroup;
    public int collisionFilterMask;
    public double boundingRadius;
    public boolean collisionResponse;
    public boolean fixedRotation = false;
    public boolean isTrigger = false;

    public ArrayList<CollisionShape> shapes;
    public ArrayList<Vec3> shapeOffsets;
    public ArrayList<Quaternion> shapeOrientations;

    public World world;
    public Material material;
    public WorldBoundingBox aabb;
    public boolean aabbNeedsUpdate;

    public Vec3 initPosition;
    public Vec3 position;
    public Vec3 previousPosition;
    public Vec3 interpolatedPosition;

    public Vec3 linearFactor;
    public Vec3 angularFactor;

    public Vec3 initVelocity;
    public Vec3 velocity;
    public Vec3 force;
    public Vec3 torque;

    public Vec3 initAngularVelocity;
    public Vec3 angularVelocity;
    public double angularDamping = 0.01;

    private Vec3 inertia;
    private Vec3 invInertia;
    private Matrix3 invInertiaWorld;
    private Vec3 invInertiaSolve;
    public Matrix3 invInertiaWorldSolve;
    public double invMassSolve;

    public Quaternion quaternion;
    public Quaternion initQuaternion;
    public Quaternion previousQuaternion;
    public Quaternion interpolatedQuaternion;

    public double linearDamping = 0.01;

    public Vec3 vLambda;
    public Vec3 wLambda;

    // TMP, muss mal gucken wie ich die interpretiere
    public String diceType;
    public String diceShape;
    public String diceMaterial;
    public boolean secretRoll;
    public int startAtIteration;

    //Extra
    public boolean dead = false;
    public int result = -1;
    public int asleepAtIteration = -1;

    public float[] stepQuaternions = new float[1001 * 4];
    public float[] stepPositions = new float[1001 * 3];

    public PhysicsBody(PhysicsBodyOptions bodyOptions) {
        this.id = idCounter++;
        this.index = -1;
        //this.world = null
        this.vLambda = new Vec3();
        this.collisionFilterGroup = bodyOptions.collisionFilterGroup;
        this.collisionFilterMask = bodyOptions.collisionFilterMask;
        this.collisionResponse = bodyOptions.collisionResponse;

        if (bodyOptions.position != null) {
            this.position = new Vec3(bodyOptions.position);
            this.previousPosition = new Vec3(bodyOptions.position);
            this.interpolatedPosition = new Vec3(bodyOptions.position);
            this.initPosition = new Vec3(bodyOptions.position);
        } else {
            this.position = new Vec3();
            this.previousPosition = new Vec3();
            this.interpolatedPosition = new Vec3();
            this.initPosition = new Vec3();
        }

        if (bodyOptions.velocity != null) {
            this.velocity = new Vec3(bodyOptions.velocity);
        } else {
            this.velocity = new Vec3();
        }

        this.initVelocity = new Vec3();
        this.force = new Vec3();
        this.mass = bodyOptions.mass;
        this.invMass = mass > 0 ? 1.0 / mass : 0;
        this.material = bodyOptions.material;

        this.linearDamping = bodyOptions.linearDamping;

        if (bodyOptions.type != null && bodyOptions.type.equals(BodyType.STATIC)) {
            this.type = bodyOptions.type;
        } else {
            this.type = mass <= 0.0 ? BodyType.STATIC : BodyType.DYNAMIC;
        }

        this.allowSleep = bodyOptions.allowSleep;
        this.sleepState = SleepState.ACTIVE;
        this.sleepSpeedLimit = bodyOptions.sleepSpeedLimit;
        this.sleepTimeLimit = bodyOptions.sleepTimeLimit;
        this.timeLastSleepy = 0;
        this.wakeUpAfterNarrowphase = false;

        this.torque = new Vec3();

        if (bodyOptions.quaternion != null) {
            this.quaternion = new Quaternion(bodyOptions.quaternion);
            this.initQuaternion = new Quaternion(bodyOptions.quaternion);
            this.previousQuaternion = new Quaternion(bodyOptions.quaternion);
            this.interpolatedQuaternion = new Quaternion(bodyOptions.quaternion);
        } else {
            this.quaternion = new Quaternion();
            this.initQuaternion = new Quaternion();
            this.previousQuaternion = new Quaternion();
            this.interpolatedQuaternion = new Quaternion();
        }

        if (bodyOptions.angularVelocity != null) {
            this.angularVelocity = new Vec3(bodyOptions.angularVelocity);
        } else {
            this.angularVelocity = new Vec3();
        }

        this.initAngularVelocity = new Vec3();

        this.shapes = new ArrayList<>();
        this.shapeOffsets = new ArrayList<>();
        this.shapeOrientations = new ArrayList<>();

        this.inertia = new Vec3();
        this.invInertia = new Vec3();
        this.invInertiaWorld = new Matrix3();
        this.invMassSolve = 0;
        this.invInertiaSolve = new Vec3();
        this.invInertiaWorldSolve = new Matrix3();

        this.fixedRotation = bodyOptions.fixedRotation;
        this.angularDamping = bodyOptions.angularDamping;

        if (bodyOptions.linearFactor != null) {
            this.linearFactor = new Vec3(bodyOptions.linearFactor);
        } else {
            this.linearFactor = new Vec3(1, 1, 1);
        }

        if (bodyOptions.angularFactor != null) {
            this.angularFactor = new Vec3(bodyOptions.angularFactor);
        } else {
            this.angularFactor = new Vec3(1, 1, 1);
        }

        this.aabb = new WorldBoundingBox(null, null);
        this.aabbNeedsUpdate = true;
        this.boundingRadius = 0;
        this.wLambda = new Vec3();
        this.isTrigger = bodyOptions.isTrigger;

        if (bodyOptions.shapes != null) {
            for (CollisionShape shape : bodyOptions.shapes) {
                addShape(shape, null, null);
            }
        }

        updateMassProperties();
    }

    public void wakeUp() {
        this.wakeUpAfterNarrowphase = false;
        this.sleepState = SleepState.ACTIVE;
    }

    public void sleep() {
        this.sleepState = SleepState.SLEEPING;
        this.velocity.set(0, 0, 0);
        this.angularVelocity.set(0, 0, 0);
        this.wakeUpAfterNarrowphase = false;
    }

    public void sleepTick(double time) {
        if (this.allowSleep) {
            SleepState sleepState = this.sleepState;
            double speedSquared = this.velocity.lengthSquared() + this.angularVelocity.lengthSquared();
            double speedLimitSquared = Math.sqrt(this.sleepSpeedLimit);

            if (sleepState.equals(SleepState.ACTIVE) && speedSquared < speedLimitSquared) {
                this.sleepState = SleepState.DROWSY;
                this.timeLastSleepy = time;
                //this.dispatchEvent(Body.sleepyEvent);
            } else if (sleepState.equals(SleepState.DROWSY) && speedSquared > speedLimitSquared) {
                this.wakeUp();
            } else if (sleepState.equals(SleepState.DROWSY) && time - this.timeLastSleepy > this.sleepTimeLimit) {
                this.sleep();
                //this.dispatchEvent(Body.sleepEvent);
            }
        }
    }

    public void updateSolveMassProperties() {
        if (this.sleepState.equals(SleepState.SLEEPING) || this.type.equals(BodyType.KINEMATIC)) {
            this.invMassSolve = 0;
            this.invInertiaSolve.setZero();
            this.invInertiaWorldSolve.setZero();
        } else {
            this.invMassSolve = this.invMass;
            this.invInertiaSolve.copy(this.invInertia);
            this.invInertiaWorldSolve.copy(this.invInertiaWorld);
        }
    }

    public Vec3 pointToLocalFrame(Vec3 worldPoint, Vec3 result) {
        worldPoint.sub(this.position, result);
        this.quaternion.conjugate(quaternion).vmult(result, result);
        return result;
    }

    public Vec3 vectorToLocalFrame(Vec3 worldPoint, Vec3 result) {
        this.quaternion.conjugate(quaternion).vmult(worldPoint, result);
        return result;
    }

    public Vec3 vectorToWorldFrame(Vec3 worldPoint, Vec3 result) {
        this.quaternion.vmult(worldPoint, result);
        return result;
    }

    public Vec3 pointToWorldFrame(Vec3 worldPoint, Vec3 result) {
        this.quaternion.vmult(worldPoint, result);
        result.add(this.position, result);
        return result;
    }

    public PhysicsBody addShape(CollisionShape shape, Vec3 _offset, Quaternion _orientation) {
        Vec3 offset = new Vec3();
        Quaternion orientation = new Quaternion();

        if (_offset != null) {
            offset.copy(_offset);
        }
        if (_orientation != null) {
            orientation.copy(_orientation);
        }

        this.shapes.add(shape);
        this.shapeOffsets.add(offset);
        this.shapeOrientations.add(orientation);
        this.updateMassProperties();
        this.updateBoundingRadius();

        this.aabbNeedsUpdate = true;

        shape.body = this;

        return this;
    }

    public PhysicsBody removeShape(CollisionShape shape) {
        int index = this.shapes.indexOf(shape);

        if (index == -1) {
            logger.warning("Shape does not belong to the body");
            return this;
        }

        this.shapes.remove(index);
        this.shapeOffsets.remove(index);
        this.shapeOrientations.remove(index);
        this.updateMassProperties();
        this.updateBoundingRadius();

        this.aabbNeedsUpdate = true;

        shape.body = null;

        return this;
    }

    public void updateBoundingRadius() {
        double radius = 0;

        for (int i = 0; i < shapes.size(); i++) {
            CollisionShape shape = shapes.get(i);
            shape.updateBoundingSphereRadius();
            double offset = shapeOffsets.get(i).length();
            double r = shape.boundingSphereRadius;
            if (offset + r > radius) {
                radius = offset + r;
            }
        }

        this.boundingRadius = radius;
    }

    public void updateAABB() {
        int N = shapes.size();

        for (int i = 0; i < N; i++) {
            CollisionShape shape = shapes.get(i);

            // Shape Weltposition berechnen
            quaternion.vmult(shapeOffsets.get(i), tmpVec);
            tmpVec.add(position, tmpVec);

            // Shape Weltrotation berechnen
            quaternion.mult(shapeOrientations.get(i), tmpQuat);

            // Shape AABB berechnen
            shape.calculateWorldAABB(tmpVec, tmpQuat, updateAABB_shapeAABB.lowerBound, updateAABB_shapeAABB.upperBound);

            if (i == 0) {
                aabb.copy(updateAABB_shapeAABB);
            } else {
                aabb.extend(updateAABB_shapeAABB);
            }
        }

        aabbNeedsUpdate = false;
    }

    public void updateInertiaWorld(boolean force) {
        Vec3 I = this.invInertia;
        if (I.x == I.y && I.y == I.z && !force) {
            // Trägheitsmoment ist isotrop – keine Transformation nötig
            return;
        }

        uiw_m1.setRotationFromQuaternion(this.quaternion);
        uiw_m1.transpose(uiw_m2);
        uiw_m1.scale(I, uiw_m1);
        uiw_m1.mmult(uiw_m2, this.invInertiaWorld);
    }

    public void applyForce(Vec3 force, Vec3 relativePoint) {
        if (type != BodyType.DYNAMIC) return;

        if (sleepState == SleepState.SLEEPING) {
            wakeUp();
        }

        // Rotationskraft berechnen: relativePoint × force
        relativePoint.cross(force, applyForce_rotForce);

        // Linearkraft addieren
        this.force.add(force, this.force);

        // Rotationskraft addieren
        this.torque.add(applyForce_rotForce, this.torque);
    }

    public void applyLocalForce(Vec3 localForce, Vec3 localPoint) {
        if (type != BodyType.DYNAMIC) return;

        // Kraft und Punkt in Weltkoordinaten transformieren
        vectorToWorldFrame(localForce, applyLocalForce_worldForce);
        vectorToWorldFrame(localPoint, applyLocalForce_relativePointWorld);

        applyForce(applyLocalForce_worldForce, applyLocalForce_relativePointWorld);
    }

    public void applyTorque(Vec3 torgue) {
        if (this.type != BodyType.DYNAMIC) {
            return;
        }

        if (this.sleepState == SleepState.SLEEPING) {
            this.wakeUp();
        }

        this.torque.add(torque, this.torque);
    }

    public void applyImpulse(Vec3 impulse, Vec3 relativePoint) {
        if (type != BodyType.DYNAMIC) return;

        if (sleepState == SleepState.SLEEPING) {
            wakeUp();
        }

        // Linearen Impuls berechnen: velo = impulse * invMass
        applyImpulse_velo.copy(impulse);
        applyImpulse_velo.scale(invMass, applyImpulse_velo);

        // Lineargeschwindigkeit addieren
        velocity.add(applyImpulse_velo, velocity);

        // Rotationsimpuls berechnen: r × impulse
        relativePoint.cross(impulse, applyImpulse_rotVelo);

        // invInertiaWorld * rotVelo
        invInertiaWorld.vmult(applyImpulse_rotVelo, applyImpulse_rotVelo);

        // Winkelgeschwindigkeit addieren
        angularVelocity.add(applyImpulse_rotVelo, angularVelocity);
    }

    public void updateMassProperties() {
        invMass = mass > 0 ? 1.0 / mass : 0;

        boolean fixed = fixedRotation;

        // AABB approximieren
        updateAABB();
        updateMassProperties_halfExtents.set(
                (aabb.upperBound.x - aabb.lowerBound.x) / 2,
                (aabb.upperBound.y - aabb.lowerBound.y) / 2,
                (aabb.upperBound.z - aabb.lowerBound.z) / 2
        );

        Box.calculateInertia(updateMassProperties_halfExtents, mass, inertia);

        invInertia.set(
                inertia.x > 0 && !fixed ? 1.0 / inertia.x : 0,
                inertia.y > 0 && !fixed ? 1.0 / inertia.y : 0,
                inertia.z > 0 && !fixed ? 1.0 / inertia.z : 0
        );

        updateInertiaWorld(true);
    }

    public Vec3 getVelocityAtWorldPoint(Vec3 worldPoint, Vec3 result) {
        Vec3 r = new Vec3();
        worldPoint.sub(this.position, r);
        this.angularVelocity.cross(r, result);
        this.velocity.add(result, result);
        return result;
    }

    public void integrate(double dt, boolean quatNormalize, boolean quatNormalizeFast) {
        previousPosition.copy(position);
        previousQuaternion.copy(quaternion);

        if (!(type == BodyType.DYNAMIC || type == BodyType.KINEMATIC)
                || sleepState == SleepState.SLEEPING) {
            return;
        }

        double iMdt = invMass * dt;
        velocity.x += force.x * iMdt * linearFactor.x;
        velocity.y += force.y * iMdt * linearFactor.y;
        velocity.z += force.z * iMdt * linearFactor.z;

        double[] e = invInertiaWorld.elements;
        double tx = torque.x * angularFactor.x;
        double ty = torque.y * angularFactor.y;
        double tz = torque.z * angularFactor.z;
        angularVelocity.x += dt * (e[0] * tx + e[1] * ty + e[2] * tz);
        angularVelocity.y += dt * (e[3] * tx + e[4] * ty + e[5] * tz);
        angularVelocity.z += dt * (e[6] * tx + e[7] * ty + e[8] * tz);

        // Leap Frog
        position.x += velocity.x * dt;
        position.y += velocity.y * dt;
        position.z += velocity.z * dt;

        quaternion.integrate(angularVelocity, dt, angularFactor, quaternion);

        if (quatNormalize) {
            if (quatNormalizeFast) {
                //quaternion.normalizeFast();
                quaternion.normalize();
            } else {
                quaternion.normalize();
            }
        }

        aabbNeedsUpdate = true;
        updateInertiaWorld(false);
    }

    public Vec3 getPosition() {
        return position;
    }

    public Vec3 getVelocity() {
        return velocity;
    }

    public Vec3 getAngularVelocity() {
        return angularVelocity;
    }
}