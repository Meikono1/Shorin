package com.fuchsbau.shorin.Engine.Physics.Shape;

public enum SleepState {
    ACTIVE, DROWSY, SLEEPING;


    public String nextEvent(SleepState state) {
        switch (state) {
            case ACTIVE -> {
                return "wakeup";
            }
            case DROWSY -> {
                return "sleepy";
            }
            case SLEEPING -> {
                return "sleep";
            }
        }
        return "";
    }
}
