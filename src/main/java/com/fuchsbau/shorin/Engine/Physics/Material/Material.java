package com.fuchsbau.shorin.Engine.Physics.Material;

public class Material {

    public String name;
    public int id;
    public double friction;
    public double restitution;

    private static int idCounter = 0;

    public Material() {
        this.name = "";
        this.id = idCounter++;
        this.friction = -1;
        this.restitution = -1;
    }

    public Material(String name) {
        this.name = name;
        this.id = idCounter++;
        this.friction = -1;
        this.restitution = -1;
    }

    public Material(double friction, double restitution) {
        this.name = "";
        this.id = idCounter++;
        this.friction = friction;
        this.restitution = restitution;
    }
}