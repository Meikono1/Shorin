package com.fuchsbau.shorin.Engine.Physics.Constraints;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.ContactEquation;

public class PointToPointConstraint extends Constraint {

    public Vec3            pivotA;
    public Vec3            pivotB;
    public ContactEquation equationX;
    public ContactEquation equationY;
    public ContactEquation equationZ;

    public PointToPointConstraint(PhysicsBody bodyA, Vec3 pivotA,
                                  PhysicsBody bodyB, Vec3 pivotB) {
        this(bodyA, pivotA, bodyB, pivotB, 1e6);
    }

    public PointToPointConstraint(PhysicsBody bodyA, Vec3 pivotA,
                                  PhysicsBody bodyB, Vec3 pivotB,
                                  double maxForce) {
        super(bodyA, bodyB, true, true);

        this.pivotA = pivotA != null ? pivotA.clone() : new Vec3();
        this.pivotB = pivotB != null ? pivotB.clone() : new Vec3();

        this.equationX = new ContactEquation(bodyA, bodyB);
        this.equationY = new ContactEquation(bodyA, bodyB);
        this.equationZ = new ContactEquation(bodyA, bodyB);

        equations.add(equationX);
        equations.add(equationY);
        equations.add(equationZ);

        // Bidirektional
        equationX.minForce = equationY.minForce = equationZ.minForce = -maxForce;
        equationX.maxForce = equationY.maxForce = equationZ.maxForce =  maxForce;

        equationX.ni.set(1, 0, 0);
        equationY.ni.set(0, 1, 0);
        equationZ.ni.set(0, 0, 1);
    }

    @Override
    public void update() {
        // Pivots in Weltkoordinaten rotieren
        bodyA.quaternion.vmult(pivotA, equationX.ri);
        bodyB.quaternion.vmult(pivotB, equationX.rj);

        equationY.ri.copy(equationX.ri);
        equationY.rj.copy(equationX.rj);
        equationZ.ri.copy(equationX.ri);
        equationZ.rj.copy(equationX.rj);
    }
}