package com.mlesniak.raytracer.math;

import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.scene.SceneObject;
import com.mlesniak.raytracer.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Raytracer {
    private static final Logger LOG = LoggerFactory.getLogger(Raytracer.class);
    private final SceneValues sceneValues;

    private Scene scene;
    private final ExecutorService executorService;

    /**
     * Store precomputed scene values which are relevant for each pixel.
     */
    private class SceneValues {
        // Compute correct pixel and screen dimensions to compute the viewplane we are looking at.
        final double fovRad = Math.PI * (scene.getFov() / 2) / 180;
        final double ratio = (double) scene.getHeight() / scene.getWidth();
        // We divide by 2 since we define the full FoV in the scene definition (which is more intuitive).
        final double halfWidth = Math.tan(fovRad / 2);
        final double halfHeight = halfWidth * ratio;
        final double cameraWidth = halfWidth * 2;
        final double cameraHeight = halfHeight * 2;
        final double pixelWidth = cameraWidth / (scene.getWidth() - 1);
        final double pixelHeight = cameraHeight / (scene.getHeight() - 1);

        // Predefined static vectors.
        final Vector3D cameraUp = new Vector3D(0, 1, 0);
        final Vector3D eyeRay = scene.getCamera().path(scene.getLookAt()).normalize();
        final Vector3D right = eyeRay.crossProduct(cameraUp);
        final Vector3D up = right.crossProduct(eyeRay);
    }

    public Raytracer(Scene scene) {
        this.scene = scene;
        int cores = Runtime.getRuntime().availableProcessors();
        LOG.info("Initialized executor service with {} cores", cores);
        executorService = Executors.newFixedThreadPool(cores);
        sceneValues = new SceneValues();
    }

    public BufferedImage raytrace() {
        Stopwatch.start("raytrace");

        // Parallelize over lines.
        int[][] pixels = new int[scene.getWidth()][scene.getHeight()];
        for (int y = 0; y < scene.getHeight(); y++) {
            final int line = y;
            executorService.execute(() -> {
                for (int x = 0; x < scene.getWidth(); x++) {
                    int rgb = computePixel(scene, x, line);
                    // Image and mathematical coordinate systems are different,
                    // hence we have to flip w.r.t to the y-axis.
                    pixels[x][scene.getHeight() - line - 1] = rgb;
                }
            });
        }

        // Wait until all threads are finished.
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOG.error("We waited {} days. This should never happen.", Long.MAX_VALUE);
            throw new IllegalStateException("Timeout while awaiting computation");
        }

        // Copy raw data to java image.
        BufferedImage image = new BufferedImage(scene.getWidth(), scene.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < scene.getHeight(); y++) {
            for (int x = 0; x < scene.getWidth(); x++) {
                image.setRGB(x, y, pixels[x][y]);
            }
        }

        long duration = Stopwatch.stop("raytrace");
        showStatistics(duration);

        return image;
    }

    private int computePixel(Scene scene, int x, int y) {
        // Compute position on the viewplane.
        Vector3D xShift = sceneValues.right.scale(x * sceneValues.pixelWidth - sceneValues.halfWidth);
        Vector3D yShift = sceneValues.up.scale(y * sceneValues.pixelHeight - sceneValues.halfHeight);

        // Compute ray from eye to position on viewplane.
        Vector3D ray = sceneValues.eyeRay.plus(xShift).plus(yShift).normalize();

        // Check ray against all objects in the scene.
        int color = 0;
        double minimalDistance = Double.MAX_VALUE;
        for (SceneObject object : scene.getObjects()) {
            Optional<Vector3D> oi = object.intersect(scene.getCamera(), ray);
            if (oi.isPresent()) {
                // Check length to camera.
                Vector3D intersection = oi.get();
                double objectDist = scene.getCamera().distance(intersection);
                if (objectDist < minimalDistance) {
                    minimalDistance = objectDist;

                    // Compute angle between ray and normal to compute color smoothing factor.
                    Vector3D n = object.normal(intersection);
                    // We only have one light source, use this.
                    Vector3D light = scene.getLights().get(0);
                    Vector3D path = intersection.path(light).normalize();
                    double factor = n.dot(path);
                    color = object.getColor();
                    int r = (color >> 16) & 0xFF;
                    int g = (color >> 8) & 0xFF;
                    int b = color & 0xFF;

                    // Diffuse and ambient coefficient.
                    double kd = 0.9;
                    double ka = 0.2;

                    r = (int) (kd * factor * r + ka * r);
                    g = (int) (kd * factor * g + ka * g);
                    b = (int) (kd * factor * b + ka * b);

                    color = toRGB(r, g, b);

                    // Quick hack to have something working: Check if the intersection has a visible path to the
                    // light source. If not, use shadow color. This approach needs to be refactored for performance
                    // (move code up) and generality (more light sources):
                    Vector3D raytoLight = intersection.path(light).normalize();
                    for (SceneObject shadowObject : scene.getObjects()) {
                        if (shadowObject == object) {
                            continue;
                        }
                        Optional<Vector3D> lightIntersection = shadowObject.intersect(intersection, raytoLight);
                        if (lightIntersection.isPresent()) {
                            color = 0;
                        }
                    }
                }
            }
        }

        return color;
    }

    private int toRGB(int r, int g, int b) {
        if (r > 255) {
            r = 255;
        }
        if (r < 0) {
            r = 0;
        }
        if (g > 255) {
            g = 255;
        }
        if (g < 0) {
            g = 0;
        }
        if (b > 255) {
            b = 255;
        }
        if (b < 0) {
            b = 0;
        }

        return r << 16 | g << 8 | b;
    }

    private void showStatistics(long duration) {
        long pixels = scene.getWidth() * scene.getHeight();
        long pixelPerMs = pixels / duration;
        LOG.info("pixel={}, duration={}, pixel per ms = {}, pixel per sec = {}", pixels, duration, pixelPerMs,
                NumberFormat.getIntegerInstance().format(pixelPerMs * 1000));
    }
}
