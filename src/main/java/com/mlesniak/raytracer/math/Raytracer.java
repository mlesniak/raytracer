package com.mlesniak.raytracer.math;

import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.scene.SceneObject;
import com.mlesniak.raytracer.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Raytracer {
    private static final Logger LOG = LoggerFactory.getLogger(Raytracer.class);

    private Scene scene;
    private final ExecutorService executorService;

    public Raytracer(Scene scene) {
        this.scene = scene;
        int cores = Runtime.getRuntime().availableProcessors();
        LOG.info("Initialized executor service with {} cores", cores);
        executorService = Executors.newFixedThreadPool(cores);
    }

    public BufferedImage raytrace() {
        Stopwatch.start("raytrace");
        BufferedImage image = new BufferedImage(scene.getWidth(), scene.getHeight(), BufferedImage.TYPE_INT_RGB);

        // Parallelize over lines.
        for (int y = 0; y < scene.getHeight(); y++) {
            final int line = y;
            executorService.execute((Runnable) () -> {
                for (int x = 0; x < scene.getWidth(); x++) {
                    int rgb = computePixel(scene, x, line);
                    // Image and mathematical coordinate systems are different,
                    // hence we have to flip w.r.t to the y-axis.
                    image.setRGB(x, scene.getHeight() - line - 1, rgb);
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

        long duration = Stopwatch.stop("raytrace");
        showStatistics(duration);

        return image;
    }

    private void showStatistics(long duration) {
        long pixels = scene.getWidth() * scene.getHeight();
        long pixelPerMs = pixels / duration;
        LOG.info("pixel={}, duration={}, pixel per ms = {}", pixels, duration, pixelPerMs);
    }

    private int computePixel(Scene scene, int x, int y) {
//        LOG.info("x={}, y={}", x, y);

        Vector3D camera = scene.getCamera();

        // Current reference http://www.macwright.org/literate-raytracer/

        // We need this in radians.
        double fovRad = Math.PI * (scene.getFov() / 2) / 180; // TODO ML Understand formula
        double ratio = (double) scene.getHeight() / scene.getWidth();
        double halfWidth = Math.tan(fovRad / 2);
        double halfHeight = halfWidth * ratio;
        double cameraWidth = halfWidth * 2;
        double cameraHeight = halfHeight * 2;
        double pixelWidth = cameraWidth / (scene.getWidth() - 1);
        double pixelHeight = cameraHeight / (scene.getHeight() - 1);

        Vector3D eyeVector = camera.path(scene.getLookAt()).normalize();
//        LOG.info("  eyeVector={}", eyeVector);

        Vector3D vup = new Vector3D(0, 1, 0); //.normalize();
        Vector3D right = eyeVector.crossProduct(vup);
//        LOG.info("  right={}", right);
        Vector3D up = right.crossProduct(eyeVector);
//        LOG.info("  up={}", up);

        Vector3D xcomp = right.scale(((double) x) * pixelWidth - halfWidth);
//        LOG.info("  xcomp={}", xcomp);
        Vector3D ycomp = up.scale(((double) y) * pixelHeight - halfHeight);
//        LOG.info("  ycomp={}", ycomp);

        Vector3D ray = eyeVector.add(xcomp).add(ycomp).normalize();
//        LOG.info("  ray={}", ray);

        // Check ray against all objects in the scene.
        int color = 0;
        for (SceneObject object : scene.getObjects()) {
            Optional<Vector3D> oi = object.intersect(scene.getCamera(), ray);
            if (oi.isPresent()) {
                color = object.getColor();
            }
        }

        return color;
    }

//    private int toRGB(int r, int g, int b) {
//        return r << 16 | g << 8 | b;
//    }
}
