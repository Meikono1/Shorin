package com.fuchsbau.shorin.Engine.Physics.Solver.Equation;

import com.fuchsbau.shorin.Engine.Physics.Math.JacobianElement;
import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.CollisionShape;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;

public abstract class Equation {
    private static final Vec3 iMfi = new Vec3();
    private static final Vec3 iMfj = new Vec3();
    private static final Vec3 invIi_vmult_taui = new Vec3();
    private static final Vec3 invIj_vmult_tauj = new Vec3();
    private static final Vec3 tmp = new Vec3();
    private static final Vec3 addToWlambda_temp = new Vec3();

    private double id;
    private static int idCounter = 0;
    public double minForce;
    public double maxForce;

    public double a;
    public double b;
    public double eps;

    public double multiplier;

    public PhysicsBody bi;
    public PhysicsBody bj;
    public CollisionShape si;
    public CollisionShape sj;

    public JacobianElement jElementA;
    public JacobianElement jElementB;

    public boolean enabled;

    public Equation(PhysicsBody bi, PhysicsBody bj, double minForce, double maxForce) {
        this.id = Equation.idCounter++;
        this.bi = bi;
        this.bj = bj;
        this.minForce = minForce == 0 ? minForce : -1e6;
        this.maxForce = maxForce == 0 ? maxForce : 1e6;
        this.a = 0.0;
        this.b = 0.0;
        this.eps = 0.0;
        this.jElementA = new JacobianElement();
        this.jElementB = new JacobianElement();
        this.enabled = true;
        this.multiplier = 0;

        setStablePhysicsKinematics(1e7, 4, 1f / 120);
    }


    public void setStablePhysicsKinematics(double stiffness, double relaxation, float timeStep) {
        this.a = 4.0 / (timeStep * (1 + 4 * relaxation));
        this.b = (4.0 * relaxation) / (1 + 4 * relaxation);
        this.eps = 4.0 / (timeStep * timeStep * stiffness * (1 + 4 * relaxation));
    }

    public abstract double computeB(double h);

    public double computeB(double a, double b, double h) {
        return (-computeGq()) * a - computeGW() * b - computeGiMf() * h;
    }

    public double computeGq() {
        return jElementA.spatial.dot(bi.getPosition()) + jElementB.spatial.dot(bj.getPosition());
    }

    public double computeGW() {
        return jElementA.multiplyVectors(bi.getVelocity(), bi.getAngularVelocity()) + jElementB.multiplyVectors(bj.getVelocity(), bj.getAngularVelocity());
    }

    public double computeGWlambda() {
        Vec3 vi = bi.vLambda;
        Vec3 vj = bj.vLambda;
        Vec3 wi = bi.wLambda;
        Vec3 wj = bj.wLambda;
        return jElementA.multiplyVectors(vi, wi) + jElementB.multiplyVectors(vj, wj);
    }

    public double computeGiMf() {
        bi.force.scale(bi.invMassSolve, iMfi);
        bj.force.scale(bj.invMassSolve, iMfj);

        bi.invInertiaWorldSolve.vmult(bi.torque, invIi_vmult_taui);
        bj.invInertiaWorldSolve.vmult(bj.torque, invIj_vmult_tauj);

        return jElementA.multiplyVectors(iMfi, invIi_vmult_taui)
                + jElementB.multiplyVectors(iMfj, invIj_vmult_tauj);
    }

    public double computeGiMGt() {
        double result = bi.invMassSolve + bj.invMassSolve;

        bi.invInertiaWorldSolve.vmult(jElementA.rotational, tmp);
        result += tmp.dot(jElementA.rotational);

        bj.invInertiaWorldSolve.vmult(jElementB.rotational, tmp);
        result += tmp.dot(jElementB.rotational);

        return result;
    }

    public void addToWlambda(double deltalambda) {
        // Lineare Geschwindigkeit addieren
        bi.vLambda.addScaledVector(bi.invMassSolve * deltalambda, jElementA.spatial, bi.vLambda);
        bj.vLambda.addScaledVector(bj.invMassSolve * deltalambda, jElementB.spatial, bj.vLambda);

        // Winkelgeschwindigkeit addieren
        bi.invInertiaWorldSolve.vmult(jElementA.rotational, addToWlambda_temp);
        bi.wLambda.addScaledVector(deltalambda, addToWlambda_temp, bi.wLambda);

        bj.invInertiaWorldSolve.vmult(jElementB.rotational, addToWlambda_temp);
        bj.wLambda.addScaledVector(deltalambda, addToWlambda_temp, bj.wLambda);
    }

    public void setSpookParams(double stiffness, double relaxation, double timeStep) {
        double d = relaxation;
        double k = stiffness;
        double h = timeStep;
        this.a   = 4.0 / (h * (1 + 4 * d));
        this.b   = (4.0 * d) / (1 + 4 * d);
        this.eps = 4.0 / (h * h * k * (1 + 4 * d));
    }

    public double computeC() {
        return computeGiMGt() + eps;
    }
}