package com.fuchsbau.shorin.Engine.Physics.Solver.Equation;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;

public class ContactEquation extends Equation {
    private static final Vec3 ContactEquation_computeB_temp1 = new Vec3();
    private static final Vec3 ContactEquation_computeB_temp2 = new Vec3();
    private static final Vec3 ContactEquation_computeB_temp3 = new Vec3();
    private static final Vec3 getImpact_vi = new Vec3();
    private static final Vec3 getImpact_vj = new Vec3();
    private static final Vec3 getImpact_xi = new Vec3();
    private static final Vec3 getImpact_xj = new Vec3();
    private static final Vec3 getImpact_relVel = new Vec3();

    // "bounciness": u1 = -e*u0
    public double restitution = 0.0;

    // Vektor vom Zentrum von bi zum Kontaktpunkt
    public Vec3 ri = new Vec3();

    // Vektor vom Zentrum von bj zum Kontaktpunkt
    public Vec3 rj = new Vec3();

    // Kontaktnormale, zeigt aus body i heraus
    public Vec3 ni = new Vec3();

    public ContactEquation(PhysicsBody bodyA, PhysicsBody bodyB) {
        super(bodyA, bodyB, 0, 1e6);
    }

    public ContactEquation(PhysicsBody bodyA, PhysicsBody bodyB, double maxForce) {
        super(bodyA, bodyB, 0, maxForce);
    }

    @Override
    public double computeB(double h) {
        Vec3 rixn = ContactEquation_computeB_temp1;
        Vec3 rjxn = ContactEquation_computeB_temp2;
        Vec3 penetrationVec = ContactEquation_computeB_temp3;

        // Kreuzprodukte
        ri.cross(ni, rixn);
        rj.cross(ni, rjxn);

        // G = [ -ni  -rixn  ni  rjxn ]
        ni.negate(jElementA.spatial);
        rixn.negate(jElementA.rotational);
        jElementB.spatial.copy(ni);
        jElementB.rotational.copy(rjxn);

        // Penetrationsvektor
        penetrationVec.copy(bj.position);
        penetrationVec.add(rj, penetrationVec);
        penetrationVec.sub(bi.position, penetrationVec);
        penetrationVec.sub(ri, penetrationVec);

        double g = ni.dot(penetrationVec);

        double ePlusOne = restitution + 1;
        double GW = ePlusOne * bj.velocity.dot(ni)
                - ePlusOne * bi.velocity.dot(ni)
                + bj.angularVelocity.dot(rjxn)
                - bi.angularVelocity.dot(rixn);

        double GiMf = computeGiMf();

        return -g * a - GW * b - h * GiMf;
    }

    public double getImpactVelocityAlongNormal() {
        bi.position.add(ri, getImpact_xi);
        bj.position.add(rj, getImpact_xj);

        bi.getVelocityAtWorldPoint(getImpact_xi, getImpact_vi);
        bj.getVelocityAtWorldPoint(getImpact_xj, getImpact_vj);

        getImpact_vi.sub(getImpact_vj, getImpact_relVel);

        return ni.dot(getImpact_relVel);
    }
}