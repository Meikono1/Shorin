package com.fuchsbau.shorin.Engine.System;

public enum Expertise {
    U, T, E, M, L;


    public String fullName() {
        return switch (this) {
            case U -> "Untrained";
            case T -> "Trained";
            case E -> "Expert";
            case M -> "Master";
            case L -> "Legendary";
        };
    }

    @Override
    public String toString() {
        return fullName();
    }
}
