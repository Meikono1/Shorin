package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.System.NpcBuild;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Token {

    // --- Presentation ---
    public final String name;
    public int row;
    public int col;

    // --- Information ---
    public int initiative = 0;
    public NpcBuild npcBuild;
    public int maxActions = 3;
    public final BooleanProperty reactionUsed = new SimpleBooleanProperty(false);

    public boolean isPlayer = false;
    public boolean isActive = false;

    public Token(int row, int col, NpcBuild npcBuild) {
        this.row = row;
        this.col = col;
        this.name = npcBuild.name;
        this.npcBuild = npcBuild;
    }

    @Deprecated
    public Token(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }
}