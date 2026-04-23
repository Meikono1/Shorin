package com.fuchsbau.shorin.Engine.Physics.Collision;

import com.fuchsbau.shorin.Engine.Physics.Math.Vec3;
import com.fuchsbau.shorin.Engine.Physics.World.World;


public class RayOptions {

    public Vec3 from = null;
    public Vec3 to = null;
    public RayMode mode = RayMode.CLOSEST;
    public RaycastResult result = null;
    public boolean skipBackfaces = false;
    public int collisionFilterMask = -1;
    public int collisionFilterGroup = -1;
    public boolean checkCollisionResponse = true;
    public World.RaycastCallback callback = null;
}