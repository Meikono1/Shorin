package com.fuchsbau.shorin.Engine.Physics.Constraints;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.Equation;

import java.util.ArrayList;
import java.util.List;

public class Constraint {

    private static int idCounter = 0;

    public List<Equation> equations = new ArrayList<>();
    public PhysicsBody bodyA;
    public PhysicsBody bodyB;
    public int id;
    public boolean collideConnected;

    public Constraint(PhysicsBody bodyA, PhysicsBody bodyB,
                      boolean collideConnected, boolean wakeUpBodies) {
        this.bodyA = bodyA;
        this.bodyB = bodyB;
        this.id = idCounter++;
        this.collideConnected = collideConnected;

        if (wakeUpBodies) {
            if (bodyA != null) bodyA.wakeUp();
            if (bodyB != null) bodyB.wakeUp();
        }
    }

    public Constraint(PhysicsBody bodyA, PhysicsBody bodyB) {
        this(bodyA, bodyB, true, true);
    }

    public void update() {
        throw new UnsupportedOperationException("update() nicht implementiert in: " + getClass().getSimpleName());
    }

    public void enable() {
        for (Equation eq : equations) eq.enabled = true;
    }

    public void disable() {
        for (Equation eq : equations) eq.enabled = false;
    }
}