package com.fuchsbau.shorin.Engine.Map;

import com.fuchsbau.shorin.Engine.System.NpcBuild;

public class Token {

    public int row;
    public int col;
    public final String name;
    public NpcBuild npcBuild;

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