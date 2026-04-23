package com.fuchsbau.shorin.Engine.Physics.Material;

public class ContactMaterial {

    private static int idCounter = 0;

    public int id;
    public Material[] materials;
    public double friction;
    public double restitution;
    public double contactEquationStiffness;
    public double contactEquationRelaxation;
    public double frictionEquationStiffness;
    public double frictionEquationRelaxation;

    public ContactMaterial(Material m1, Material m2,
                           double friction, double restitution) {
        this(m1, m2, friction, restitution, 1e7, 3, 1e7, 3);
    }

    public ContactMaterial(Material m1, Material m2,
                           double friction, double restitution,
                           double contactEquationStiffness, double contactEquationRelaxation,
                           double frictionEquationStiffness, double frictionEquationRelaxation) {
        this.id = idCounter++;
        this.materials = new Material[]{m1, m2};
        this.friction = friction;
        this.restitution = restitution;
        this.contactEquationStiffness = contactEquationStiffness;
        this.contactEquationRelaxation = contactEquationRelaxation;
        this.frictionEquationStiffness = frictionEquationStiffness;
        this.frictionEquationRelaxation = frictionEquationRelaxation;
    }
}