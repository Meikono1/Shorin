package com.fuchsbau.shorin.Engine.Physics.Solver;

import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Engine.Physics.Solver.Equation.Equation;
import com.fuchsbau.shorin.Engine.Physics.World.PhysicsDebugProbe;

import java.util.List;

public class GaussSeidelSolver extends Solver {

    // Anzahl Solver-Iterationen
    public int iterations = 10;

    // Konvergenz-Schwellwert
    public double tolerance = 1e-7;// --- Debug ---

    public PhysicsDebugProbe debugProbe = new PhysicsDebugProbe();

    public GaussSeidelSolver() {
        super();
    }

    @Override
    public int solve(double dt, List<PhysicsBody> bodies) {
        int iter = 0;
        int Neq = equations.size();
        int Nbodies = bodies.size();
        double tolSquared = tolerance * tolerance;

        if (Neq == 0) return 0;

        // updateSolveMassProperties für alle Bodies
        for (PhysicsBody b : bodies) {
            b.updateSolveMassProperties();
        }

        // Bs und invCs einmal vorberechnen
        double[] lambda = new double[Neq];
        double[] Bs = new double[Neq];
        double[] invCs = new double[Neq];

        for (int i = 0; i < Neq; i++) {
            lambda[i] = 0.0;
            Bs[i] = equations.get(i).computeB(dt);
            invCs[i] = 1.0 / equations.get(i).computeC();
        }

        // vlambda / wlambda zurücksetzen
        for (PhysicsBody b : bodies) {
            b.vLambda.set(0, 0, 0);
            b.wLambda.set(0, 0, 0);
        }

        // Gauss-Seidel Iterationen
        for (iter = 0; iter < iterations; iter++) {
            double deltalambdaTot = 0.0;

            for (int j = 0; j < Neq; j++) {
                Equation c = equations.get(j);

                double B = Bs[j];
                double invC = invCs[j];
                double lambdaj = lambda[j];
                double GWlambda = c.computeGWlambda();
                double deltalambda = invC * (B - GWlambda - c.eps * lambdaj);

                if (lambdaj + deltalambda < c.minForce) {
                    deltalambda = c.minForce - lambdaj;
                } else if (lambdaj + deltalambda > c.maxForce) {
                    deltalambda = c.maxForce - lambdaj;
                }

                lambda[j] += deltalambda;
                deltalambdaTot += Math.abs(deltalambda);

                c.addToWlambda(deltalambda);

                // [PROBE] größten Normal-Impuls pro Step festhalten
                if (debugProbe != null) debugProbe.recordImpulse(lambda[j]);
            }

            if (deltalambdaTot * deltalambdaTot < tolSquared) {
                break;
            }
        }

        // vlambda * linearFactor auf velocity addieren
        // wlambda * angularFactor auf angularVelocity addieren
        for (PhysicsBody b : bodies) {
            b.vLambda.vmul(b.linearFactor, b.vLambda);
            b.velocity.add(b.vLambda, b.velocity);

            b.wLambda.vmul(b.angularFactor, b.wLambda);
            b.angularVelocity.add(b.wLambda, b.angularVelocity);
        }

        // multiplier setzen – rückwärts wie im Original
        double invDt = 1.0 / dt;
        for (int l = Neq - 1; l >= 0; l--) {
            equations.get(l).multiplier = lambda[l] * invDt;
        }

        return iter;
    }
}