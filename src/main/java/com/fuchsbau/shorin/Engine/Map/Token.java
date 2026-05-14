package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.System.NonPlayerCharacter;
import com.fuchsbau.shorin.Engine.System.StatBlock;
import com.fuchsbau.shorin.Engine.System.Character.PlayerCharacter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.UUID;

public class Token {
    // --- Presentation ---
    public final String id = UUID.randomUUID().toString();
    public final String name;
    public int row;
    public int col;

    // --- Information ---
    public int initiative = 0;
    public StatBlock Statblock;
    public int maxActions = 3;
    public final BooleanProperty reactionUsed = new SimpleBooleanProperty(false);

    public int currentHp = -1;

    public boolean isPlayer = false;
    public boolean isActive = false;

    public Token(int row, int col, NonPlayerCharacter npcBuild) {
        this.row = row;
        this.col = col;
        this.name = npcBuild.name;
        this.Statblock = npcBuild;
    }
    public Token(int row, int col, PlayerCharacter character) {
        this.row = row;
        this.col = col;
        this.name = character.name;
        this.isPlayer = true;
    }

    public int getMaxHp() {
        return Statblock != null ? Statblock.hp : 1;
    }

    public int getCurrentHp() {
        if (currentHp < 0) currentHp = getMaxHp();
        return currentHp;
    }

    public double getHpPercent() {
        int max = getMaxHp();
        if (max <= 0) return 1.0;
        return Math.max(0.0, Math.min(1.0, (double) getCurrentHp() / max));
    }


    //@TODO erweitern
    public boolean isAlly() {
        return !isPlayer && Statblock != null && Statblock.traits.contains("ally");
    }

    public boolean isNeutral() {
        return !isPlayer && Statblock != null && Statblock.traits.contains("neutral");
    }

}