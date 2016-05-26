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
     * @param camera origin of the ray
     * @param ray    ray to check against intersection.
     * @return true if intersects
     */
    public abstract Optional<Vector3D> intersect(Vector3D camera, Vector3D ray);

    /**
     * Compute the normal for the given intersection point.
     *
     * @param point intersection point.
     * @return normal of this point.
     */
    public abstract Vector3D normal(Vector3D point);
}
