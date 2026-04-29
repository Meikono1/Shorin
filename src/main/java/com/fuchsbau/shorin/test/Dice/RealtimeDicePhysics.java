package com.fuchsbau.shorin.test.Dice;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Physics.Collision.NaiveBroadphase;
import com.fuchsbau.shorin.Engine.Physics.Material.ContactMaterial;
import com.fuchsbau.shorin.Engine.Physics.Material.Material;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.*;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceShape;
import com.fuchsbau.shorin.Engine.Physics.World.World;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.*;
import java.util.logging.Logger;

public class RealtimeDicePhysics {
    private static final Logger log = FileLogger.getLogger();

    public static final double BARRIERS_SCALE = 0.97;

    private final List<PhysicsBody> barriers = new ArrayList<>();

    private World world;
    private Material diceBodyMaterial;
    private Material deskBodyMaterial;
    private Material barrierBodyMaterial;

    public final Map<Integer, PhysicsBody> diceList = new LinkedHashMap<>();
    private final Map<String, CollisionShape> shapeList = new HashMap<>();

    public void init(double height, double width, Margin margin) {
        log.info("[Physics] init – h=" + height + " w=" + width);
        this.world = new World();
        this.diceBodyMaterial = new Material();
        this.deskBodyMaterial = new Material();
        this.barrierBodyMaterial = new Material();

        world.gravity.set(0, 0, -9.8 * 50);
        world.broadphase = new NaiveBroadphase();
        world.solver.iterations = 24;
        world.allowSleep = false;

        addContactMaterials();
        addDesk();
        addBarriers(height, width, margin);
        log.info("[Physics] init fertig – bodies=" + world.bodies.size());
    }

    private void addContactMaterials() {
        world.addContactMaterial(new ContactMaterial(deskBodyMaterial, diceBodyMaterial, 0.35, 0.10));
        world.addContactMaterial(new ContactMaterial(barrierBodyMaterial, diceBodyMaterial, 0.20, 0.20));
        world.addContactMaterial(new ContactMaterial(diceBodyMaterial, diceBodyMaterial, 0.15, 0.15));
    }

    private void addDesk() {
        PhysicsBodyOptions options = new PhysicsBodyOptions();
        options.type = BodyType.STATIC;
        options.shapes.add(new Plane());
        options.material = deskBodyMaterial;

        PhysicsBody desk = new PhysicsBody(options);
        desk.allowSleep = false;
        world.addBody(desk);
    }

    private void addBarriers(double height, double width, Margin margin) {
        barriers.clear();

        double[][] config = {
                {1, 0, 0, Math.PI / 2, 0, (height - margin.top * 2) * BARRIERS_SCALE, 0},
                {1, 0, 0, -Math.PI / 2, 0, (-height + margin.bottom * 2) * BARRIERS_SCALE, 0},
                {0, 1, 0, -Math.PI / 2, (width - margin.right * 2) * BARRIERS_SCALE, 0, 0},
                {0, 1, 0, Math.PI / 2, (-width + margin.left * 2) * BARRIERS_SCALE, 0, 0}
        };

        for (double[] c : config) {
            Vec3 axis = new Vec3(c[0], c[1], c[2]);
            double angle = c[3];
            Vec3 position = new Vec3(c[4], c[5], c[6]);

            PhysicsBodyOptions options = new PhysicsBodyOptions();
            options.type = BodyType.STATIC;
            options.shapes.add(new Plane());
            options.material = barrierBodyMaterial;

            PhysicsBody barrier = new PhysicsBody(options);
            barrier.allowSleep = false;
            barrier.quaternion.setFromAxisAngle(axis, angle);
            barrier.position.copy(position);

            world.addBody(barrier);
            barriers.add(barrier);
        }
    }

    public void updateBarriers(double height, double width, Margin margin) {
        if (barriers.size() < 4) return;

        barriers.get(0).position.set(0, (height - margin.top * 2) * BARRIERS_SCALE, 0);
        barriers.get(1).position.set(0, (-height + margin.bottom * 2) * BARRIERS_SCALE, 0);
        barriers.get(2).position.set((width - margin.right * 2) * BARRIERS_SCALE, 0, 0);
        barriers.get(3).position.set((-width + margin.left * 2) * BARRIERS_SCALE, 0, 0);
    }

    public void createShape(String type, double radius) {
        log.info("[Physics] createShape type=" + type + " radius=" + radius);
        DiceShape.DiceShapeData data = DiceShape.get(type);

        switch (data.type) {
            case CONVEXPOLYHEDRON ->
                    shapeList.put(type, loadGeom(data.vertices, data.faces, radius, data.skipLastFaceIndex));
            case CYLINDER -> shapeList.put(type, new Cylinder(
                    radius * data.radiusTop,
                    radius * data.radiusBottom,
                    radius * data.height,
                    data.numSegments
            ));
            default -> throw new IllegalArgumentException("Unbekannter Shape-Typ: " + data.type);
        }
        log.info("[Physics] Shape erstellt: " + type);
    }

    private ConvexPolyhedron loadGeom(double[][] vertices, int[][] faces, double radius, boolean skipLastFaceIndex) {
        List<Vec3> vectors = new ArrayList<>(vertices.length);
        List<int[]> faceList = new ArrayList<>(faces.length);

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

    public void createDice(int id, String shape, String material, VectorData vectordata,
                           double mass, DiceOptions options) {
        log.info("[Physics] createDice id=" + id + " shape=" + shape);

        CollisionShape collisionShape = shapeList.get(shape);
        if (collisionShape == null) {
            throw new IllegalStateException("Shape nicht erstellt: " + shape);
        }

        PhysicsBodyOptions bodyOptions = new PhysicsBodyOptions();
        bodyOptions.type = BodyType.DYNAMIC;
        bodyOptions.shapes.add(collisionShape);
        bodyOptions.material = diceBodyMaterial;
        bodyOptions.mass = mass;
        bodyOptions.allowSleep = false;

        PhysicsBody body = new PhysicsBody(bodyOptions);

        body.position.set(vectordata.pos.x, vectordata.pos.y, vectordata.pos.z);
        body.quaternion.setFromAxisAngle(
                new Vec3(vectordata.axis.x, vectordata.axis.y, vectordata.axis.z),
                vectordata.axis.a * Math.PI * 2
        );
        body.angularVelocity.set(vectordata.angle.x, vectordata.angle.y, vectordata.angle.z);
        body.velocity.set(vectordata.velocity.x, vectordata.velocity.y, vectordata.velocity.z);

        body.linearDamping = 0.22;
        body.angularDamping = 0.28;

        body.diceType = vectordata.type;
        body.diceShape = shape;
        body.diceMaterial = material;
        body.secretRoll = options != null && options.secret;

        diceList.put(id, body);
        log.info("[Physics] Dice id=" + id + " body erstellt pos=(" +
                round1(vectordata.pos.x) + "," + round1(vectordata.pos.y) + "," + round1(vectordata.pos.z) + ")");
    }

    private double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    public void addDice(int id) {
        PhysicsBody dice = diceList.get(id);
        if (dice != null) {
            world.addBody(dice);
        }
    }

    public void clearDice() {
        log.info("[Physics] clearDice – " + diceList.size() + " Würfel entfernt");
        for (PhysicsBody body : new ArrayList<>(diceList.values())) {
            world.removeBody(body);
        }
        diceList.clear();
    }

    public void step(double dt) {
        world.step(dt);
    }

    public Collection<PhysicsBody> getDiceBodies() {
        return diceList.values();
    }

    public PhysicsBody getDie(int id) {
        return diceList.get(id);
    }

    public int getContactCount() {
        return world != null && world.contacts != null ? world.contacts.size() : 0;
    }

    public int getFrictionContactCount() {
        return world != null && world.frictionEquations != null ? world.frictionEquations.size() : 0;
    }

    public int getBodyCount() {
        return world != null && world.bodies != null ? world.bodies.size() : 0;
    }

    public World getWorld(){
        return world;
    }

    public Integer getDiceValue(int id) {
        PhysicsBody dice = diceList.get(id);
        if (dice == null) return null;

        Vec3 vector = new Vec3(0, 0, dice.diceShape.equals("d4") ? -1 : 1);

        ConvexPolyhedron shape = (ConvexPolyhedron) dice.shapes.get(0);
        int[] faceValues = DiceShape.get(dice.diceShape).faceValues;

        int closestFace = 0;
        double closestAngle = Math.PI * 2;

        for (int i = 0; i < shape.faceNormals.size(); i++) {
            if (faceValues[i] == 0) continue;

            Vec3 faceWorld = new Vec3();
            faceWorld.copy(shape.faceNormals.get(i));
            dice.quaternion.vmult(faceWorld, faceWorld);

            double angle = faceWorld.angleTo(vector);
            if (angle < closestAngle) {
                closestAngle = angle;
                closestFace = i;
            }
        }

        return faceValues[closestFace];
    }
}