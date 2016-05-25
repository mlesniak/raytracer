package com.mlesniak.raytracer.scene;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.mlesniak.raytracer.math.Vector3D;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private Vector3D lookAt;
    private double fov;

    private List<SceneObject> objects;

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

    public Vector3D getLookAt() {
        return lookAt;
    }

    public void setLookAt(Vector3D lookAt) {
        this.lookAt = lookAt;
    }

    public double getFov() {
        return fov;
    }

    public void setFov(double fov) {
        this.fov = fov;
    }

    public static Scene readScene(String filename) throws IOException {
        FileInputStream stream = FileUtils.openInputStream(new File(filename));
        InputStreamReader streamReader = new InputStreamReader(stream, "UTF-8");
        YamlReader reader = new YamlReader(streamReader);
        return reader.read(Scene.class);
    }
}
