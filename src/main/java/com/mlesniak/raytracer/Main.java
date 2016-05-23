package com.mlesniak.raytracer;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.mlesniak.raytracer.math.Vector3D;
import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.scene.SceneObject;
import com.mlesniak.raytracer.scene.Sphere;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 * Application entry point.
 * <p>
 * // TODO ML Shading
 * // TODO ML Refactoring!
 * // TODO ML Lights / Shadows
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");
        if (args.length < 1) {
            LOG.error("No filename given. Aborting.");
            return;
        }
        Scene scene = readScene(args[0]);

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
        for (SceneObject object : scene.getObjects()) {
            Optional<Vector3D> oi = object.intersect(scene.getCamera(), ray);
            if (oi.isPresent()) {
                Vector3D i = oi.get();
                // We only have sphere for now.
                Sphere s = (Sphere) object;
                // Determine normal vector.
                Vector3D norm = new Vector3D((i.x - s.center.x) / s.radius, (i.y - s.center.y) / s.radius, (i.y -
                        s.center.y) / s.radius).normalize();
                // Determine angle to camera for poor man's shading.
                Vector3D cam = camera.copy().normalize();
                double angle = Math.acos(cam.dot(norm));
                color = toRGB((int) ((double) 0xFF * angle * 10), 0, 0);
            }
        }

        return color;
    }

    private static int toRGB(int r, int g, int b) {
        return r << 16 | g << 8 | b;
    }

    private static Scene readScene(String filename) throws IOException {
        FileInputStream stream = FileUtils.openInputStream(new File(filename));
        InputStreamReader streamReader = new InputStreamReader(stream, "UTF-8");
        YamlReader reader = new YamlReader(streamReader);
        return reader.read(Scene.class);
    }
}
