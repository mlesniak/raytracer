package com.mlesniak.raytracer;

import com.mlesniak.raytracer.math.Raytracer;
import com.mlesniak.raytracer.scene.Scene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Application entry point.
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");
        Scene scene = readScene(args);
        BufferedImage image = new Raytracer(scene).raytrace();
        writeImage(scene, image);
        LOG.info("Application finished");
    }

    private static Scene readScene(String[] args) throws IOException {
        if (args.length < 1) {
            LOG.error("No filename given. Aborting.");
            System.exit(1);
        }
        return Scene.readScene(args[0]);
    }

    private static void writeImage(Scene scene, BufferedImage image) throws IOException {
        final String pathname = scene.getFilename();
        ImageIO.write(image, "png", new File(pathname));
        LOG.info("Wrote image to file {}", pathname);
    }
}
