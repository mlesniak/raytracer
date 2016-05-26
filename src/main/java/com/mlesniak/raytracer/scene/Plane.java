package com.mlesniak.raytracer.scene;

import com.mlesniak.raytracer.math.Vector3D;

import java.util.Optional;

/**
 * Sphere object with center and radius.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Plane extends SceneObject {
    public Vector3D point;
    public Vector3D normal;

    public Plane() {
        point = new Vector3D(0, 0, 0);
        normal = new Vector3D(0, 0, 0);
    }

    @Override
    public Optional<Vector3D> intersect(Vector3D camera, Vector3D ray) {
        double vd = ray.dot(normal);
        if (vd == 0) {
            return Optional.empty();
        }

        double v0 = normal.dot(point.minus(camera));
        double t = v0 / vd;

        if (t < 0) {
            // Plane behind eye, ignore it.
            return Optional.empty();
        }

        return Optional.of(camera.plus(ray.scale(t)));
    }

    public Vector3D getPoint() {
        return point;
    }

    public void setPoint(Vector3D point) {
        this.point = point;
    }

    public Vector3D getNormal() {
        return normal;
    }

    public void setNormal(Vector3D normal) {
        this.normal = normal;
    }
}
