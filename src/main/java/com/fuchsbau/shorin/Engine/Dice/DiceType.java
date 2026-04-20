package com.fuchsbau.shorin.Engine.Dice;

public enum DiceType {
    D2(2),
    D4(4),
    D6(6),
    D8(8),
    D12(12),
    D20(20);

    private final int sides;

    DiceType(int sides) {
        this.sides = sides;
    }

    public int getSides() {
        return sides;
    }
}