package com.fuchsbau.shorin.Engine.Map;

public class Token {

    final int row, col;
    final String name;

    Token(int row, int col, String name) {
        this.row = row;
        this.col = col;
        this.name = name;
    }
}