package com.mlesniak.raytracer;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

/**
 * Application entry point.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");
        Scene scene = readScene();

        // Draw single pixels.
        long start = System.currentTimeMillis();
        BufferedImage image = new BufferedImage(scene.getWidth(), scene.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < scene.getHeight(); y++) {
            for (int x = 0; x < scene.getWidth(); x++) {
                int rgb = computePixel(scene, x, y);
                image.setRGB(x, scene.getHeight() - y - 1, rgb);
            }
        }
        long end = System.currentTimeMillis();
        long duration = end - start;
        long pixels = scene.getWidth() * scene.getHeight();
        long pixelPerMs = pixels / duration;
        LOG.info("pixel={}, duration={}, pixel per ms = {}", pixels, duration, pixelPerMs);

        // Write file.
        final String pathname = scene.getFilename();
        ImageIO.write(image, "png", new java.io.File(pathname));
        LOG.info("Wrote image to file {}", pathname);
        LOG.info("Application finished");

        yamlPlayground(scene);
    }

    private static int computePixel(Scene scene, int x, int y) {
        // Later we should factor this out into a "precalculated values" - object.
        double stepX = (scene.getView().getUpperRight().getX() - scene.getView().getLowerLeft().getX()) /
                scene.getWidth();
        double stepY = (scene.getView().getUpperRight().getY() - scene.getView().getLowerLeft().getY()) /
                scene.getHeight();

        // Determine point on view plane.
        Vector3D pView = new Vector3D(scene.getView().getLowerLeft().x + x * stepX, scene.getView().getLowerLeft().y
                + y * stepY, scene.getView().getLowerLeft().z);
        // LOG.info("pView={}", pView);

        // Determine normalized ray from camera.
        Vector3D camera = scene.getCamera();
        Vector3D ray = new Vector3D(camera.x - pView.x, camera.y - pView.y, camera.z - pView.z).normalize();

        // Check ray against all objects in the scene.
        int color = 0;
        for (Scene.SceneObject object : scene.getObjects()) {
            if (object.intersect(scene.getCamera(), ray).isPresent()) {
                color = object.getColor();
            }
        }

        return color;
    }

//    private static int toRGB(int r, int g, int b) {
//        return r << 16 | g << 8 | b;
//    }

    private static void yamlPlayground(Scene scene) throws YamlException {
        if (true) {
            // Exit point if we do not play around with scenes. Prevents findbug error about unused method.
            return;
        }

        // Playground for examining new yaml struture elements in a scene.
        Scene.Sphere sphere = new Scene.Sphere();
        sphere.setCenter(new Vector3D(5, 5, 1));
        sphere.setRadius(1.0);
        scene.setObjects(new LinkedList<>());
        scene.getObjects().add(sphere);

        StringWriter sw = new StringWriter();
        YamlWriter writer = new YamlWriter(sw);
        writer.write(scene);
        writer.close();
        LOG.debug("Scene={}", sw.toString());
    }

    private static Scene readScene() throws YamlException, UnsupportedEncodingException {
        InputStream stream = Main.class.getResourceAsStream("/scene/default.yaml");
        InputStreamReader streamReader = new InputStreamReader(stream, "UTF-8");
        YamlReader reader = new YamlReader(streamReader);
        return reader.read(Scene.class);
    }
}
