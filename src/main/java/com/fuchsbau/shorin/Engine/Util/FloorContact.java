package com.fuchsbau.shorin.Engine.Util;

public record FloorContact(
        boolean colliding,
        int contactCount,
        double penetration
) {
}
