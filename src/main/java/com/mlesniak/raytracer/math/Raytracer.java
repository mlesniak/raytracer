package com.mlesniak.raytracer.math;

import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.scene.SceneObject;
import com.mlesniak.raytracer.scene.Sphere;
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
        cores = 1;
        LOG.info("Initialized executor service with {} cores", cores);
        executorService = Executors.newFixedThreadPool(cores);
    }

    public BufferedImage raytrace() {
        long start = System.currentTimeMillis();
        BufferedImage image = new BufferedImage(scene.getWidth(), scene.getHeight(), BufferedImage.TYPE_INT_RGB);

        // Parallelize over lines.
        for (int y = 0; y < scene.getHeight(); y++) {
            final int line = y;
            executorService.submit((Runnable) () -> {
                for (int x = 0; x < scene.getWidth(); x++) {
//                    LOG.info("Processing x={}, y={}", x, line);
                    int rgb = computePixel(scene, x, line);
                    // Image and mathematical coordinate systems are different, hence we have to flip w.r.t to the
                    // y-axis.
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
        long end = System.currentTimeMillis();
        long duration = end - start;
        long pixels = scene.getWidth() * scene.getHeight();
        long pixelPerMs = pixels / duration;
        LOG.info("pixel={}, duration={}, pixel per ms = {}", pixels, duration, pixelPerMs);

        return image;
    }

    private int computePixel(Scene scene, int x, int y) {
        // TODO ML Predefine constant value object
        double aspectRatio = (double) scene.getHeight() / scene.getWidth();
        double fovx = Math.PI / 4;
        double fovy = aspectRatio * fovx;


        // Later we should factor this out into a "precalculated values" - object.
        double stepX = (scene.getView().getUpperRight().getX() - scene.getView().getLowerLeft().getX()) /
                scene.getWidth();
        double stepY = (scene.getView().getUpperRight().getY() - scene.getView().getLowerLeft().getY()) /
                scene.getHeight();
//        LOG.info("  stepX={}, stepY={}", stepX, stepY);

        // Determine point on view plane.
        double xx = scene.getView().getLowerLeft().x + x * stepX;
        double yy = scene.getView().getLowerLeft().y + y * stepY;

        Vector3D pView = new Vector3D(
                xx * Math.tan(fovx),
                yy * Math.tan(fovy),
                scene.getView().getLowerLeft().z);
//        LOG.info("  pView={}", pView);

        // Determine normalized ray from camera.
        Vector3D camera = scene.getCamera();
        Vector3D ray = new Vector3D(
                (pView.x - camera.x),
                (pView.y - camera.y),
                (pView.z - camera.z));
        ray = ray.normalize();

        // Check ray against all objects in the scene.
        int color = 0;
        for (SceneObject object : scene.getObjects()) {
            Optional<Vector3D> oi = object.intersect(scene.getCamera(), ray);
            if (oi.isPresent()) {
                Vector3D i = oi.get();
                // We only have sphere for now.
                Sphere s = (Sphere) object;
                // Determine normal vector.
                // TODO ML Must be done on a per-object basis?
                Vector3D norm = new Vector3D((i.x - s.center.x) / s.radius, (i.y - s.center.y) / s.radius, (i.y -
                        s.center.y) / s.radius).normalize();
                // Determine angle to camera for poor man's shading.
                Vector3D cam = camera.copy().normalize();
                double angle = Math.acos(cam.dot(norm));
//                color = toRGB((int) ((double) 0xFF * angle * 10), 0, 0);
                color = s.getColor();
            }
        }

        return color;
    }

    private int toRGB(int r, int g, int b) {
        return r << 16 | g << 8 | b;
    }
}
