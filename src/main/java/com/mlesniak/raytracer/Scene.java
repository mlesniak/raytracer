package com.mlesniak.raytracer;

/**
 * Describe a full scene.
 * <p>
 * We use YAML serialization to store a scene as a readable and editable file.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Scene {
    private String filename;
    private Vector3D camera;

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

    @Override
    public String toString() {
        return "Scene{" +
                "filename='" + filename + '\'' +
                ", camera=" + camera +
                '}';
    }
}
