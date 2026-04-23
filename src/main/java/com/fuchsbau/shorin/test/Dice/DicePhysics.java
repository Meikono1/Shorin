package com.fuchsbau.shorin.test.Dice;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Collision.NaiveBroadphase;
import com.fuchsbau.shorin.Engine.Physics.Constraints.PointToPointConstraint;
import com.fuchsbau.shorin.Engine.Physics.Material.*;
import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.*;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape;
import com.fuchsbau.shorin.Engine.Physics.World.World;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.*;
import java.util.logging.Logger;

public class DicePhysics {
    private static final Logger logger = FileLogger.getLogger();

    private final List<PhysicsBody> barriers = new ArrayList<>();
    private static final double barriersScale = 0.97;

    private PhysicsBody jointBody;
    private World world;
    private Material dice_body_material;
    private Material desk_body_material;
    private Material barrier_body_material;

    public final Map<Integer, PhysicsBody> diceList = new HashMap<>();
    private final Map<String, CollisionShape> shapeList = new HashMap<>();

    private String animstate;
    private boolean muteSoundSecretRolls;

    private PointToPointConstraint mouseConstraint;
    private String lastSoundType = "";
    private int lastSoundStep = 0;
    private int lastSound = 0;
    private int soundDelay = 2;
    private int iterationsNeeded = 0;
    private int iteration = 0;
    private CollideData[] detectedCollides;

    private int minIterations;
    private int nbIterationsBetweenRolls;
    private double framerate;
    private boolean canBeFlipped;

    public void init(double height, double width, Margin margin, boolean muteSoundSecretRolls) {
        this.world = new World();
        this.dice_body_material = new Material();
        this.desk_body_material = new Material();
        this.barrier_body_material = new Material();

        this.soundDelay = 2;
        this.animstate = "throw";
        this.muteSoundSecretRolls = muteSoundSecretRolls;
        this.diceList.clear();

        world.gravity.set(0, 0, -9.8 * 800);
        world.broadphase = new NaiveBroadphase();
        world.solver.iterations = 24;
        world.allowSleep = true;

        this.muteSoundSecretRolls = false;//data.muteSoundSecretRolls;

        addContactMaterials();
        addDesk();
        addBarriers(height, width, margin);
        addJointBody();
        reset();

        logger.info("DicePhysics initialisiert – h=" + height + " w=" + width);
    }

    private void addContactMaterials() {
        // Würfel vs Boden
        //world.addContactMaterial(new ContactMaterial(desk_body_material, dice_body_material, 0.01, 0.5));
        world.addContactMaterial(new ContactMaterial(desk_body_material, dice_body_material, 0.35, 0.1));

        // Würfel vs Wand
        //world.addContactMaterial(new ContactMaterial(barrier_body_material, dice_body_material, 0.0, 0.95));
        world.addContactMaterial(new ContactMaterial(barrier_body_material, dice_body_material, 0.20, 0.20));

        // Würfel vs Würfel
        //world.addContactMaterial(new ContactMaterial(dice_body_material, dice_body_material, 0.01, 0.7));
        world.addContactMaterial(new ContactMaterial(dice_body_material, dice_body_material, 0.15, 0.15));

        logger.fine("addContactMaterials – 3 ContactMaterials registriert");
    }

    private void addDesk() {
        PhysicsBodyOptions physicsBodyOptions = new PhysicsBodyOptions();
        physicsBodyOptions.type = BodyType.STATIC;
        physicsBodyOptions.shapes.add(new Plane());
        physicsBodyOptions.material = desk_body_material;

        PhysicsBody desk = new PhysicsBody(physicsBodyOptions);
        desk.allowSleep = false;
        world.addBody(desk);

        logger.fine("addDesk – Boden hinzugefügt");
    }

    private void addBarriers(double height, double width, Margin margin) {
        barriers.clear();

        double[][] config = {
                // axis,                         angle,           position
                {1, 0, 0, Math.PI / 2, 0, (height - margin.top * 2) * barriersScale, 0},
                {1, 0, 0, -Math.PI / 2, 0, (-height + margin.bottom * 2) * barriersScale, 0},
                {0, 1, 0, -Math.PI / 2, (width - margin.right * 2) * barriersScale, 0, 0},
                {0, 1, 0, Math.PI / 2, (-width + margin.left * 2) * barriersScale, 0, 0}
        };

        for (double[] c : config) {
            Vec3 axis = new Vec3(c[0], c[1], c[2]);
            double angle = c[3];
            Vec3 position = new Vec3(c[4], c[5], c[6]);

            PhysicsBodyOptions physicsBodyOptions = new PhysicsBodyOptions();
            physicsBodyOptions.type = BodyType.STATIC;
            physicsBodyOptions.shapes.add(new Plane());
            physicsBodyOptions.material = desk_body_material;

            PhysicsBody barrier = new PhysicsBody(physicsBodyOptions);
            barrier.allowSleep = false;
            barrier.quaternion.setFromAxisAngle(axis, angle);
            barrier.position.copy(position);

            world.addBody(barrier);
            barriers.add(barrier);

            logger.fine("addBarriers – Wand hinzugefügt pos=" + position);
        }
    }

    public void updateBarriers(double height, double width, Margin margin) {
        barriers.get(0).position.set(0, (height - margin.top * 2) * barriersScale, 0);
        barriers.get(1).position.set(0, (-height + margin.bottom * 2) * barriersScale, 0);
        barriers.get(2).position.set((width - margin.right * 2) * barriersScale, 0, 0);
        barriers.get(3).position.set((-width + margin.left * 2) * barriersScale, 0, 0);

        logger.fine("updateBarriers – Wände aktualisiert");
    }

    private void addJointBody() {
        jointBody = new PhysicsBody(new PhysicsBodyOptions());
        jointBody.mass = 0;
        jointBody.addShape(new Sphere(0.1), null, null);
        jointBody.collisionFilterGroup = 0;
        jointBody.collisionFilterMask = 0;

        world.addBody(jointBody);

        logger.fine("addJointBody – Joint Body hinzugefügt");
    }

    public void createDice(int id, String shape, String material, VectorData vectordata,
                           double mass, int startAtIteration, DiceOptions options) {

        CollisionShape collisionShape = shapeList.get(shape);

        PhysicsBodyOptions physicsBodyOptions = new PhysicsBodyOptions();
        physicsBodyOptions.type = BodyType.DYNAMIC;
        physicsBodyOptions.shapes.add(collisionShape);
        physicsBodyOptions.material = dice_body_material;
        physicsBodyOptions.mass = mass;
        physicsBodyOptions.allowSleep =true;
        physicsBodyOptions.sleepSpeedLimit = 75;
        physicsBodyOptions.sleepTimeLimit = 0.9;

        PhysicsBody body = new PhysicsBody(physicsBodyOptions);

        body.position.set(vectordata.pos.x, vectordata.pos.y, vectordata.pos.z);
        body.quaternion.setFromAxisAngle(
                new Vec3(vectordata.axis.x, vectordata.axis.y, vectordata.axis.z),
                vectordata.axis.a * Math.PI * 2
        );
        body.angularVelocity.set(vectordata.angle.x, vectordata.angle.y, vectordata.angle.z);
        body.velocity.set(vectordata.velocity.x, vectordata.velocity.y, vectordata.velocity.z);
        //body.linearDamping = 0.1;
        //body.angularDamping = 0.1;
        body.linearDamping = 0.22;
        body.angularDamping = 0.28;

        // Metadaten für Kollisions-Events
        body.diceType = vectordata.type;
        body.diceShape = shape;
        body.diceMaterial = material;
        body.secretRoll = options != null && options.secret;
        body.startAtIteration = startAtIteration;

        diceList.put(id, body);

        logger.fine("createDice – id=" + id + " shape=" + shape + " mass=" + mass);
    }

    public void addDice(int id) {
        PhysicsBody dice = diceList.get(id);
        world.addBody(dice);
        logger.fine("addDice – id=" + id);
    }

    public void removeDice(List<Integer> ids) {
        for (int id : ids) {
            PhysicsBody dice = diceList.get(id);
            world.removeBody(dice);
            diceList.remove(id);
            logger.fine("removeDice – id=" + id);
        }
    }

    private void prepareForSimulation() {
        for (PhysicsBody body : diceList.values()) {
            body.stepPositions = new float[1001 * 3];
            body.stepQuaternions = new float[1001 * 4];

            // Startzustand direkt in Frame 0 schreiben
            body.stepPositions[0] = (float) body.position.x;
            body.stepPositions[1] = (float) body.position.y;
            body.stepPositions[2] = (float) body.position.z;

            body.stepQuaternions[0] = (float) body.quaternion.x;
            body.stepQuaternions[1] = (float) body.quaternion.y;
            body.stepQuaternions[2] = (float) body.quaternion.z;
            body.stepQuaternions[3] = (float) body.quaternion.w;
        }
    }

    private void clearPlaybackBuffers() {
        for (PhysicsBody body : diceList.values()) {
            body.stepPositions = null;
            body.stepQuaternions = null;
        }
    }


    private void cleanAfterThrow() {
        for (Map.Entry<Integer, PhysicsBody> entry : diceList.entrySet()) {
            PhysicsBody body = entry.getValue();
            body.stepPositions = new float[1001 * 3];
            body.stepQuaternions = new float[1001 * 4];
        }
        logger.fine("cleanAfterThrow – Arrays zurückgesetzt");
    }

    private void eventCollide(PhysicsBody body, PhysicsBody target) {
        if (body == null) return;

        int now = world.stepnumber;
        String currentSoundType = body.mass > 0 ? "dice" : "table";

        if (shouldSkipSound(now, currentSoundType)) return;

        if (body.mass > 0) {
            // Würfel vs Würfel
            handleDiceCollision(body, target);
        } else {
            // Würfel vs Tisch
            handleTableCollision(body, target);
        }

        updateLastSound(now);
    }

    private boolean shouldSkipSound(int now, String currentSoundType) {
        boolean soundPlayedThisStep = lastSoundStep == now;
        boolean notEnoughDelay = lastSound > now;

        if (soundPlayedThisStep || notEnoughDelay) {
            boolean sameSoundType = currentSoundType.equals("dice") && lastSoundType.equals("dice");
            return !(!currentSoundType.equals("dice")) || sameSoundType;
        }
        return false;
    }

    private void handleDiceCollision(PhysicsBody body, PhysicsBody target) {
        double speed = body.velocity.length();
        if (speed < 250) return;

        double strength = calculateStrength(speed, 550, 0.2);
        boolean shouldMute = muteSoundSecretRolls && (body.secretRoll || target.secretRoll);
        double finalStrength = shouldMute ? 0 : strength;

        if (animstate.equals("simulate")) {
            detectedCollides[iteration] = new CollideData("dice", body.diceType, body.diceMaterial, finalStrength);
        } else {
            logger.fine("collide – source=dice type=" + body.diceType + " strength=" + finalStrength);
        }

        lastSoundType = "dice";
    }

    private void handleTableCollision(PhysicsBody body, PhysicsBody target) {
        double speed = target.velocity.length();
        if (speed < 100) return;

        double strength = calculateStrength(speed, 500, 0.2);
        boolean shouldMute = muteSoundSecretRolls && (body.secretRoll || target.secretRoll);
        double finalStrength = shouldMute ? 0 : strength;

        if (animstate.equals("simulate")) {
            detectedCollides[iteration] = new CollideData("table", null, null, finalStrength);
        } else {
            logger.fine("collide – source=table strength=" + finalStrength);
        }

        lastSoundType = "table";
    }

    private double calculateStrength(double speed, double maxSpeed, double minStrength) {
        return Math.max(Math.min(speed / maxSpeed, 1.0), minStrength);
    }

    private void updateLastSound(int now) {
        lastSoundStep = now;
        lastSound = now + soundDelay;
    }

    public void addConstraint(int id, Vec3 pos) {
        PhysicsBody dice = diceList.get(id);

        Vec3 v1 = pos.sub(dice.position);

        // Anti-Quaternion um in lokale Körperkoordinaten zu transformieren
        Quaternion antiRot = dice.quaternion.inverse();
        Vec3 pivot = antiRot.vmult(v1);

        // Joint Body zur Klickposition bewegen
        jointBody.position.set(pos.x, pos.y, pos.z + 150);

        // Constraint erstellen und zur Welt hinzufügen
        mouseConstraint = new PointToPointConstraint(dice, pivot, jointBody, new Vec3(0, 0, 0));
        world.addConstraint(mouseConstraint);

        logger.fine("addConstraint – id=" + id + " pos=" + pos);
    }

    public void updateConstraint(Vec3 pos) {
        if (mouseConstraint != null) {
            jointBody.position.set(pos.x, pos.y, pos.z + 150);
            mouseConstraint.update();
        }
    }

    public void removeConstraint() {
        if (mouseConstraint != null) {
            world.removeConstraint(mouseConstraint);
            mouseConstraint = null;
        }
    }

    public void createShape(String type, double radius) {
        DiceShape.DiceShapeData data = DiceShape.get(type);

        switch (data.type) {
            case  CONVEXPOLYHEDRON ->
                    shapeList.put(type, loadGeom(data.vertices, data.faces, radius, data.skipLastFaceIndex));
            case CYLINDER ->
                    shapeList.put(type, new Cylinder(radius * data.radiusTop, radius * data.radiusBottom, radius * data.height, data.numSegments));
            default -> throw new IllegalArgumentException("Unbekannter Shape-Typ: " + data.type);
        }

        logger.fine("createShape – type=" + type + " radius=" + radius);
    }

    private ConvexPolyhedron loadShape(List<Vec3> vertices, List<int[]> faces, double radius, boolean skipLastFaceIndex) {
        List<Vec3> cv = new ArrayList<>(vertices.size());
        List<int[]> cf = new ArrayList<>(faces.size());

        for (Vec3 v : vertices) {
            cv.add(new Vec3(v.x * radius, v.y * radius, v.z * radius));
        }

        for (int[] face : faces) {
            if (skipLastFaceIndex) {
                cf.add(Arrays.copyOf(face, face.length - 1));
            } else {
                cf.add(face);
            }
        }

        return new ConvexPolyhedron(cv, cf, null, null, null);
    }

    private ConvexPolyhedron loadGeom(double[][] vertices, int[][] faces, double radius, boolean skipLastFaceIndex) {
        List<Vec3>  vectors = new ArrayList<>(vertices.length);
        List<int[]> faceList = new ArrayList<>(faces.length);

        /*for (double[] v : vertices) {
            Vec3 vec = new Vec3(v[0], v[1], v[2]);
            vec.normalize();
            vectors.add(vec);
        }*/
        for (double[] v : vertices) {
            Vec3 vec = new Vec3(v[0], v[1], v[2]);
            vec.normalize();
            vec.scale(radius, vec);
            vectors.add(vec);
        }

        for (int[] face : faces) {
            faceList.add(skipLastFaceIndex ? Arrays.copyOf(face, face.length - 1) : face);
        }

        return new ConvexPolyhedron(vectors, faceList, null, null, null);
    }

    public Integer getDiceValue(int id) {
        PhysicsBody dice = diceList.get(id);

        if (dice == null) return null;
        if (dice.result < 0) return dice.result;

        // D4 schaut nach unten, alle anderen nach oben
        Vec3 vector = new Vec3(0, 0, dice.diceShape.equals("d4") ? -1 : 1);

        ConvexPolyhedron shape = (ConvexPolyhedron) dice.shapes.get(0);
        int[] faceValues = DiceShape.get(dice.diceShape).faceValues;

        int closestFace = 0;
        double closestAngle = Math.PI * 2;

        for (int i = 0; i < shape.faceNormals.size(); i++) {
            if (faceValues[i] == 0) continue;

            // Face-Normal in Weltkoordinaten rotieren
            Vec3 faceCannon = new Vec3();
            faceCannon.copy(shape.faceNormals.get(i));
            dice.quaternion.vmult(faceCannon, faceCannon);

            double angle = faceCannon.angleTo(vector);
            if (angle < closestAngle) {
                closestAngle = angle;
                closestFace = i;
            }
        }

        int dieValue = faceValues[closestFace];
        dice.result = dieValue;

        logger.info("getDiceValue – id=" + id + " value=" + dieValue);
        return dieValue;
    }

    private void reset() {
        lastSoundType = "";
        lastSoundStep = 0;
        lastSound = 0;
        detectedCollides = new CollideData[1000];
        iterationsNeeded = 0;
        animstate = "simulate";
        iteration = 0;

        logger.fine("reset – Simulation zurückgesetzt");
    }

    public SimulationResult simulateThrow(int minIterations, int nbIterationsBetweenRolls,
                                          double framerate, boolean canBeFlipped) {
        long startTime = System.nanoTime();

        reset();

        this.minIterations = minIterations;
        this.nbIterationsBetweenRolls = nbIterationsBetweenRolls;
        this.framerate = framerate;
        this.canBeFlipped = canBeFlipped;

        prepareForSimulation();

        runPhysicsSimulation();

        List<Integer> ids = new ArrayList<>();
        List<float[]> quaternions = new ArrayList<>();
        List<float[]> positions = new ArrayList<>();
        List<Boolean> deads = new ArrayList<>();

        for (Map.Entry<Integer, PhysicsBody> entry : diceList.entrySet()) {
            PhysicsBody dice = entry.getValue();
            ids.add(entry.getKey());
            quaternions.add(dice.stepQuaternions);
            positions.add(dice.stepPositions);
            deads.add(dice.dead);
        }

        animstate = "throw";

        long durationMs = (System.nanoTime() - startTime) / 1_000_000;
        logger.info("simulateThrow – " + durationMs + "ms iterations=" + iterationsNeeded
                + " dice=" + diceList.size());

        //cleanAfterThrow();

        return new SimulationResult(ids, quaternions, positions, detectedCollides, deads, iterationsNeeded);
    }

    private void runPhysicsSimulation() {
        while (!throwFinished()) {
            iteration++;

            // Würfel zur Welt hinzufügen wenn ihre Startiteration erreicht ist
            if (iteration % nbIterationsBetweenRolls == 0) {
                for (PhysicsBody dice : diceList.values()) {
                    if (dice.startAtIteration == iteration) {
                        world.addBody(dice);
                    }
                }
            }

            world.step(framerate);

            // Position und Quaternion jedes Bodies für diesen Step speichern
            for (PhysicsBody body : world.bodies) {
                if (body.stepPositions != null) {
                    body.stepQuaternions[iteration * 4] = (float) body.quaternion.x;
                    body.stepQuaternions[iteration * 4 + 1] = (float) body.quaternion.y;
                    body.stepQuaternions[iteration * 4 + 2] = (float) body.quaternion.z;
                    body.stepQuaternions[iteration * 4 + 3] = (float) body.quaternion.w;

                    body.stepPositions[iteration * 3] = (float) body.position.x;
                    body.stepPositions[iteration * 3 + 1] = (float) body.position.y;
                    body.stepPositions[iteration * 3 + 2] = (float) body.position.z;
                }
            }
        }
    }

    private boolean throwFinished() {
        if (iteration <= minIterations) return false;

        boolean stopped = true;

        for (Map.Entry<Integer, PhysicsBody> entry : diceList.entrySet()) {
            PhysicsBody dice = entry.getValue();
            if (dice.sleepState.ordinal() < 2) {
                stopped = false;
                break;
            } else {
                dice.asleepAtIteration = iteration;
            }
        }

        if (iteration >= 1000) stopped = true;

        if (stopped) {
            iterationsNeeded = iteration;
            for (Map.Entry<Integer, PhysicsBody> entry : diceList.entrySet()) {
                int id = entry.getKey();
                PhysicsBody dice = entry.getValue();

                dice.result = getDiceValue(id);

                if (!canBeFlipped) {
                    dice.mass = 0;
                    dice.dead = dice.asleepAtIteration > 0;
                    dice.updateMassProperties();
                }
            }
        }

        return stopped;
    }

    public PlayStepResult playStep(double timeDiff) {
        if (animstate.equals("simulate")) return null;

        List<Integer> ids = new ArrayList<>();
        float[] quaternions = new float[diceList.size() * 4];
        float[] positions = new float[diceList.size() * 3];
        boolean worldAsleep = true;

        // Prüfen ob alle Bodies schlafen
        for (PhysicsBody body : world.bodies) {
            if (body.allowSleep && body.sleepState.ordinal() < 2) {
                worldAsleep = false;
                break;
            }
        }

        if (!worldAsleep) {
            world.step(framerate, timeDiff);

            int idx = 0;
            for (Map.Entry<Integer, PhysicsBody> entry : diceList.entrySet()) {
                PhysicsBody dice = entry.getValue();
                if (dice.dead) continue;

                ids.add(entry.getKey());
                int qi = idx * 4;
                int pi = idx * 3;

                quaternions[qi] = (float) dice.quaternion.x;
                quaternions[qi + 1] = (float) dice.quaternion.y;
                quaternions[qi + 2] = (float) dice.quaternion.z;
                quaternions[qi + 3] = (float) dice.quaternion.w;

                positions[pi] = (float) dice.position.x;
                positions[pi + 1] = (float) dice.position.y;
                positions[pi + 2] = (float) dice.position.z;

                idx++;
            }
        }

        return new PlayStepResult(ids, quaternions, positions, worldAsleep);
    }

    public void getWorldInfo() {
        logger.info("World iteration: " + world.stepnumber);
        logger.info("World hat " + world.bodies.size() + " Bodies:");

        for (int i = 0; i < world.bodies.size(); i++) {
            logger.info("body " + i + ": " + world.bodies.get(i));
        }

        logger.info("World hat " + world.constraints.size() + " Constraints:");

        for (int i = 0; i < world.constraints.size(); i++) {
            logger.info("constraint " + i + ": " + world.constraints.get(i));
        }
    }

    public void clearDice() {
        for (PhysicsBody body : new ArrayList<>(diceList.values())) {
            world.removeBody(body);
        }
        diceList.clear();
    }
}