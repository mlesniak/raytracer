package com.mlesniak.raytracer;

import java.util.List;

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
     * Generalized scene object.
     */
    public interface SceneObject {
        /**
         * Check for intersection.
         *
         * @param ray ray to check against intersection.
         * @return true if intersects
         */
        boolean intersect(Vector3D ray);
    }

    /**
     * Sphere description.
     */
    public static class Sphere implements SceneObject {
        public Vector3D center;
        public double radius;

        @Override
        public boolean intersect(Vector3D ray) {
            return Math.random() > 0.5;
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
