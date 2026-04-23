package com.fuchsbau.shorin.Engine.Physics.Solver;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.Equation;

import java.util.ArrayList;
import java.util.List;

public class Solver {

    public List<Equation> equations = new ArrayList<>();


    public int solve(double dt, List<PhysicsBody> bodies) {
        throw new UnsupportedOperationException("solve() nicht implementiert für: " + getClass().getSimpleName());
    }

    public void addEquation(Equation eq) {
        if (eq.enabled && !eq.bi.isTrigger && !eq.bj.isTrigger) {
            equations.add(eq);
        }
    }

    public void removeEquation(Equation eq) {
        equations.remove(eq);
    }

    public void removeAllEquations() {
        equations.clear();
    }
}