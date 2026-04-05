package com.fuchsbau.shorin.Engine.RPG;

import com.fuchsbau.shorin.Engine.Map.Core.Tile;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

/**
 * GameClock
 * <p>
 * Verwendung:
 * GameClock clock = GameClock.getInstance();
 * clock.start();
 * clock.setSpeed(Speed.X5);
 * <p>
 * // Reise 12 Miles mit Speed 25ft über normales Terrain:
 * clock.travelTo("Whitebridge", 12.0, 25, TerrainType.NORMAL, () -> ankommen());
 */
public class GameClock {
    // Singleton
    private static GameClock instance;

    public static GameClock getInstance() {
        if (instance == null) instance = new GameClock();
        return instance;
    }

    // PF2e Konstanten

    public static final double SECONDS_PER_TURN = 6.0;
    public static final int TURNS_PER_MINUTE = 10;
    public static final int TURNS_PER_HOUR = 600;
    public static final int TURNS_PER_DAY = 14_400;

    /**
     * Berechnet Reisedauer in Turns.
     *
     * @param distanceMiles Entfernung in Meilen
     * @param speedFeet     Character Speed in Feet (z.B. 25)
     * @param terrain       Geländetyp
     * @return Reisedauer in Turns
     */
    public static long travelDurationTurns(double distanceMiles, int speedFeet, Tile terrain) {
        // Feet per Minute = Speed × 10 (z.B. Speed 25 → 250 ft/min)
        double feetPerMinute = speedFeet * 10.0 * terrain.getTerrainmultiplier();
        double minutes = (distanceMiles * 5280.0) / feetPerMinute;
        return Math.max(1, Math.round(minutes * TURNS_PER_MINUTE));
    }

    // Speed-Enum

    /**
     * Turns pro Real-Sekunde bei den verschiedenen Speeds.
     * X1  → 1 Turn/s    = 6 In-Game-Sekunden/s  (fast Echtzeit)
     * X5  → 5 Turns/s   = 30 In-Game-Sekunden/s (Exploration)
     * X10 → 10 Turns/s  = 1 In-Game-Minute/s    (Reisen)
     * X60 → 60 Turns/s  = 6 In-Game-Minuten/s   (Schnellreise)
     */
    public enum Speed {
        PAUSED(0.0, "⏸"), X1(1.0, "1×"), X2(2.0, "2×"), X5(5.0, "5×"), X10(10.0, "10×"), X60(60.0, "60×");

        public final double turnsPerSecond;
        public final String label;

        Speed(double turnsPerSecond, String label) {
            this.turnsPerSecond = turnsPerSecond;
            this.label = label;
        }
    }

    // State

    /**
     * Gesamte vergangene Turns seit Spielstart (Tag 1, 08:00)
     */
    private double totalTurns = turnsFromDayHourMin(1, 8, 0);

    private Speed currentSpeed = Speed.PAUSED;

    // JavaFX Properties für UI-Binding
    private final IntegerProperty dayProperty = new SimpleIntegerProperty();
    private final IntegerProperty hourProperty = new SimpleIntegerProperty();
    private final IntegerProperty minuteProperty = new SimpleIntegerProperty();
    private final IntegerProperty turnOfMinuteProperty = new SimpleIntegerProperty();
    private final StringProperty timeStringProperty = new SimpleStringProperty();
    private final ObjectProperty<Speed> speedProperty = new SimpleObjectProperty<>(Speed.PAUSED);

    // Travel
    private TravelJob activeTravelJob = null;

    private static class TravelJob {
        final String destination;
        final long durationTurns;
        final double startTurns;
        final Runnable onArrival;

        TravelJob(String destination, long durationTurns, double startTurns, Runnable onArrival) {
            this.destination = destination;
            this.durationTurns = durationTurns;
            this.startTurns = startTurns;
            this.onArrival = onArrival;
        }

        double progress(double currentTurns) {
            if (durationTurns <= 0) return 1.0;
            return Math.min(1.0, (currentTurns - startTurns) / durationTurns);
        }

        boolean isFinished(double currentTurns) {
            return currentTurns >= startTurns + durationTurns;
        }

        String remainingString(double currentTurns) {
            long remaining = Math.max(0, (long) ((startTurns + durationTurns) - currentTurns));
            return formatTurns(remaining);
        }
    }

    // Listeners

    public interface ClockListener {
        /**
         * Jede neue In-Game-Minute
         */
        void onMinuteTick(int day, int hour, int minute);
    }

    public interface TravelListener {
        void onTravelProgress(String destination, double progress, String remainingTime);

        void onTravelArrived(String destination);
    }

    private final List<ClockListener> clockListeners = new ArrayList<>();
    private final List<TravelListener> travelListeners = new ArrayList<>();

    // ── AnimationTimer ─────────────────────────────────────────────────────────
    private long lastNanoTime = -1;
    private int lastNotifiedMin = -1;

    private final AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (currentSpeed == Speed.PAUSED) {
                lastNanoTime = now;
                return;
            }
            if (lastNanoTime < 0) {
                lastNanoTime = now;
                return;
            }

            double deltaSeconds = (now - lastNanoTime) / 1_000_000_000.0;
            lastNanoTime = now;

            // Lag-Schutz: max 100ms pro Frame
            deltaSeconds = Math.min(deltaSeconds, 0.1);

            totalTurns += deltaSeconds * currentSpeed.turnsPerSecond;

            updateTimeProperties();
            checkTravelJob();
        }
    };

    // ── Konstruktor ────────────────────────────────────────────────────────────
    private GameClock() {
        updateTimeProperties();
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /**
     * Startet den AnimationTimer. Einmal beim Spielstart aufrufen.
     */
    public void start() {
        lastNanoTime = -1;
        timer.start();
    }

    /**
     * Stoppt den Timer (z.B. beim Verlassen des GameScreens).
     */
    public void stop() {
        timer.stop();
        lastNanoTime = -1;
    }

    public void setSpeed(Speed speed) {
        this.currentSpeed = speed;
        speedProperty.set(speed);
    }

    public Speed getSpeed() {
        return currentSpeed;
    }

    /**
     * Reise starten mit automatischerBerechnung.
     *
     * @param destination   Zielname
     * @param distanceMiles Entfernung in Meilen
     * @param speedFeet     Char Speed in Feet (z.B. 25)
     * @param terrain       Geländetyp
     * @param onArrival     Callback bei Ankunft (JavaFX-Thread)
     */
    public void travelTo(String destination, double distanceMiles, int speedFeet, Tile terrain, Runnable onArrival) {
        long turns = travelDurationTurns(distanceMiles, speedFeet, terrain);
        activeTravelJob = new TravelJob(destination, turns, totalTurns, onArrival);
        if (currentSpeed == Speed.PAUSED) setSpeed(Speed.X10);
    }

    /**
     * Reise mit manuell angegebener Dauer in Turns (für Events/Skripte).
     */
    public void travelToForTurns(String destination, long durationTurns, Runnable onArrival) {
        activeTravelJob = new TravelJob(destination, durationTurns, totalTurns, onArrival);
        if (currentSpeed == Speed.PAUSED) setSpeed(Speed.X10);
    }

    public void cancelTravel() {
        activeTravelJob = null;
    }

    public boolean isTraveling() {
        return activeTravelJob != null;
    }

    public String getTravelDestination() {
        return activeTravelJob != null ? activeTravelJob.destination : null;
    }

    public double getTravelProgress() {
        return activeTravelJob == null ? -1.0 : activeTravelJob.progress(totalTurns);
    }

    public String getTravelRemainingTime() {
        return activeTravelJob == null ? "" : activeTravelJob.remainingString(totalTurns);
    }

    public double getTotalTurns() {
        return totalTurns;
    }

    /**
     * Für Speichern/Laden
     */
    public void setTotalTurns(double turns) {
        this.totalTurns = turns;
        updateTimeProperties();
    }

    // Getters
    public int getDay() {
        return dayProperty.get();
    }

    public int getHour() {
        return hourProperty.get();
    }

    public int getMinute() {
        return minuteProperty.get();
    }

    public int getTurnOfMinute() {
        return turnOfMinuteProperty.get();
    }

    public DayPhase getDayPhase() {
        int h = getHour();
        if (h >= 5 && h < 8) return DayPhase.DAWN;
        if (h >= 8 && h < 12) return DayPhase.MORNING;
        if (h >= 12 && h < 17) return DayPhase.AFTERNOON;
        if (h >= 17 && h < 20) return DayPhase.DUSK;
        if (h >= 20 && h < 23) return DayPhase.EVENING;
        return DayPhase.NIGHT;
    }

    public enum DayPhase {DAWN, MORNING, AFTERNOON, DUSK, EVENING, NIGHT}

    // Properties
    public IntegerProperty dayProperty() {
        return dayProperty;
    }

    public IntegerProperty hourProperty() {
        return hourProperty;
    }

    public IntegerProperty minuteProperty() {
        return minuteProperty;
    }

    public StringProperty timeStringProperty() {
        return timeStringProperty;
    }

    public ObjectProperty<Speed> speedProperty() {
        return speedProperty;
    }

    // Listener
    public void addClockListener(ClockListener l) {
        clockListeners.add(l);
    }

    public void removeClockListener(ClockListener l) {
        clockListeners.remove(l);
    }

    public void addTravelListener(TravelListener l) {
        travelListeners.add(l);
    }

    public void removeTravelListener(TravelListener l) {
        travelListeners.remove(l);
    }

    // ── Statische Utilities ────────────────────────────────────────────────────

    /**
     * Turns → lesbarer String z.B. "3h 20min" oder "45min"
     */
    public static String formatTurns(long turns) {
        long totalSec = (long) (turns * SECONDS_PER_TURN);
        long hours = totalSec / 3600;
        long minutes = (totalSec % 3600) / 60;

        if (hours > 0 && minutes > 0) return hours + "h " + minutes + "min";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "min";
        return (totalSec) + "s";
    }

    private static double turnsFromDayHourMin(int day, int hour, int min) {
        return (double) day * TURNS_PER_DAY + (double) hour * TURNS_PER_HOUR + (double) min * TURNS_PER_MINUTE;
    }

    // ── Interne Update-Logik ───────────────────────────────────────────────────

    private void updateTimeProperties() {
        long total = (long) totalTurns;

        int day = (int) (total / TURNS_PER_DAY);
        int remainder = (int) (total % TURNS_PER_DAY);
        int hour = remainder / TURNS_PER_HOUR;
        remainder %= TURNS_PER_HOUR;
        int minute = remainder / TURNS_PER_MINUTE;
        int turnOfMin = remainder % TURNS_PER_MINUTE;

        dayProperty.set(day);
        hourProperty.set(hour);
        minuteProperty.set(minute);
        turnOfMinuteProperty.set(turnOfMin);
        timeStringProperty.set(String.format("Tag %d · %02d:%02d", day, hour, minute));

        if (minute != lastNotifiedMin) {
            lastNotifiedMin = minute;
            for (ClockListener l : clockListeners) {
                l.onMinuteTick(day, hour, minute);
            }
        }
    }

    private void checkTravelJob() {
        if (activeTravelJob == null) return;

        double progress = activeTravelJob.progress(totalTurns);
        String remaining = activeTravelJob.remainingString(totalTurns);

        for (TravelListener l : travelListeners) {
            l.onTravelProgress(activeTravelJob.destination, progress, remaining);
        }

        if (activeTravelJob.isFinished(totalTurns)) {
            String dest = activeTravelJob.destination;
            Runnable callback = activeTravelJob.onArrival;
            activeTravelJob = null;

            for (TravelListener l : travelListeners) {
                l.onTravelArrived(dest);
            }

            setSpeed(Speed.PAUSED);
            if (callback != null) callback.run();
        }
    }
}