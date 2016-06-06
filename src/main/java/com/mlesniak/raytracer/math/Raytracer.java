package com.mlesniak.raytracer.math;

import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.scene.SceneObject;
import com.mlesniak.raytracer.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
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
        double fovRad = Math.PI * (scene.getFov() / 2) / 180;
        double ratio = (double) scene.getHeight() / scene.getWidth();
        // We divide by 2 since we define the full FoV in the scene definition (which is more intuitive).
        double halfWidth = Math.tan(fovRad / 2);
        double halfHeight = halfWidth * ratio;
        double cameraWidth = halfWidth * 2;
        double cameraHeight = halfHeight * 2;
        double pixelWidth = cameraWidth / (scene.getWidth() - 1);
        double pixelHeight = cameraHeight / (scene.getHeight() - 1);

        // Predefined static vectors.
        Vector3D cameraUp = new Vector3D(0, 1, 0);
        Vector3D eyeRay = scene.getCamera().path(scene.getLookAt()).normalize();
        Vector3D right = eyeRay.crossProduct(cameraUp);
        Vector3D up = right.crossProduct(eyeRay);
    }

    public Raytracer(Scene scene) {
        this.scene = scene;
        int cores = Runtime.getRuntime().availableProcessors();
        LOG.info("Initialized executor service with {} cores", cores);
        executorService = Executors.newFixedThreadPool(cores);
        sceneValues = new SceneValues();
    }

    public BufferedImage raytrace() throws InterruptedException {
        Stopwatch.start("raytrace");

        // Parallelize over lines.
        int[] pixels = new int[scene.getHeight() * scene.getWidth()];
        for (int y = 0; y < scene.getHeight(); y++) {
            final int line = y;
            executorService.execute(() -> {
                for (int x = 0; x < scene.getWidth(); x++) {
                    int rgb = computePixel(scene, x, line);
                    // Image and mathematical coordinate systems are different,
                    // hence we have to flip w.r.t the y-axis.
                    pixels[(scene.getHeight() - line - 1) * scene.getWidth() + x] = rgb;
                }
            });
        }

        // Wait until all threads are finished.
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOG.error("We waited {} days. This should never happen.", Long.MAX_VALUE);
            throw e;
        }

        // Create image from raw pixels.
        ColorModel colorModel = DirectColorModel.getRGBdefault();
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(scene.getWidth(), scene.getHeight());
        DataBuffer buffer = new DataBufferInt(pixels, scene.getWidth() * scene.getHeight());
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
        BufferedImage image = new BufferedImage(colorModel, raster, false, null);

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
        int color = 0xFF << 24;
        double minimalDistance = Double.MAX_VALUE;
        for (SceneObject object : scene.getObjects()) {
            Optional<Vector3D> oi = object.computeIntersection(scene.getCamera(), ray);
            if (oi.isPresent()) {
                // Check length to camera.
                Vector3D intersection = oi.get();
                double objectDist = scene.getCamera().distance(intersection);
                if (objectDist < minimalDistance) {
                    minimalDistance = objectDist;

                    // Compute angle between ray and normal to compute color smoothing factor.
                    Vector3D n = object.computeNormal(intersection);
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
                    color = checkLightIntersection(scene, color, object, intersection, raytoLight);
                }
            }
        }

        return color;
    }

    // Beginning of refactoring for phong shading.
    private int checkLightIntersection(Scene scene, int color, SceneObject object, Vector3D intersection, Vector3D
            raytoLight) {
        int updatedColor = color;
        for (SceneObject shadowObject : scene.getObjects()) {
            if (shadowObject == object) {
                continue;
            }
            Optional<Vector3D> lightIntersection =
                    shadowObject.computeIntersection(intersection, raytoLight);
            if (lightIntersection.isPresent()) {
                updatedColor = 0xFF << 24;
            }
        }
        return updatedColor;
    }

    private int toRGB(int r, int g, int b) {
        int rFixed = r;
        int gFixed = g;
        int bFixed = b;
        if (rFixed > 255) {
            rFixed = 255;
        }
        if (rFixed < 0) {
            rFixed = 0;
        }
        if (gFixed > 255) {
            gFixed = 255;
        }
        if (gFixed < 0) {
            gFixed = 0;
        }
        if (bFixed > 255) {
            bFixed = 255;
        }
        if (bFixed < 0) {
            bFixed = 0;
        }

        // No transparency.
        return 0xFF << 24 | rFixed << 16 | gFixed << 8 | bFixed;
    }

    private void showStatistics(long duration) {
        long pixels = (long) scene.getWidth() * scene.getHeight();
        long pixelPerMs = pixels / duration;
        LOG.info("pixel={}, duration={}, pixel per ms = {}, pixel per sec = {}", pixels, duration, pixelPerMs,
                NumberFormat.getIntegerInstance().format(pixelPerMs * 1000));
    }
}
