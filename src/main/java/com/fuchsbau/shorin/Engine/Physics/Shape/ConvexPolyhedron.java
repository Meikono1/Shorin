package com.fuchsbau.shorin.Engine.Physics.Shape;


import com.fuchsbau.shorin.Engine.Physics.Math.Quaternion;
import com.fuchsbau.shorin.Engine.Physics.Math.Transform;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConvexPolyhedron extends CollisionShape {
    private final Logger logger = FileLogger.getLogger();

    private static final double[] maxminA = new double[2];
    private static final double[] maxminB = new double[2];
    private static final Vec3 project_worldVertex = new Vec3();
    private static final Vec3 project_localAxis = new Vec3();
    private static final Vec3 project_localOrigin = new Vec3();

    public List<Vec3> vertices;

    public List<int[]> faces;
    public List<Vec3> faceNormals;

    public List<Vec3> worldVertices;
    public boolean worldVerticesNeedsUpdate;
    public List<Vec3> worldFaceNormals;
    public boolean worldFaceNormalsNeedsUpdate;

    public List<Vec3> uniqueAxes;
    public List<Vec3> uniqueEdges;

    public ConvexPolyhedron(
            List<Vec3> vertices,
            List<int[]> faces,
            List<Vec3> normals,
            List<Vec3> axes,
            Double boundingRadius
    ) {
        super(new CollisionShapeOptions(1, -1, true, ShapeType.CONVEXPOLYHEDRON));

        this.vertices = vertices != null ? vertices : new ArrayList<>();
        this.faces = faces != null ? faces : new ArrayList<>();
        this.faceNormals = normals != null ? normals : new ArrayList<>();

        if (this.faceNormals.isEmpty()) {
            computeNormals();
        }

        if (boundingRadius == null) {
            updateBoundingSphereRadius();
        } else {
            this.boundingSphereRadius = boundingRadius;
        }

        this.worldVertices = new ArrayList<>();
        this.worldVerticesNeedsUpdate = true;
        this.worldFaceNormals = new ArrayList<>();
        this.worldFaceNormalsNeedsUpdate = true;
        this.uniqueAxes = axes != null ? new ArrayList<>(axes) : null;
        this.uniqueEdges = new ArrayList<>();
        this.computeEdges();
    }

    public void computeEdges() {
        uniqueEdges.clear();
        Vec3 edge = new Vec3();

        for (int[] face : faces) {
            int numVertices = face.length;
            for (int j = 0; j < numVertices; j++) {
                int k = (j + 1) % numVertices;
                vertices.get(face[j]).sub(vertices.get(face[k]), edge);
                edge.normalize();

                boolean found = false;
                for (Vec3 e : uniqueEdges) {
                    if (e.almostEquals(edge)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    uniqueEdges.add(edge.clone());
                }
            }
        }
    }

    public void computeNormals() {
        // faceNormals auf Faces-Größe bringen
        while (faceNormals.size() < faces.size()) faceNormals.add(new Vec3());
        while (faceNormals.size() > faces.size()) faceNormals.remove(faceNormals.size() - 1);

        for (int i = 0; i < faces.size(); i++) {
            int[] face = faces.get(i);

            // Prüfen ob alle Vertices existieren
            for (int j = 0; j < face.length; j++) {
                if (face[j] >= vertices.size()) {
                    throw new RuntimeException("Vertex " + face[j] + " not found!");
                }
            }

            Vec3 n = faceNormals.get(i);
            getFaceNormal(i, n);
            n.negate(n);
            faceNormals.set(i, n);

            Vec3 vertex = vertices.get(face[0]);
            if (n.dot(vertex) < 0) {
                logger.warning(".faceNormals[" + i + "] = " + n + " zeigt in die Shape – Vertices CCW prüfen.");
            }
        }
    }

    public void getFaceNormal(int i, Vec3 target) {
        int[] f = faces.get(i);
        Vec3 va = vertices.get(f[0]);
        Vec3 vb = vertices.get(f[1]);
        Vec3 vc = vertices.get(f[2]);
        computeNormal(va, vb, vc, target);
    }

    public static void computeNormal(Vec3 va, Vec3 vb, Vec3 vc, Vec3 target) {
        Vec3 cb = new Vec3();
        Vec3 ab = new Vec3();
        vb.sub(va, ab);
        vc.sub(vb, cb);
        cb.cross(ab, target);
        if (!target.isZero()) {
            target.normalize();
        }
    }

    public void clipAgainstHull(
            Vec3 posA, Quaternion quatA,
            ConvexPolyhedron hullB, Vec3 posB, Quaternion quatB,
            Vec3 separatingNormal,
            double minDist, double maxDist,
            List<ConvexPolyhedronContactPoint> result
    ) {
        Vec3 worldNormal = new Vec3();
        int closestFaceB = -1;
        double dmax = -Double.MAX_VALUE;

        for (int face = 0; face < hullB.faces.size(); face++) {
            worldNormal.copy(hullB.faceNormals.get(face));
            quatB.vmult(worldNormal, worldNormal);
            double d = worldNormal.dot(separatingNormal);
            if (d > dmax) {
                dmax = d;
                closestFaceB = face;
            }
        }

        List<Vec3> worldVertsB1 = new ArrayList<>();
        for (int i = 0; i < hullB.faces.get(closestFaceB).length; i++) {
            Vec3 worldb = new Vec3();
            worldb.copy(hullB.vertices.get(hullB.faces.get(closestFaceB)[i]));
            quatB.vmult(worldb, worldb);
            posB.add(worldb, worldb);
            worldVertsB1.add(worldb);
        }

        if (closestFaceB >= 0) {
            clipFaceAgainstHull(separatingNormal, posA, quatA, worldVertsB1, minDist, maxDist, result);
        }
    }

    public boolean findSeparatingAxis(
            ConvexPolyhedron hullB,
            Vec3 posA, Quaternion quatA,
            Vec3 posB, Quaternion quatB,
            Vec3 target,
            int[] faceListA,
            int[] faceListB
    ) {
        Vec3 faceANormalWS3 = new Vec3();
        Vec3 worldNormal1 = new Vec3();
        Vec3 deltaC = new Vec3();
        Vec3 worldEdge0 = new Vec3();
        Vec3 worldEdge1 = new Vec3();
        Vec3 cross = new Vec3();

        double dmin = Double.MAX_VALUE;

        // Face Normals von hullA testen
        if (uniqueAxes == null) {
            int numFacesA = faceListA != null ? faceListA.length : faces.size();
            for (int i = 0; i < numFacesA; i++) {
                int fi = faceListA != null ? faceListA[i] : i;
                faceANormalWS3.copy(faceNormals.get(fi));
                quatA.vmult(faceANormalWS3, faceANormalWS3);

                Double d = testSepAxis(faceANormalWS3, hullB, posA, quatA, posB, quatB);
                if (d == null) return false;
                if (d < dmin) {
                    dmin = d;
                    target.copy(faceANormalWS3);
                }
            }
        } else {
            for (int i = 0; i < uniqueAxes.size(); i++) {
                quatA.vmult(uniqueAxes.get(i), faceANormalWS3);
                Double d = testSepAxis(faceANormalWS3, hullB, posA, quatA, posB, quatB);
                if (d == null) return false;
                if (d < dmin) {
                    dmin = d;
                    target.copy(faceANormalWS3);
                }
            }
        }

        // Face Normals von hullB testen
        if (hullB.uniqueAxes == null) {
            int numFacesB = faceListB != null ? faceListB.length : hullB.faces.size();
            for (int i = 0; i < numFacesB; i++) {
                int fi = faceListB != null ? faceListB[i] : i;
                worldNormal1.copy(hullB.faceNormals.get(fi));
                quatB.vmult(worldNormal1, worldNormal1);
                Double d = testSepAxis(worldNormal1, hullB, posA, quatA, posB, quatB);
                if (d == null) return false;
                if (d < dmin) {
                    dmin = d;
                    target.copy(worldNormal1);
                }
            }
        } else {
            for (int i = 0; i < hullB.uniqueAxes.size(); i++) {
                quatB.vmult(hullB.uniqueAxes.get(i), worldNormal1);
                Double d = testSepAxis(worldNormal1, hullB, posA, quatA, posB, quatB);
                if (d == null) return false;
                if (d < dmin) {
                    dmin = d;
                    target.copy(worldNormal1);
                }
            }
        }

        // Kanten testen
        for (int e0 = 0; e0 < uniqueEdges.size(); e0++) {
            quatA.vmult(uniqueEdges.get(e0), worldEdge0);
            for (int e1 = 0; e1 < hullB.uniqueEdges.size(); e1++) {
                quatB.vmult(hullB.uniqueEdges.get(e1), worldEdge1);
                worldEdge0.cross(worldEdge1, cross);
                if (!cross.almostZero()) {
                    cross.normalize();
                    Double dist = testSepAxis(cross, hullB, posA, quatA, posB, quatB);
                    if (dist == null) return false;
                    if (dist < dmin) {
                        dmin = dist;
                        target.copy(cross);
                    }
                }
            }
        }

        posB.sub(posA, deltaC);
        if (deltaC.dot(target) > 0.0) {
            target.negate(target);
        }

        return true;
    }

    public Double testSepAxis(
            Vec3 axis,
            ConvexPolyhedron hullB,
            Vec3 posA, Quaternion quatA,
            Vec3 posB, Quaternion quatB
    ) {
        ConvexPolyhedron.project(this, axis, posA, quatA, maxminA);
        ConvexPolyhedron.project(hullB, axis, posB, quatB, maxminB);

        double maxA = maxminA[0], minA = maxminA[1];
        double maxB = maxminB[0], minB = maxminB[1];

        if (maxA < minB || maxB < minA) {
            return null; // Separiert
        }

        double d0 = maxA - minB;
        double d1 = maxB - minA;
        return d0 < d1 ? d0 : d1;
    }

    public void calculateLocalInertia(double mass, Vec3 target) {
        Vec3 aabbmin = new Vec3();
        Vec3 aabbmax = new Vec3();
        computeLocalAABB(aabbmin, aabbmax);

        double x = aabbmax.x - aabbmin.x;
        double y = aabbmax.y - aabbmin.y;
        double z = aabbmax.z - aabbmin.z;

        target.x = (1.0 / 12.0) * mass * (2 * y * 2 * y + 2 * z * 2 * z);
        target.y = (1.0 / 12.0) * mass * (2 * x * 2 * x + 2 * z * 2 * z);
        target.z = (1.0 / 12.0) * mass * (2 * y * 2 * y + 2 * x * 2 * x);
    }

    public double getPlaneConstantOfFace(int face_i) {
        int[] f = faces.get(face_i);
        Vec3 n = faceNormals.get(face_i);
        Vec3 v = vertices.get(f[0]);
        return -n.dot(v);
    }

    public void clipFaceAgainstHull(
            Vec3 separatingNormal,
            Vec3 posA, Quaternion quatA,
            List<Vec3> worldVertsB1,
            double minDist, double maxDist,
            List<ConvexPolyhedronContactPoint> result
    ) {
        Vec3 faceANormalWS = new Vec3();
        Vec3 edge0 = new Vec3();
        Vec3 worldEdge0 = new Vec3();
        Vec3 worldPlaneAnormal1 = new Vec3();
        Vec3 planeNormalWS1 = new Vec3();
        Vec3 worldA1 = new Vec3();
        Vec3 localPlaneNormal = new Vec3();
        Vec3 planeNormalWS = new Vec3();

        List<Vec3> pVtxIn = new ArrayList<>(worldVertsB1);
        List<Vec3> pVtxOut = new ArrayList<>();

        int closestFaceA = -1;
        double dmin = Double.MAX_VALUE;

        // Face mit Normal am nächsten zur Separationsachse finden
        for (int face = 0; face < faces.size(); face++) {
            faceANormalWS.copy(faceNormals.get(face));
            quatA.vmult(faceANormalWS, faceANormalWS);
            double d = faceANormalWS.dot(separatingNormal);
            if (d < dmin) {
                dmin = d;
                closestFaceA = face;
            }
        }

        if (closestFaceA < 0) return;

        int[] polyA = faces.get(closestFaceA);

        // Verbundene Faces finden
        List<Integer> connectedFaces = new ArrayList<>();
        for (int i = 0; i < faces.size(); i++) {
            if (i == closestFaceA) continue;
            for (int j = 0; j < faces.get(i).length; j++) {
                boolean found = false;
                for (int v : polyA) {
                    if (v == faces.get(i)[j]) {
                        found = true;
                        break;
                    }
                }
                if (found && !connectedFaces.contains(i)) {
                    connectedFaces.add(i);
                }
            }
        }

        // Polygon gegen angrenzende Faces clippen
        int numVerticesA = polyA.length;
        for (int i = 0; i < numVerticesA; i++) {
            Vec3 a = vertices.get(polyA[i]);
            Vec3 b = vertices.get(polyA[(i + 1) % numVerticesA]);

            a.sub(b, edge0);
            worldEdge0.copy(edge0);
            quatA.vmult(worldEdge0, worldEdge0);
            posA.add(worldEdge0, worldEdge0);

            worldPlaneAnormal1.copy(faceNormals.get(closestFaceA));
            quatA.vmult(worldPlaneAnormal1, worldPlaneAnormal1);
            posA.add(worldPlaneAnormal1, worldPlaneAnormal1);

            worldEdge0.cross(worldPlaneAnormal1, planeNormalWS1);
            planeNormalWS1.negate(planeNormalWS1);

            worldA1.copy(a);
            quatA.vmult(worldA1, worldA1);
            posA.add(worldA1, worldA1);

            int otherFace = connectedFaces.get(i);
            localPlaneNormal.copy(faceNormals.get(otherFace));
            double localPlaneEq = getPlaneConstantOfFace(otherFace);

            planeNormalWS.copy(localPlaneNormal);
            quatA.vmult(planeNormalWS, planeNormalWS);
            double planeEqWS = localPlaneEq - planeNormalWS.dot(posA);

            clipFaceAgainstPlane(pVtxIn, pVtxOut, planeNormalWS, planeEqWS);

            pVtxIn.clear();
            pVtxIn.addAll(pVtxOut);
            pVtxOut.clear();
        }

        // Nur Kontaktpunkte hinter der Witness-Face behalten
        localPlaneNormal.copy(faceNormals.get(closestFaceA));
        double localPlaneEq = getPlaneConstantOfFace(closestFaceA);
        planeNormalWS.copy(localPlaneNormal);
        quatA.vmult(planeNormalWS, planeNormalWS);
        double planeEqWS = localPlaneEq - planeNormalWS.dot(posA);

        for (Vec3 point : pVtxIn) {
            double depth = planeNormalWS.dot(point) + planeEqWS;

            if (depth <= minDist) {
                depth = minDist;
            }

            if (depth <= maxDist && depth <= 1e-6) {
                result.add(new ConvexPolyhedronContactPoint(point, planeNormalWS, depth));
            }
        }
    }

    public List<Vec3> clipFaceAgainstPlane(
            List<Vec3> inVertices, List<Vec3> outVertices,
            Vec3 planeNormal, double planeConstant
    ) {
        int numVerts = inVertices.size();
        if (numVerts < 2) return outVertices;

        Vec3 firstVertex = inVertices.get(numVerts - 1);
        Vec3 lastVertex = inVertices.get(0);

        double n_dot_first = planeNormal.dot(firstVertex) + planeConstant;
        double n_dot_last;

        for (int vi = 0; vi < numVerts; vi++) {
            lastVertex = inVertices.get(vi);
            n_dot_last = planeNormal.dot(lastVertex) + planeConstant;

            if (n_dot_first < 0) {
                if (n_dot_last < 0) {
                    // Start < 0, End < 0 → lastVertex ausgeben
                    Vec3 newv = new Vec3();
                    newv.copy(lastVertex);
                    outVertices.add(newv);
                } else {
                    // Start < 0, End >= 0 → Schnittpunkt ausgeben
                    Vec3 newv = new Vec3();
                    firstVertex.lerp(lastVertex, n_dot_first / (n_dot_first - n_dot_last), newv);
                    outVertices.add(newv);
                }
            } else {
                if (n_dot_last < 0) {
                    // Start >= 0, End < 0 → Schnittpunkt und End ausgeben
                    Vec3 newv = new Vec3();
                    firstVertex.lerp(lastVertex, n_dot_first / (n_dot_first - n_dot_last), newv);
                    outVertices.add(newv);
                    outVertices.add(lastVertex);
                }
            }

            firstVertex = lastVertex;
            n_dot_first = n_dot_last;
        }

        return outVertices;
    }

    public void computeWorldVertices(Vec3 position, Quaternion quat) {
        while (worldVertices.size() < vertices.size()) {
            worldVertices.add(new Vec3());
        }

        for (int i = 0; i < vertices.size(); i++) {
            quat.vmult(vertices.get(i), worldVertices.get(i));
            position.add(worldVertices.get(i), worldVertices.get(i));
        }

        worldVerticesNeedsUpdate = false;
    }

    public void computeLocalAABB(Vec3 aabbmin, Vec3 aabbmax) {
        aabbmin.set(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        aabbmax.set(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

        for (Vec3 v : vertices) {
            if (v.x < aabbmin.x) aabbmin.x = v.x;
            else if (v.x > aabbmax.x) aabbmax.x = v.x;

            if (v.y < aabbmin.y) aabbmin.y = v.y;
            else if (v.y > aabbmax.y) aabbmax.y = v.y;

            if (v.z < aabbmin.z) aabbmin.z = v.z;
            else if (v.z > aabbmax.z) aabbmax.z = v.z;
        }
    }

    public void computeWorldFaceNormals(Quaternion quat) {
        int N = faceNormals.size();
        while (worldFaceNormals.size() < N) {
            worldFaceNormals.add(new Vec3());
        }

        for (int i = 0; i < N; i++) {
            quat.vmult(faceNormals.get(i), worldFaceNormals.get(i));
        }

        worldFaceNormalsNeedsUpdate = false;
    }

    public void updateBoundingSphereRadius() {
        double max2 = 0;
        for (Vec3 v : vertices) {
            double norm2 = v.lengthSquared();
            if (norm2 > max2) max2 = norm2;
        }
        boundingSphereRadius = Math.sqrt(max2);
    }

    public void calculateWorldAABB(Vec3 pos, Quaternion quat, Vec3 min, Vec3 max) {
        Vec3 temp = new Vec3();

        double minx = Double.MAX_VALUE, miny = Double.MAX_VALUE, minz = Double.MAX_VALUE;
        double maxx = -Double.MAX_VALUE, maxy = -Double.MAX_VALUE, maxz = -Double.MAX_VALUE;

        for (Vec3 v : vertices) {
            temp.copy(v);
            quat.vmult(temp, temp);
            pos.add(temp, temp);

            if (temp.x < minx) minx = temp.x;
            if (temp.x > maxx) maxx = temp.x;
            if (temp.y < miny) miny = temp.y;
            if (temp.y > maxy) maxy = temp.y;
            if (temp.z < minz) minz = temp.z;
            if (temp.z > maxz) maxz = temp.z;
        }

        min.set(minx, miny, minz);
        max.set(maxx, maxy, maxz);
    }

    public double volume() {
        return (4.0 * Math.PI * this.boundingSphereRadius) / 3.0;
    }

    public Vec3 getAveragePointLocal(Vec3 target) {
        if (target == null) target = new Vec3();

        for (Vec3 v : vertices) {
            v.add(target, target);
        }
        target.scale(1.0 / vertices.size(), target);
        return target;
    }

    public void transformAllPoints(Vec3 offset, Quaternion quat) {
        // Rotation anwenden
        if (quat != null) {
            for (Vec3 v : vertices) {
                quat.vmult(v, v);
            }
            for (Vec3 v : faceNormals) {
                quat.vmult(v, v);
            }
        }

        // Offset anwenden
        if (offset != null) {
            for (Vec3 v : vertices) {
                v.add(offset, v);
            }
        }
    }

    // Rückgabe: 1, -1 oder 0 (0 = false, außerhalb)
    public int pointIsInside(Vec3 p) {
        Vec3 pointInside = new Vec3();
        getAveragePointLocal(pointInside);

        for (int i = 0; i < faces.size(); i++) {
            Vec3 n = faceNormals.get(i);
            Vec3 v = vertices.get(faces.get(i)[0]);

            Vec3 vToP = new Vec3();
            p.sub(v, vToP);
            double r1 = n.dot(vToP);

            Vec3 vToPointInside = new Vec3();
            pointInside.sub(v, vToPointInside);
            double r2 = n.dot(vToPointInside);

            if ((r1 < 0 && r2 > 0) || (r1 > 0 && r2 < 0)) {
                return 0; // außerhalb
            }
        }

        return -1; // positiveResult ist null → false → -1
    }

    public static void project(ConvexPolyhedron shape, Vec3 axis, Vec3 pos, Quaternion quat, double[] result) {
        Vec3 localAxis = project_localAxis;
        Vec3 localOrigin = project_localOrigin;

        localOrigin.setZero();

        // Achse in lokalen Frame transformieren
        Transform.vectorToLocalFrame(pos, quat, axis, localAxis);
        Transform.pointToLocalFrame(pos, quat, localOrigin, localOrigin);
        double add = localOrigin.dot(localAxis);

        double max = shape.vertices.get(0).dot(localAxis);
        double min = max;

        for (int i = 1; i < shape.vertices.size(); i++) {
            double val = shape.vertices.get(i).dot(localAxis);
            if (val > max) max = val;
            if (val < min) min = val;
        }

        min -= add;
        max -= add;

        if (min > max) {
            double temp = min;
            min = max;
            max = temp;
        }

        result[0] = max;
        result[1] = min;
    }
}