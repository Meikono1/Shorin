package com.fuchsbau.shorin.Engine.Encounter;

import com.fuchsbau.shorin.Engine.Map.Token;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EncounterState {

    // Runde
    public final IntegerProperty round = new SimpleIntegerProperty(1);

    // Initiative
    public final ObservableList<Token> initiative =
            FXCollections.observableArrayList();
    public final ObjectProperty<Token> activeToken =
            new SimpleObjectProperty<>();

    // Action-Punkte des aktiven Tokens
    public final IntegerProperty actionsUsed = new SimpleIntegerProperty(0);

    // Methoden
    public void nextTurn() {
        int idx = initiative.indexOf(activeToken.get());
        actionsUsed.set(0);

        if (idx < 0 || initiative.isEmpty()) return;

        if (idx + 1 >= initiative.size()) {
            round.set(round.get() + 1);
            activeToken.set(initiative.getFirst());
        } else {
            activeToken.set(initiative.get(idx + 1));
        }
        activeToken.get().reactionUsed.set(false);
    }

    public boolean canAct(int actionCost) {
        Token active = activeToken.get();
        if (active == null) return false;
        return actionsUsed.get() + actionCost <= active.maxActions;
    }

    public void useAction(int cost) {
        actionsUsed.set(actionsUsed.get() + cost);
    }
}