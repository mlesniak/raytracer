package com.mlesniak.raytracer.scene;

import com.mlesniak.raytracer.math.Vector3D;

import java.util.Optional;

/**
 * General scene object with common properties.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public abstract class SceneObject {
    private int color;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Check for intersection.
     *
     * @param origin origin of the ray
     * @param ray    ray to check against intersection.
     * @return true if intersects
     */
    public abstract Optional<Vector3D> intersect(Vector3D origin, Vector3D ray);
}
