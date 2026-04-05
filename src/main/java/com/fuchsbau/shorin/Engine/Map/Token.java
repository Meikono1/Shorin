package com.fuchsbau.shorin.Engine.Map;

public class Token {

    public final int row;
    public final int col;
    public final String name;

    public Token(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }
}