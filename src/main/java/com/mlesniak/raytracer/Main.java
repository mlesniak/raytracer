package com.mlesniak.raytracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

/**
 * Application entry point.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");

        // Draw single pixels.
        final int width = 320;
        final int height = 200;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int r = x % 0xFF;
                int g = y % 0xFF;
                int b = 30 % 0xFF;
                int rgb = r << 16 | g << 8 | b;
                image.setRGB(x, y, rgb);
            }
        }

        // Write file.
        final String pathname = "image.png";
        ImageIO.write(image, "png", new java.io.File(pathname));
        LOG.info("Wrote image to file {}", pathname);
    }
}
