package com.fuchsbau.shorin.Engine.Physics.World;

import com.fuchsbau.shorin.Engine.Physics.Collision.*;
import com.fuchsbau.shorin.Engine.Physics.Constraints.Constraint;
import com.fuchsbau.shorin.Engine.Physics.Material.ContactMaterial;
import com.fuchsbau.shorin.Engine.Physics.Material.Material;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.BodyType;
import com.fuchsbau.shorin.Engine.Physics.Shape.CollisionShape;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Shape.SleepState;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.ContactEquation;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.Equation;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.FrictionEquation;
import com.fuchsbau.shorin.Engine.Physics.Solver.GaussSeidelSolver;
import com.fuchsbau.shorin.Engine.Physics.Util.TupleDictionary;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class World {

    private static final Logger logger = FileLogger.getLogger();
    // Statische Temp-Objekte in World.java – wiederverwendet um Heap-Allokation zu vermeiden

    private static final WorldBoundingBox tmpAABB1 = new WorldBoundingBox(null, null);
    private static final Vec3 step_tmp1 = new Vec3();
    private static final Ray tmpRay = new Ray();

    // Kollisionspaare für Broadphase
    private static final List<PhysicsBody> World_step_p1 = new ArrayList<>();
    private static final List<PhysicsBody> World_step_p2 = new ArrayList<>();

    // Pools für wiederverwendbare Objekte
    private static final List<ContactEquation> World_step_oldContacts = new ArrayList<>();
    private static final List<FrictionEquation> World_step_frictionEquationPool = new ArrayList<>();

    // Für emitContactEvents
    private static final List<Integer> additions = new ArrayList<>();
    private static final List<Integer> removals = new ArrayList<>();

    private ArrayCollisionMatrix collisionMatrix;
    private ArrayCollisionMatrix collisionMatrixPrevious;
    private OverlapKeeper bodyOverlapKeeper;
    private OverlapKeeper shapeOverlapKeeper;
    private TupleDictionary contactMaterialTable;
    public double lastCallTime = 0;

    public double dt = -1;
    public boolean allowSleep = false;
    public int stepnumber = 0;
    public double time = 0.0;
    public double default_dt = 1.0 / 60.0;
    public double accumulator = 0;
    public int nextId = 0;
    public boolean hasActiveBodies = false;
    public boolean doProfiling = false;
    public int quatNormalizeSkip = 0;
    public boolean quatNormalizeFast = false;

    public Vec3 gravity;
    public Vec3 frictionGravity;

    public List<PhysicsBody> bodies = new ArrayList<>();
    public List<ContactEquation> contacts = new ArrayList<>();
    public List<FrictionEquation> frictionEquations = new ArrayList<>();
    public List<ContactMaterial> contactmaterials = new ArrayList<>();
    public List<Object> constraints = new ArrayList<>();
    public List<Object> subsystems = new ArrayList<>();

    public GaussSeidelSolver solver;
    public Broadphase broadphase;
    public Narrowphase narrowphase;

    public Material defaultMaterial;
    public ContactMaterial defaultContactMaterial;

    public Map<Integer, PhysicsBody> idToBodyMap = new HashMap<>();

    // --- Debug / Profiling ---
    // Zum Deaktivieren: probe = null setzen ODER probe.enabled = false
    public PhysicsDebugProbe probe = null;

    public World() {
        this.gravity = new Vec3();
        this.solver = new GaussSeidelSolver();
        this.broadphase = new NaiveBroadphase();
        this.narrowphase = new Narrowphase(this);

        this.collisionMatrix = new ArrayCollisionMatrix();
        this.collisionMatrixPrevious = new ArrayCollisionMatrix();
        this.bodyOverlapKeeper = new OverlapKeeper();
        this.shapeOverlapKeeper = new OverlapKeeper();

        this.defaultMaterial = new Material("default");
        this.defaultContactMaterial = new ContactMaterial(
                defaultMaterial, defaultMaterial, 0.3, 0.0
        );

        this.contactMaterialTable = new TupleDictionary();

        this.broadphase.setWorld(this);

        logger.fine("World initialisiert");
    }

    public World(Vec3 gravity, boolean allowSleep, GaussSeidelSolver solver, Broadphase broadphase) {
        this();
        if (gravity != null) this.gravity.copy(gravity);
        if (solver != null) this.solver = solver;
        if (broadphase != null) this.broadphase = broadphase;
        this.allowSleep = allowSleep;
    }

    public ContactMaterial getContactMaterial(Material m1, Material m2) {
        return contactMaterialTable.get(m1.id, m2.id);
    }

    public void collisionMatrixTick() {
        ArrayCollisionMatrix temp = collisionMatrixPrevious;
        collisionMatrixPrevious = collisionMatrix;
        collisionMatrix = temp;
        collisionMatrix.reset();

        bodyOverlapKeeper.tick();
        shapeOverlapKeeper.tick();
    }

    public void addConstraint(Constraint c) {
        constraints.add(c);
    }

    public void removeConstraint(Constraint c) {
        constraints.remove(c);
    }

    public boolean raycastAll(Vec3 from, Vec3 to, RayOptions options, RaycastCallback callback) {
        options.mode = RayMode.ALL;
        options.from = from;
        options.to = to;
        options.callback = callback;
        return tmpRay.intersectWorld(this, options);
    }

    public boolean raycastAny(Vec3 from, Vec3 to, RayOptions options, RaycastResult result) {
        options.mode = RayMode.ANY;
        options.from = from;
        options.to = to;
        options.result = result;
        return tmpRay.intersectWorld(this, options);
    }

    public boolean raycastClosest(Vec3 from, Vec3 to, RayOptions options, RaycastResult result) {
        options.mode = RayMode.CLOSEST;
        options.from = from;
        options.to = to;
        options.result = result;
        return tmpRay.intersectWorld(this, options);
    }

    public void addBody(PhysicsBody body) {
        if (bodies.contains(body)) return;

        body.index = bodies.size();
        bodies.add(body);
        body.world = this;

        body.initPosition.copy(body.position);
        body.initVelocity.copy(body.velocity);
        body.initAngularVelocity.copy(body.angularVelocity);
        body.initQuaternion.copy(body.quaternion);
        body.timeLastSleepy = this.time;

        collisionMatrix.setNumObjects(bodies.size());
        idToBodyMap.put(body.id, body);

        logger.fine("addBody – id=" + body.id + " total=" + bodies.size());
    }

    public void removeBody(PhysicsBody body) {
        body.world = null;
        int idx = bodies.indexOf(body);

        if (idx != -1) {
            bodies.remove(idx);

            // Indizes neu berechnen
            for (int i = 0; i < bodies.size(); i++) {
                bodies.get(i).index = i;
            }

            collisionMatrix.setNumObjects(bodies.size());
            idToBodyMap.remove(body.id);

            logger.fine("removeBody – id=" + body.id + " total=" + bodies.size());
        }
    }

    public PhysicsBody getBodyById(int id) {
        return idToBodyMap.get(id);
    }

    public CollisionShape getShapeById(int id) {
        for (PhysicsBody body : bodies) {
            for (CollisionShape shape : body.shapes) {
                if (shape.id == id) {
                    return shape;
                }
            }
        }
        return null;
    }

    public void addContactMaterial(ContactMaterial cmat) {
        contactmaterials.add(cmat);
        contactMaterialTable.set(cmat.materials[0].id, cmat.materials[1].id, cmat);
    }

    public void removeContactMaterial(ContactMaterial cmat) {
        int idx = contactmaterials.indexOf(cmat);
        if (idx == -1) return;

        contactmaterials.remove(idx);
        contactMaterialTable.delete(cmat.materials[0].id, cmat.materials[1].id);
    }

    public void fixedStep() {
        fixedStep(1.0 / 60.0, 10);
    }

    public void fixedStep(double dt, int maxSubSteps) {
        double time = System.nanoTime() / 1_000_000_000.0;

        if (lastCallTime == 0) {
            step(dt, null, maxSubSteps);
        } else {
            double timeSinceLastCalled = time - lastCallTime;
            step(dt, timeSinceLastCalled, maxSubSteps);
        }

        lastCallTime = time;
    }

    public void step(double dt) {
        step(dt, null, 10);
    }

    public void step(double dt, double timeSinceLastCalled) {
        step(dt, timeSinceLastCalled, 10);
    }

    public void step(double dt, Double timeSinceLastCalled, int maxSubSteps) {
        if (timeSinceLastCalled == null) {
            // Einfaches Fixed-Stepping ohne Interpolation
            internalStep(dt);
            time += dt;
        } else {
            accumulator += timeSinceLastCalled;

            long t0 = System.nanoTime();
            int substeps = 0;

            while (accumulator >= dt && substeps < maxSubSteps) {
                internalStep(dt);
                accumulator -= dt;
                substeps++;

                // Bail out wenn zu langsam
                /*if ((System.nanoTime() - t0) / 1_000_000.0 > dt * 1000) {
                    break;
                }*/
            }

            accumulator = accumulator % dt;

            // Interpolation
            double t = accumulator / dt;
            for (PhysicsBody b : bodies) {
                b.previousPosition.lerp(b.position, t, b.interpolatedPosition);
                b.previousQuaternion.slerp(b.quaternion, t, b.interpolatedQuaternion);
                b.previousQuaternion.normalize();
            }

            time += timeSinceLastCalled;
        }
    }

    public void internalStep(double dt) {
        this.dt = dt;
        int N = bodies.size();

        double gx = gravity.x;
        double gy = gravity.y;
        double gz = gravity.z;

        // Schwerkraft auf alle dynamischen Bodies anwenden
        for (PhysicsBody bi : bodies) {
            if (bi.type == BodyType.DYNAMIC) {
                bi.force.x += bi.mass * gx;
                bi.force.y += bi.mass * gy;
                bi.force.z += bi.mass * gz;
            }
        }

        // [PROBE] vor Gravity
        if (probe != null) probe.snapshot(PhysicsDebugProbe.PHASE_START, bodies, gz);

        // Schwerkraft auf alle dynamischen Bodies anwenden
        for (PhysicsBody bi : bodies) {
            if (bi.type == BodyType.DYNAMIC) {
                bi.force.x += bi.mass * gx;
                bi.force.y += bi.mass * gy;
                bi.force.z += bi.mass * gz;
            }
        }

        // [PROBE] nach Gravity
        if (probe != null) probe.snapshot(PhysicsDebugProbe.PHASE_AFTER_GRAV, bodies, gz);

        // Subsysteme updaten
        for (Object subsystem : subsystems) {
            // subsystem.update();
        }

        // Broadphase – Kollisionspaare finden
        World_step_p1.clear();
        World_step_p2.clear();
        broadphase.collisionPairs(this, World_step_p1, World_step_p2);

        // [PROBE] Broadphase-Ergebnis
        if (probe != null) probe.recordBroadphasePairs(World_step_p1.size());

        // Constraints mit collideConnected=false aus Paaren entfernen
        for (int i = constraints.size() - 1; i >= 0; i--) {
            Constraint c = (Constraint) constraints.get(i);
            if (!c.collideConnected) {
                for (int j = World_step_p1.size() - 1; j >= 0; j--) {
                    if ((c.bodyA == World_step_p1.get(j) && c.bodyB == World_step_p2.get(j)) ||
                            (c.bodyB == World_step_p1.get(j) && c.bodyA == World_step_p2.get(j))) {
                        World_step_p1.remove(j);
                        World_step_p2.remove(j);
                    }
                }
            }
        }

        collisionMatrixTick();

        // Alte Kontakte in Pool verschieben
        World_step_oldContacts.addAll(contacts);
        contacts.clear();

        World_step_frictionEquationPool.addAll(frictionEquations);
        frictionEquations.clear();

        // Narrowphase – Kontakte generieren
        narrowphase.getContacts(World_step_p1, World_step_p2, this, contacts,
                World_step_oldContacts, frictionEquations, World_step_frictionEquationPool);

        // Friction Equations zum Solver hinzufügen
        for (FrictionEquation fe : frictionEquations) {
            solver.addEquation(fe);
        }

        // Kontakte verarbeiten
        for (ContactEquation c : contacts) {
            PhysicsBody bi = c.bi;
            PhysicsBody bj = c.bj;

            // Contact Material bestimmen
            ContactMaterial cm;
            if (bi.material != null && bj.material != null) {
                cm = getContactMaterial(bi.material, bj.material);
                if (cm == null) cm = defaultContactMaterial;
            } else {
                cm = defaultContactMaterial;
            }

            // Material-Restitution anwenden
            if (bi.material != null && bj.material != null) {
                if (bi.material.restitution >= 0 && bj.material.restitution >= 0) {
                    c.restitution = bi.material.restitution * bj.material.restitution;
                }
            }

            solver.addEquation(c);

            // Sleep-Wakeup-Logik
            if (bi.allowSleep && bi.type == BodyType.DYNAMIC
                    && bi.sleepState == SleepState.SLEEPING
                    && bj.sleepState == SleepState.ACTIVE
                    && bj.type != BodyType.STATIC) {
                double speedSquaredB = bj.velocity.lengthSquared() + bj.angularVelocity.lengthSquared();
                if (speedSquaredB >= bj.sleepSpeedLimit * bj.sleepSpeedLimit * 2) {
                    bi.wakeUpAfterNarrowphase = true;
                }
            }

            if (bj.allowSleep && bj.type == BodyType.DYNAMIC
                    && bj.sleepState == SleepState.SLEEPING
                    && bi.sleepState == SleepState.ACTIVE
                    && bi.type != BodyType.STATIC) {
                double speedSquaredA = bi.velocity.lengthSquared() + bi.angularVelocity.lengthSquared();
                if (speedSquaredA >= bi.sleepSpeedLimit * bi.sleepSpeedLimit * 2) {
                    bj.wakeUpAfterNarrowphase = true;
                }
            }

            collisionMatrix.set(bi, bj, true);
            bodyOverlapKeeper.set(bi.id, bj.id);
            shapeOverlapKeeper.set(c.si.id, c.sj.id);
        }

        // Bodies aufwecken
        for (PhysicsBody bi : bodies) {
            if (bi.wakeUpAfterNarrowphase) {
                bi.wakeUp();
                bi.wakeUpAfterNarrowphase = false;
            }
        }

        // User-Constraints hinzufügen
        for (Object obj : constraints) {
            Constraint c = (Constraint) obj;
            c.update();
            for (Equation eq : c.equations) {
                solver.addEquation(eq);
            }
        }


        // [PROBE] Kontaktzahlen vor Solver
        if (probe != null) probe.recordContactCount(contacts.size(), frictionEquations.size());

        // Solver
        solver.solve(dt, bodies);

        // [PROBE] nach Solver (Velocities sind jetzt korrigiert)
        if (probe != null) probe.snapshot(PhysicsDebugProbe.PHASE_AFTER_SOLVE, bodies, gz);

        solver.removeAllEquations();

        // Dämpfung anwenden
        for (PhysicsBody bi : bodies) {
            if (bi.type == BodyType.DYNAMIC) {
                double ld = Math.pow(1.0 - bi.linearDamping, dt);
                bi.velocity.scale(ld, bi.velocity);
                double ad = Math.pow(1.0 - bi.angularDamping, dt);
                bi.angularVelocity.scale(ad, bi.angularVelocity);
            }
        }

        // [PROBE] nach Damping
        if (probe != null) probe.snapshot(PhysicsDebugProbe.PHASE_AFTER_DAMP, bodies, gz);

        // Integration – Leap Frog
        boolean quatNormalize = stepnumber % (quatNormalizeSkip + 1) == 0;
        boolean quatNormalizeFast = this.quatNormalizeFast;

        for (PhysicsBody body : bodies) {
            body.integrate(dt, quatNormalize, quatNormalizeFast);
        }

        // [PROBE] nach Integration + Step abschließen
        if (probe != null) {
            probe.snapshot(PhysicsDebugProbe.PHASE_AFTER_INTEG, bodies, gz);
            probe.endStep();
        }

        clearForces();
        broadphase.dirty = true;
        stepnumber++;

        // Sleep Update
        boolean hasActiveBodies = true;
        if (allowSleep) {
            hasActiveBodies = false;
            for (PhysicsBody bi : bodies) {
                bi.sleepTick(time);
                if (bi.sleepState != SleepState.SLEEPING) {
                    hasActiveBodies = true;
                }
            }
        }
        this.hasActiveBodies = hasActiveBodies;
    }

    protected void onBeginContact(PhysicsBody bodyA, PhysicsBody bodyB) {
    }

    protected void onEndContact(PhysicsBody bodyA, PhysicsBody bodyB) {
    }

    //TODO erweitern ?
    public void emitContactEvents() {
        // Body-Overlap Änderungen
        bodyOverlapKeeper.getDiff(additions, removals);

        for (int i = 0; i < additions.size() - 1; i += 2) {
            PhysicsBody bodyA = getBodyById(additions.get(i));
            PhysicsBody bodyB = getBodyById(additions.get(i + 1));
            onBeginContact(bodyA, bodyB);
        }

        for (int i = 0; i < removals.size() - 1; i += 2) {
            PhysicsBody bodyA = getBodyById(removals.get(i));
            PhysicsBody bodyB = getBodyById(removals.get(i + 1));
            onEndContact(bodyA, bodyB);
        }

        additions.clear();
        removals.clear();
    }

    public void clearForces() {
        for (PhysicsBody b : bodies) {
            b.force.set(0, 0, 0);
            b.torque.set(0, 0, 0);
        }
    }

    @FunctionalInterface
    public interface RaycastCallback {
        void accept(RaycastResult result);
    }
}