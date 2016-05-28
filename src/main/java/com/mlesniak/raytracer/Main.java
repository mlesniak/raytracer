package com.mlesniak.raytracer;

import com.mlesniak.raytracer.math.Raytracer;
import com.mlesniak.raytracer.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Application entry point.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public final class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private Main() {
        // Empty.
    }

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");
        Optional<Scene> scene = readScene(args);
        if (scene.isPresent()) {
            Scene s = scene.get();
            BufferedImage image = new Raytracer(s).raytrace();
            writeImage(s, image);
        }
        LOG.info("Application finished");
    }

    private static Optional<Scene> readScene(String[] args) throws IOException {
        if (args.length < 1) {
            LOG.error("No filename given. Aborting.");
            return Optional.empty();
        }
        return Optional.of(Scene.readScene(args[0]));
    }

    private static void writeImage(Scene scene, BufferedImage image) throws IOException {
        final String pathname = scene.getFilename();
        ImageIO.write(image, "png", new File(pathname));
        LOG.info("Wrote image to file {}", pathname);
    }
}
