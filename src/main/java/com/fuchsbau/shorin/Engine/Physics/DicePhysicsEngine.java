package com.fuchsbau.shorin.Engine.Physics;

import com.fuchsbau.shorin.Engine.Dice.DiceOptions;
import com.fuchsbau.shorin.Engine.Dice.DiceTray;
import com.fuchsbau.shorin.Engine.Physics.Shape.CollisionShape;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Util.DiceSpawnData;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.logging.Logger;

public class DicePhysicsEngine {

    private static final Logger logger = FileLogger.getLogger();
    private final Map<Integer, PhysicsBody> activeDice = new HashMap<>();

    private DiceTray tray;

    private boolean throwActive;
    private boolean throwFinished;

    private int iteration;
    private int minIterations = 30;
    private int maxIterations = 1000;

    private double fixedTimeStep = 1.0 / 60.0;
    private double simulationTime;
    private static final double RENDER_SCALE = 40.0;

    // Lichtvektor für Shading – normiert
   // private static final Vec3 LIGHT_DIR = new Vec3(0.5, -0.7, -1.0).normalize();

    // Farben pro Zustand
    private static final Color COLOR_ROLLING = Color.STEELBLUE;
    private static final Color COLOR_SLEEPING = Color.DARKSEAGREEN;
    private static final double SHADOW_ALPHA = 0.18;

    public void initWorld(DiceTray tray) {
        this.tray = tray;
        resetSimulation();
    }

    public void resetSimulation() {
        activeDice.clear();
        throwActive = false;
        throwFinished = false;
        iteration = 0;
        simulationTime = 0.0;
    }

    public void addDie(int id, PhysicsBody die) {
        activeDice.put(id, die);
        onBodyAdded(die);
    }

    public void removeDie(PhysicsBody die) {
        activeDice.remove(die);
        onBodyRemoved(die);
    }

    public List<PhysicsBody> getActiveDice() {
        List<PhysicsBody> ret = new ArrayList<>(activeDice.size());
        for (PhysicsBody object : activeDice.values()) {

            // TODO:
        }

        return ret;
    }

    public void startThrow() {
        throwActive = true;
        throwFinished = false;
        iteration = 0;
        simulationTime = 0.0;

        for (PhysicsBody die : activeDice.values()) {
            wakeBody(die);
            onThrowStarted(die);
        }
    }

    public void simulateThrow() {
        startThrow();

        while (!isThrowFinished()) {
            stepWorld(fixedTimeStep);
        }

        finishThrow();
    }

    public void stepWorld(double dt) {
        if (!throwActive || throwFinished) {
            return;
        }
        iteration++;
        simulationTime += dt;

        preStep(dt);

        applyGravity(dt);
        applyExternalForces(dt);
        applyDamping(dt);

        detectCollisions();
        solveContacts(dt);

        integrateBodies(dt);
        updateSleepStates(simulationTime);

        postStep(dt);

        if (isThrowFinished()) {
            finishThrow();
        }
    }

    public boolean isThrowFinished() {
        if (!throwActive) {
            return true;
        }

        if (!hasReachedMinIterations()) {
            return false;
        }

        if (iteration >= maxIterations) {
            return true;
        }

        return haveAllBodiesSlept();
    }

    public void finishThrow() {
        if (throwFinished) {
            return;
        }

        throwActive = false;
        throwFinished = true;

        for (PhysicsBody die : activeDice.values()) {
            finishDiceResult(die);
            onThrowFinished(die);
        }
    }

    public boolean hasReachedMinIterations() {
        return iteration >= minIterations;
    }

    public boolean haveAllBodiesSlept() {
        for (PhysicsBody die : activeDice.values()) {
            if (!isBodySleeping(die)) {
                return false;
            }
        }
        return true;
    }

    protected void preStep(double dt) {
        // Hook für Debug, Profiling, Buffering
    }

    protected void postStep(double dt) {
        // Hook für Debug, Profiling, Events
    }

    protected void onBodyAdded(PhysicsBody die) {
        // Optionaler Hook
    }

    protected void onBodyRemoved(PhysicsBody die) {
        // Optionaler Hook
    }

    protected void onThrowStarted(PhysicsBody die) {
        // Optionaler Hook
    }

    protected void onThrowFinished(PhysicsBody die) {
        // Optionaler Hook
    }

    protected void applyGravity(double dt) {
        for (PhysicsBody die : activeDice.values()) {

            // TODO:
        }
    }

    protected void applyExternalForces(double dt) {
        for (PhysicsBody die : activeDice.values()) {
            if (isBodySleeping(die)) {
                continue;
            }

            // TODO:
            // torque / impulses / user interaction / tray effects
        }
    }

    protected void applyDamping(double dt) {
        for (PhysicsBody die : activeDice.values()) {
            if (isBodySleeping(die)) {
                continue;
            }

            // TODO:
            // linear damping
            // angular damping
        }
    }

    protected void detectCollisions() {
        // TODO:
        // 1. collect contacts die vs floor
        // 2. collect contacts die vs walls
        // 3. later: die vs die
    }

    protected void solveContacts(double dt) {
        // TODO:
        // iterate over contacts
        // solve normal impulse
        // solve friction impulse
        // multiple iterations
    }

    protected void integrateBodies(double dt) {
        for (PhysicsBody die : activeDice.values()) {
            if (isBodySleeping(die)) {
                continue;
            }

            integrateBody(die, dt);
        }
    }

    // 1:1 aus Body.integrate() – Leap Frog Integration
    protected void integrateBody(PhysicsBody die, double dt) {

        // TODO:
        /*die.setPreviousPosition(die.getPosition());
        die.setPreviousRotation(die.getRotation());

        Vec3 vel = die.getVelocity();
        Vec3 angVel = die.getAngularVelocity();
        Vec3 pos = die.getPosition();
        Vec3 force = die.getForce();
        double invMass = die.getInverseMass();

        // velo += force * invMass * dt  (Leap Frog)
        double iMdt = invMass * dt;
        die.setVelocity(new Vec3(
                vel.x() + force.x() * iMdt,
                vel.y() + force.y() * iMdt,
                vel.z() + force.z() * iMdt
        ));

        // pos += velo * dt
        vel = die.getVelocity();
        die.setPosition(new Vec3(
                pos.x() + vel.x() * dt,
                pos.y() + vel.y() * dt,
                pos.z() + vel.z() * dt
        ));

        // Quaternion integrieren
        Quaternion q = die.getRotation();
        q.integrate(die.getAngularVelocity(), dt);
        q.normalize();
        die.setRotation(q);

        // Force zurücksetzen nach Integration – wie World.clearForces()
        die.setForce(new Vec3(0, 0, 0));

        // Bodenkollision
        resolveFloorContact(die);

         */
    }

    private void resolveFloorContact(PhysicsBody die) {
        /*if (!(die.getShape() instanceof ConvexHullShape hull)) return;

        Vec3       convexPosition = die.getPosition();
        Quaternion convexQuat     = die.getRotation();

        // worldNormal = (0,0,1) – Boden-Normal, unveränderlich da Boden nicht rotiert
        Vec3 worldNormal = new Vec3(0, 0, 1);

        // planePosition = (0,0,0)
        Vec3 planePosition = new Vec3(0, 0, 0);

        int numContacts = 0;
        double maxPenetration = 0;

        for (Vec3 localVertex : hull.getVertices()) {
            // worldVertex = convexQuat.vmult(vertex) + convexPosition
            Vec3 worldVertex = convexQuat.vmult(localVertex.scale(RENDER_SCALE)).add(convexPosition);

            // relpos = worldVertex - planePosition
            Vec3 relpos = worldVertex.sub(planePosition);

            // dot = worldNormal · relpos
            double dot = worldNormal.dot(relpos);

            // dot <= 0 bedeutet Vertex ist unter oder auf dem Boden
            if (dot <= 0.0) {
                numContacts++;
                maxPenetration = Math.max(maxPenetration, -dot);
            }
        }

        if (numContacts == 0) return;

        logger.fine("resolveFloorContact – " + die.getType()
                + " contacts=" + numContacts
                + " pen=" + String.format("%.3f", maxPenetration));

        // Positionskorrektur – aus dem Boden schieben
        Vec3 pos = die.getPosition();
        die.setPosition(new Vec3(pos.x(), pos.y(), pos.z() + maxPenetration));

        // Impuls nur wenn Würfel in Boden fährt
        Vec3 v = die.getVelocity();
        if (v.z() >= 0) return;

        double restitution = 0.5;
        double newVz = -v.z() * restitution;

        double friction = 0.01;
        die.setVelocity(new Vec3(
                v.x() * (1.0 - friction),
                v.y() * (1.0 - friction),
                newVz
        ));

        // Angular velocity dämpfen bei Kontakt
        Vec3 av = die.getAngularVelocity();
        die.setAngularVelocity(new Vec3(
                av.x() * 0.9,
                av.y() * 0.9,
                av.z() * 0.9
        ));

         */
    }

    protected void updateSleepStates(double time) {
        for (PhysicsBody die : activeDice.values()) {
            updateBodySleepState(die, time);
        }
    }

    protected void updateBodySleepState(PhysicsBody die, double time) {
        // TODO:
        // awake / sleepy / sleeping
        // compare linear + angular speed against limits
    }

    protected boolean isBodySleeping(PhysicsBody die) {
        // TODO:
        // replace with real sleep state later
        //return die.getSleepState().equals(SleepState.SLEEPING);
        return false;
    }

    protected void wakeBody(PhysicsBody die) {
        /*die.setSleeping(false);
        die.setRolling(true);

         */
    }

    protected void sleepBody(PhysicsBody die) {
        /*die.setSleeping(true);
        die.setRolling(false);

         */
    }

    protected void finishDiceResult(PhysicsBody die) {
        int result;

        /*if (die.getType() == DiceType.D2) {
            result = determineD2Value(die);
        } else {
            result = determineTopFaceValue(die);
        }*/

        //die.setCurrentFaceValue(result);
        //die.setLastStableFaceValue(result);
    }

    protected int determineTopFaceValue(PhysicsBody die) {
        // TODO:
        // delegate to future contact/face-normal system
        return 1;
    }

    protected int determineD2Value(PhysicsBody die) {
        // TODO:
        // special case: z-axis orientation
        return 1;
    }

    public boolean isThrowActive() {
        return throwActive;
    }

    public boolean isSimulationFinished() {
        return throwFinished;
    }

    public int getIteration() {
        return iteration;
    }

    public double getFixedTimeStep() {
        return fixedTimeStep;
    }

    public void setFixedTimeStep(double fixedTimeStep) {
        this.fixedTimeStep = fixedTimeStep;
    }

    public int getMinIterations() {
        return minIterations;
    }

    public void setMinIterations(int minIterations) {
        this.minIterations = minIterations;
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public DiceTray getTray() {
        return tray;
    }

    public PhysicsBody createDie(int id, CollisionShape shape, DiceSpawnData spawnData) {
        return createDie(id, shape, spawnData, null);
    }

    public PhysicsBody createDie(int id, CollisionShape shape, DiceSpawnData spawnData, DiceOptions options) {
        return null;
    }
        /*PhysicsBody physicsBody = new PhysicsBody(spawnData.type, 70, 1, spawnData.mass, shape);
        physicsBody.setForce(new Vec3(0, 0, 0));
        physicsBody.setPosition(spawnData.position.x(), spawnData.position.y(), spawnData.position.z());
        physicsBody.setLinearDamping(0.1);
        physicsBody.setAngularDamping(0.1);
        physicsBody.setInverseMass(1.0 / spawnData.mass);

        physicsBody.setRotationFromAxisAngle(
                spawnData.axis,
                spawnData.angle * Math.PI * 2.0
        );

        physicsBody.setVelocity(new Vec3(
                        spawnData.velocity.x(),
                        spawnData.velocity.y(),
                        spawnData.velocity.z()
                )
        );

        physicsBody.setAngularVelocity(new Vec3(
                        spawnData.angularVelocity.x(),
                        spawnData.angularVelocity.y(),
                        spawnData.angularVelocity.z()
                )
        );

        if (options != null) {
            //todo apply options.
        }

        activeDice.put(id, physicsBody);

        return physicsBody;
    }

    public void drawDice(GraphicsContext g) {
        logger.fine("drawDice – Würfel aktiv: " + activeDice.size());
        for (PhysicsBody body : activeDice.values()) {
            drawDie(g, body);
        }
    }

    private void drawDie(GraphicsContext g, PhysicsBody body) {
        if (!(body.getShape() instanceof ConvexHullShape hull)) {
            logger.warning("drawDie – kein ConvexHullShape: " + body.getType());
            return;
        }

        List<Vec3> world = buildWorldVertices(body, hull);
        List<double[]> screen = projectVertices(world);

        drawShadow(g, body);
        drawMesh(g, body, hull, world, screen);
        drawLabel(g, body);
    }

    private List<Vec3> buildWorldVertices(PhysicsBody body, ConvexHullShape hull) {
        Vec3 pos = body.getPosition();
        Quaternion rot = body.getRotation();

        List<Vec3> result = new ArrayList<>(hull.getVertices().size());
        for (Vec3 v : hull.getVertices()) {
            result.add(rot.vmult(v.scale(RENDER_SCALE)).add(pos));
        }

        logger.finest("buildWorldVertices – " + result.size() + " Vertices");
        return result;
    }

    // Isometrische Projektion: Z hebt Y an
    private List<double[]> projectVertices(List<Vec3> world) {
        List<double[]> result = new ArrayList<>(world.size());
        for (Vec3 v : world) {
            result.add(new double[]{v.x(), v.y() - v.z() * 0.35});
        }
        return result;
    }

    private void drawShadow(GraphicsContext g, PhysicsBody body) {
        Vec3 pos = body.getPosition();
        double r = body.getShape().getBoundingRadius();

        g.setFill(Color.rgb(0, 0, 0, SHADOW_ALPHA));
        g.fillOval(pos.x() - r, pos.y() - r * 0.5, r * 2, r);

        logger.finest("drawShadow – pos=(" + pos.x() + "," + pos.y() + ")");
    }

    private void drawMesh(GraphicsContext g, PhysicsBody body,
                          ConvexHullShape hull,
                          List<Vec3> world,
                          List<double[]> screen) {

        List<int[]> faces = hull.getFaces();

        // Painter's Algorithm – hinten zuerst
        List<int[]> sorted = new ArrayList<>(faces);
        sorted.sort((a, b) -> Double.compare(avgDepth(b, world), avgDepth(a, world)));

        Color base = body.isSleeping() ? COLOR_SLEEPING : COLOR_ROLLING;

        for (int[] face : sorted) {
            if (face.length < 3) continue;

            // Face-Normal aus Weltkoordinaten
            Vec3 a = world.get(face[0]);
            Vec3 b = world.get(face[1]);
            Vec3 c = world.get(face[2]);
            Vec3 n = b.sub(a).cross(c.sub(a)).normalize();

            double[] xs = new double[face.length];
            double[] ys = new double[face.length];
            for (int i = 0; i < face.length; i++) {
                xs[i] = screen.get(face[i])[0];
                ys[i] = screen.get(face[i])[1];
            }

            // Lambertsche Beleuchtung – nur Z-Komponente der Normal
            double shade = Math.max(0.25, Math.min(1.0, -n.z()));

            g.setFill(new Color(
                    clamp(base.getRed() * shade),
                    clamp(base.getGreen() * shade),
                    clamp(base.getBlue() * shade),
                    1.0
            ));
            g.fillPolygon(xs, ys, face.length);

            g.setStroke(Color.BLACK);
            g.setLineWidth(0.8);
            g.strokePolygon(xs, ys, face.length);
        }
    }

    private void drawLabel(GraphicsContext g, PhysicsBody body) {
        Vec3 pos = body.getPosition();
        double r = body.getShape().getBoundingRadius();
        double sx = pos.x();
        double sy = pos.y() - pos.z() * 0.35;

        String state = body.isSleeping() ? "STOP" : "ROLL";

        g.setFill(Color.WHITE);
        g.fillText(body.getType().name(), sx - 10, sy - r - 8);
        g.fillText(state, sx - 10, sy + 4);
    }

    private double avgDepth(int[] face, List<Vec3> world) {
        double sum = 0.0;
        for (int i : face) sum += world.get(i).z();
        return sum / face.length;
    }*/
}