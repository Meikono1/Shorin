package com.fuchsbau.shorin.Engine.System;


public class Trait {
    public String name = "";
    public String description = "";

    public Trait() {
    }

    public Trait(String name, String description) {
        this.name = name;
        this.description = description;
    }


    // Jackson dependent
    public String getName() {
        return name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    @Override
    public String toString() {
        return name;
    }
}