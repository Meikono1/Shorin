package com.fuchsbau.shorin.Engine.Physics.Solver.Equation;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;

public class FrictionEquation extends Equation {

    public Vec3 ri;
    public Vec3 rj;
    public Vec3 t; // Tangente

    private static final Vec3 computeB_temp1 = new Vec3();
    private static final Vec3 computeB_temp2 = new Vec3();

    public FrictionEquation(PhysicsBody bodyA, PhysicsBody bodyB, double slipForce) {
        super(bodyA, bodyB, -slipForce, slipForce);
        this.ri = new Vec3();
        this.rj = new Vec3();
        this.t  = new Vec3();
    }

    @Override
    public double computeB(double h) {
        Vec3 rixt = computeB_temp1;
        Vec3 rjxt = computeB_temp2;

        // Kreuzprodukte
        ri.cross(t, rixt);
        rj.cross(t, rjxt);

        // G = [-t -rixt t rjxt]
        t.negate(jElementA.spatial);
        rixt.negate(jElementA.rotational);
        jElementB.spatial.copy(t);
        jElementB.rotational.copy(rjxt);

        double GW   = computeGW();
        double GiMf = computeGiMf();

        return -GW * b - h * GiMf;
    }
}