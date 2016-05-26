package com.mlesniak.raytracer.scene;

import com.mlesniak.raytracer.math.Vector3D;

import java.util.Optional;

/**
 * Sphere object with center and radius.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Sphere extends SceneObject {
    public Vector3D center;
    public double radius;

    public Sphere() {
        center = new Vector3D(0, 0, 0);
    }

    @Override
    public Optional<Vector3D> intersect(Vector3D camera, Vector3D ray) {
        // Use code for book, optimize later.
        // double a = 1;
        double b =
                2 * (ray.x * (camera.x - center.x) + ray.y * (camera.y - center.y) + ray.z * (camera.z - center.z));
        double c = (camera.x - center.x) * (camera.x - center.x) + (camera.y - center.y) * (camera.y - center.y) +
                (camera.z - center.z) * (camera.z - center.z) - (radius * radius);

        double disc = b * b - 4 * c;
        if (disc < 0) {
            return Optional.empty();
        }

        double t0 = (-b - Math.sqrt(disc)) / 2;
        double t1 = (-b + Math.sqrt(disc)) / 2;

        double t = Math.min(t0, t1);
        // Intersection point is...
        Vector3D intersection = new Vector3D(camera.x + ray.x * t, camera.z + ray.z * t, camera.z + ray.z * t);

        return Optional.of(intersection);
    }

    public Vector3D getCenter() {
        return center;
    }

    public void setCenter(Vector3D center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
