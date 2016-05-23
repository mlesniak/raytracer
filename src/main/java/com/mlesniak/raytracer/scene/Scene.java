package com.mlesniak.raytracer.scene;

import com.mlesniak.raytracer.Vector3D;

import java.util.List;
import java.util.Optional;

/**
 * Describe a full scene.
 * <p>
 * We use YAML serialization to store a scene as a readable and editable file.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Scene {
    private int width;
    private int height;

    private String filename;
    private Vector3D camera;

    private View view;

    private List<SceneObject> objects;

    /**
     * Viewport description.
     */
    public static class View {
        public Vector3D lowerLeft;
        public Vector3D upperRight;

        public Vector3D getLowerLeft() {
            return lowerLeft;
        }

        public void setLowerLeft(Vector3D lowerLeft) {
            this.lowerLeft = lowerLeft;
        }

        public Vector3D getUpperRight() {
            return upperRight;
        }

        public void setUpperRight(Vector3D upperRight) {
            this.upperRight = upperRight;
        }

        @Override
        public String toString() {
            return "View{" +
                    "lowerLeft=" + lowerLeft +
                    ", upperRight=" + upperRight +
                    '}';
        }
    }

    /**
     * Sphere description.
     */
    public static class Sphere extends SceneObject {
        public Vector3D center;
        public double radius;

        public Sphere() {
            center = new Vector3D(0, 0, 0);
        }

        @Override
        public Optional<Vector3D> intersect(Vector3D origin, Vector3D ray) {
            // Use code for book, optimize later.
            // double a = 1;
            double b =
                    2 * (ray.x * (origin.x - center.x) + ray.y * (origin.y - center.y) + ray.z * (origin.z - center.z));
            double c = (origin.x - center.x) * (origin.x - center.x) + (origin.y - center.y) * (origin.y - center.y) +
                    (origin.z - center.z) * (origin.z - center.z) - (radius * radius);

            double disc = b * b - 4 * c;
            if (disc < 0) {
                return Optional.empty();
            }

            double t0 = (-b - Math.sqrt(disc)) / 2;
            double t1 = (-b + Math.sqrt(disc)) / 2;

            double t = Math.min(t0, t1);
            // Intersection point is...
            Vector3D intersection = new Vector3D(origin.x + ray.x * t, origin.z + ray.z * t, origin.z + ray.z * t);

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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Vector3D getCamera() {
        return camera;
    }

    public void setCamera(Vector3D camera) {
        this.camera = camera;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public List<SceneObject> getObjects() {
        return objects;
    }

    public void setObjects(List<SceneObject> objects) {
        this.objects = objects;
    }
}
