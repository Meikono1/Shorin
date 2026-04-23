package com.fuchsbau.shorin.Engine.Physics.Math;

public class JacobianElement {
    // DONE
    public Vec3 spatial;
    public Vec3 rotational;

    public JacobianElement() {
        this.spatial = new Vec3();
        this.rotational = new Vec3();
    }

    /**
     * Multiply with another JacobianElement
     */
    public double absMultiplyElement(JacobianElement element) {
        return element.spatial.dot(this.spatial) + element.rotational.dot(this.rotational);
    }

    /**
     * Multiply with two vectors
     */
    public double multiplyVectors(Vec3 spatial, Vec3 rotational) {
        return spatial.dot(this.spatial) + rotational.dot(this.rotational);
    }
}
