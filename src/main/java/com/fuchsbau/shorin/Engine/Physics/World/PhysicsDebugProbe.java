package com.fuchsbau.shorin.Engine.Physics.World;

import com.fuchsbau.shorin.Engine.Physics.Shape.BodyType;
import com.fuchsbau.shorin.Engine.Physics.Shape.PhysicsBody;
import com.fuchsbau.shorin.Logger.FileLogger;

import java.util.List;
import java.util.logging.Logger;

/**
 * Temporärer Debug-Probe für die Physics-Engine.
 * Wird von World.internalStep an 4 Zeitpunkten "gepinged" und
 * misst Energie, Impulse, Penetrationen.
 * <p>
 * Keine Allokation im Hot-Path – alle Arbeitsspeicher sind Felder.
 * Performance-Kosten wenn enabled=false: eine Nullcheck + return.
 */
public final class PhysicsDebugProbe {

    private static final Logger log = FileLogger.getLogger();

    // --- Phase-Marker, damit wir im Output nicht durcheinander kommen ---
    public static final int PHASE_START = 0; // vor Gravity
    public static final int PHASE_AFTER_GRAV = 1; // nach Gravity-Force-Add
    public static final int PHASE_AFTER_SOLVE = 2; // nach solver.solve()
    public static final int PHASE_AFTER_DAMP = 3; // nach Damping
    public static final int PHASE_AFTER_INTEG = 4; // nach integrate()

    private static final String[] PHASE_NAME = {
            "START", "GRAV", "SOLVE", "DAMP", "INTEG"
    };

    public boolean enabled = true;

    // Was geloggt wird (alles toggelbar, weil Konsole sonst explodiert)
    public boolean logEveryStep = false;     // pro Step printen? Meist zu viel
    public int logEveryN = 20;        // sonst jeden N-ten Step
    public boolean logEnergyDelta = true;
    public boolean logContacts = true;
    public boolean logImpulses = true;

    // Messwerte pro Phase – Index = PHASE_*
    private final double[] phaseKE = new double[5];
    private final double[] phasePE = new double[5]; // aus gravity
    private final double[] phaseKER = new double[5]; // rotational KE

    // Solver-Messungen
    private double maxJn = 0.0;  // größter Normal-Impulse im Step
    private double maxPen = 0.0;  // größte Penetrationstiefe
    private int contactsN = 0;
    private int frictionN = 0;
    private int broadphasePairs = 0;
    public double deepestPen = 0.0;
    public double deepestNx  = 0.0;
    public double deepestNy  = 0.0;
    public double deepestNz  = 0.0;

    // Zähler
    private long stepIndex = 0;

    /**
     * Wird von World.internalStep() nach jeder Phase aufgerufen.
     * Misst KE/PE der dynamischen Bodies und legt sie in phaseKE[phase] ab.
     */
    public void snapshot(int phase, List<PhysicsBody> bodies, double gravityZ) {
        if (!enabled) return;

        double keLin = 0.0;
        double keRot = 0.0;
        double pe = 0.0;

        for (PhysicsBody b : bodies) {
            if (b.type != BodyType.DYNAMIC) continue;

            double vx = b.velocity.x, vy = b.velocity.y, vz = b.velocity.z;
            keLin += 0.5 * b.mass * (vx * vx + vy * vy + vz * vz);

            // Näherung: rotational KE mit isotropem Inertia
            // (reicht als Indikator – wir wollen nur sehen OB es steigt)
            double wx = b.angularVelocity.x, wy = b.angularVelocity.y, wz = b.angularVelocity.z;
            double inertiaAvg = (b.mass > 0) ? 0.4 * b.mass * b.boundingRadius * b.boundingRadius : 0.0;
            keRot += 0.5 * inertiaAvg * (wx * wx + wy * wy + wz * wz);

            // potentielle Energie: m * |g| * z (damit Summe KE+PE konstant sein sollte)
            pe += b.mass * (-gravityZ) * b.position.z;
        }

        phaseKE[phase] = keLin;
        phaseKER[phase] = keRot;
        phasePE[phase] = pe;
    }

    /**
     * Vom Solver: der tatsächliche Normal-Impuls jn pro Equation.
     * Wir halten nur das Maximum fest.
     */
    public void recordImpulse(double jn) {
        if (!enabled) return;
        double abs = Math.abs(jn);
        if (abs > maxJn) maxJn = abs;
    }

    public void recordContactCount(int contacts, int friction) {
        if (!enabled) return;
        this.contactsN = contacts;
        this.frictionN = friction;
    }

    public void recordBroadphasePairs(int n) {
        if (!enabled) return;
        this.broadphasePairs = n;
    }

    public void recordPenetration(double depth) {
        if (!enabled) return;
        if (depth > maxPen) maxPen = depth;
    }

    /**
     * Am Ende von internalStep aufrufen. Printed (wenn dran) und resettet
     * die per-Step-Messwerte.
     */
    public void endStep() {
        if (!enabled) {
            return;
        }
        StringBuilder sb = new StringBuilder(256);
        sb.append("[PROBE step=").append(stepIndex).append("] ");

        stepIndex++;
        boolean shouldLog = logEveryStep || (stepIndex % logEveryN == 0);

        if (shouldLog) {

            if (logEnergyDelta) {
                // Wir interessieren uns vor allem für ΔKE(solve) und ΔKE(integ)
                // – kein Energie-Aufbau durch Solver erlaubt, PE→KE durch integrate ok.
                double dKEsolve = phaseKE[PHASE_AFTER_SOLVE] - phaseKE[PHASE_AFTER_GRAV];
                double dKEdamp = phaseKE[PHASE_AFTER_DAMP] - phaseKE[PHASE_AFTER_SOLVE];
                double dKEinteg = phaseKE[PHASE_AFTER_INTEG] - phaseKE[PHASE_AFTER_DAMP];
                double totalE = phaseKE[PHASE_AFTER_INTEG] + phaseKER[PHASE_AFTER_INTEG] + phasePE[PHASE_AFTER_INTEG];

                sb.append(String.format(
                        "KE=%.1f KEr=%.1f PE=%.1f E=%.1f | ΔKE(solve)=%+.1f ΔKE(damp)=%+.1f ΔKE(int)=%+.1f",
                        phaseKE[PHASE_AFTER_INTEG], phaseKER[PHASE_AFTER_INTEG], phasePE[PHASE_AFTER_INTEG],
                        totalE, dKEsolve, dKEdamp, dKEinteg
                ));
            }

            if (logContacts) {
                sb.append(" | pairs=").append(broadphasePairs)
                        .append(" ct=").append(contactsN)
                        .append(" fr=").append(frictionN);
            }

            if (logImpulses) {
                sb.append(String.format(" | maxJn=%.1f maxPen=%.3f", maxJn, maxPen));
            }

            sb.append(String.format(" | deepN=(%.2f,%.2f,%.2f)@pen=%.2f",
                    deepestNx, deepestNy, deepestNz, deepestPen));

            log.info(sb.toString());
        }

        // Reset der per-Step-Werte
        maxJn = 0.0;
        maxPen = 0.0;
        deepestPen = 0.0;
        deepestNx = 0.0;
        deepestNy = 0.0;
        deepestNz = 0.0;
    }

    /**
     * Von ContactEquation.computeB: die Normal- und Penetrations-Info.
     * Wir tracken den Kontakt mit der schrägsten Normale, wenn er signifikant
     * ist (Penetration > 0.5). Gesunder Boden/Wand-Kontakt: |n.z|≈1 oder ≈0.
     */
    public void recordContactNormal(double nx, double ny, double nz, double pen) {
        if (!enabled) return;
        if (pen > deepestPen) {
            deepestPen = pen;
            deepestNx  = nx;
            deepestNy  = ny;
            deepestNz  = nz;
        }
    }

    // --- Lesbare Getter fürs HUD ---
    public double getLastKE() {
        return phaseKE[PHASE_AFTER_INTEG];
    }

    public double getLastKER() {
        return phaseKER[PHASE_AFTER_INTEG];
    }

    public double getLastPE() {
        return phasePE[PHASE_AFTER_INTEG];
    }

    public double getLastDeltaKESolve() {
        return phaseKE[PHASE_AFTER_SOLVE] - phaseKE[PHASE_AFTER_GRAV];
    }

    public double getLastDeltaKEInteg() {
        return phaseKE[PHASE_AFTER_INTEG] - phaseKE[PHASE_AFTER_DAMP];
    }

    public double getLastMaxJn() {
        return maxJn;
    }

    public double getLastMaxPen() {
        return maxPen;
    }

    public int getLastContactCount() {
        return contactsN;
    }

    public int getLastFrictionCount() {
        return frictionN;
    }

    public int getLastBroadphasePairs() {
        return broadphasePairs;
    }

    public long getStepIndex() {
        return stepIndex;
    }
}