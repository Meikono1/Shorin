package com.fuchsbau.shorin.Engine.Encounter;

import com.fuchsbau.shorin.Engine.Map.Token;
import com.fuchsbau.shorin.Logger.FileLogger;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.logging.Logger;

public class EncounterState {
    private final Logger logger = FileLogger.getLogger();

    // Runde
    public final IntegerProperty round = new SimpleIntegerProperty(1);

    // Initiative
    public final ObservableList<Token> initiative =
            FXCollections.observableArrayList();
    public final ObjectProperty<Token> activeToken =
            new SimpleObjectProperty<>();
    public final IntegerProperty currentInitiative = new SimpleIntegerProperty(0);

    // Action-Punkte des aktiven Tokens
    public final IntegerProperty actionsUsed = new SimpleIntegerProperty(0);

    public void nextTurn() {
        if (initiative.isEmpty()) return;

        Token active = activeToken.get();
        int idx = active != null ? initiative.indexOf(active) : -1;
        actionsUsed.set(0);

        if (idx < 0 || idx + 1 >= initiative.size()) {
            // Letzter in der Runde — Initiative fällt auf 0 → neue Runde
            currentInitiative.set(0);
            round.set(round.get() + 1);
            activeToken.set(initiative.getFirst());
            currentInitiative.set(initiative.getFirst().initiative);
            logger.fine("Neue Runde: " + round.get());
        } else {
            Token next = initiative.get(idx + 1);
            activeToken.set(next);
            currentInitiative.set(next.initiative);
        }

        activeToken.get().reactionUsed.set(false);
        logger.fine("nextTurn → " + activeToken.get().name
                + " | Initiative: " + currentInitiative.get()
                + " | Runde: " + round.get());
    }

    public void delay(Token active, Token target) {
        if (active == null || active == target) return;

        nextTurn();

        int from = initiative.indexOf(active);
        int to = initiative.indexOf(target);
        if (from < 0 || to < 0) return;

        // Token raus und hinter target einfügen
        initiative.remove(from);
        int insertAt = initiative.indexOf(target) + 1; // nach dem Ziel
        insertAt = Math.min(insertAt, initiative.size());
        initiative.add(insertAt, active);

        logger.info("Delay: " + active.name + " → Position " + insertAt
                + " (hinter " + target.name + ")");
    }

    public boolean canAct(int cost) {
        Token active = activeToken.get();
        if (active == null) return false;
        return actionsUsed.get() + cost <= active.maxActions;
    }

    public void useAction(int cost) {
        actionsUsed.set(actionsUsed.get() + cost);
    }
}