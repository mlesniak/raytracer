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
 * Implementation of the raytracing algorithm.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Raytracer {
    private static final Logger LOG = LoggerFactory.getLogger(Raytracer.class);
    private final SceneValues sceneValues;

    private Scene scene;

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

    /**
     * Initializes a new raytracer for a given scene.
     *
     * @param scene the scene to raytrace.
     */
    public Raytracer(Scene scene) {
        this.scene = scene;

        sceneValues = new SceneValues();
    }

    public BufferedImage raytrace() throws InterruptedException {
        // Parallelization support.
        int cores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(cores);
        LOG.info("Initialized executor service with {} cores", cores);

        Stopwatch.start("raytrace");

        // Each available threads computes a single line.
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

        // Wait (foreveR) until all threads are finished.
        try {
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            LOG.error("We waited {} days. This should never happen.", Long.MAX_VALUE);
            throw e;
        }

        // Create image from raw pixels.
        BufferedImage image = createBufferedImage(pixels);

        long duration = Stopwatch.stop("raytrace");
        showStatistics(duration);

        return image;
    }

    /**
     * Create a Java BufferedImage from raw pixel values.
     *
     * @param pixels array with RGB pixel values.
     * @return an Image
     */
    private BufferedImage createBufferedImage(int[] pixels) {
        ColorModel colorModel = DirectColorModel.getRGBdefault();
        SampleModel sampleModel = colorModel.createCompatibleSampleModel(scene.getWidth(), scene.getHeight());
        DataBuffer buffer = new DataBufferInt(pixels, scene.getWidth() * scene.getHeight());
        WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
        return new BufferedImage(colorModel, raster, false, null);
    }

    /**
     * Compute the color for the given pixel in the scene.
     *
     * @param scene scene description
     * @param x     x-coordinate in the image
     * @param y     y-coordinate in the image
     * @return pixel color in compressed RGBA format.
     */
    private int computePixel(Scene scene, int x, int y) {
        // Compute position on the viewplane.
        Vector3D xShift = sceneValues.right.scale(x * sceneValues.pixelWidth - sceneValues.halfWidth);
        Vector3D yShift = sceneValues.up.scale(y * sceneValues.pixelHeight - sceneValues.halfHeight);

        // Compute ray from eye to position on viewplane.
        Vector3D ray = sceneValues.eyeRay.plus(xShift).plus(yShift).normalize();

        // Check ray against all objects in the scene.
        int color = toRGBA(0, 0, 0, 0xFF);
        double minimalDistance = Double.MAX_VALUE;
        for (SceneObject sceneObject : scene.getObjects()) {
            Optional<Vector3D> intersectObject = sceneObject.computeIntersection(scene.getCamera(), ray);
            if (!intersectObject.isPresent()) {
                // No collision. Use background color.
                continue;
            }

            // Compute distance to camera to check if this object is nearer than the one already found.
            Vector3D intersection = intersectObject.get();
            double objectDistance = scene.getCamera().distance(intersection);
            if (objectDistance >= minimalDistance) {
                // We found an object which is nearer to the camera, hence its pixel color is more relevant. Ignore
                // the current object.
                continue;
            }
            minimalDistance = objectDistance;

            // Quick hack to have something working: Check if the intersection has a visible path to the
            // light source. If not, use shadow color. This approach needs to be refactored for performance
            // (move code up) and generality (more light sources):
            // We only have one light source, use this.
            Vector3D light = scene.getLights().get(0);
            Vector3D raytoLight = intersection.path(light).normalize();
            Optional<Integer> shadowColor = checkLightIntersection(scene, sceneObject, intersection, raytoLight);
            if (shadowColor.isPresent()) {
                color = shadowColor.get();
                continue;
            }

            // Point does not lie in the shadow of another object. Compute color given angle to light source.
            color = computeColor(sceneObject, intersection, light);
        }

        return color;
    }

    /**
     * Compute color of the pixel based on the angle between light ray and intersection normal.
     *
     * @param sceneObject  intersected object
     * @param intersection intersection point
     * @param light        light source
     * @return color of the pixel
     */
    private int computeColor(SceneObject sceneObject, Vector3D intersection, Vector3D light) {
        int color;
        Vector3D n = sceneObject.computeNormal(intersection);
        Vector3D path = intersection.path(light).normalize();
        double factor = n.dot(path);
        color = sceneObject.getColor();
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        // Diffuse and ambient coefficient.
        double kd = 0.9;
        double ka = 0.2;

        r = (int) (kd * factor * r + ka * r);
        g = (int) (kd * factor * g + ka * g);
        b = (int) (kd * factor * b + ka * b);

        color = toRGBA(r, g, b, 0xFF);
        return color;
    }

    // Beginning of refactoring for phong shading.
    private Optional<Integer> checkLightIntersection(Scene scene, SceneObject object, Vector3D intersection,
                                                     Vector3D raytoLight) {
        for (SceneObject shadowObject : scene.getObjects()) {
            if (shadowObject == object) {
                continue;
            }
            Optional<Vector3D> lightIntersection = shadowObject.computeIntersection(intersection, raytoLight);
            if (lightIntersection.isPresent()) {
                return Optional.of(toRGBA(0, 0, 0, 0xFF));
            }
        }

        return Optional.empty();
    }

    /**
     * Compute the single-int RGBA value from its single parts.
     *
     * @param r red
     * @param g green
     * @param b blue
     * @param a alpha
     * @return single integer RGBA value
     */
    private int toRGBA(int r, int g, int b, int a) {
        return a << 24 | fixRGBValue(r) << 16 | fixRGBValue(g) << 8 | fixRGBValue(b);
    }

    /**
     * Corrects an integer to be in the range between 0 and 255 such that it can be used as a
     * color in an RGBA value.
     *
     * @param value value to check
     * @return correct value
     */
    private int fixRGBValue(int value) {
        if (value > 255) {
            value = 255;
        }
        if (value < 0) {
            value = 0;
        }
        return value;
    }

    private void showStatistics(long duration) {
        long pixels = (long) scene.getWidth() * scene.getHeight();
        long pixelPerMs = pixels / duration;
        LOG.info("pixel={}, duration={}, pixel per ms = {}, pixel per sec = {}", pixels, duration, pixelPerMs,
                NumberFormat.getIntegerInstance().format(pixelPerMs * 1000));
    }
}
