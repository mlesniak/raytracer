package com.mlesniak.raytracer;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Application entry point.
 */
public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Application starting");
        Map<String, String> config = readScene();

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
        final String pathname = config.get("imagePath");
        ImageIO.write(image, "png", new java.io.File(pathname));
        LOG.info("Wrote image to file {}", pathname);
        LOG.info("Application finished");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> readScene() throws YamlException, UnsupportedEncodingException {
        InputStream stream = Main.class.getResourceAsStream("/scene/default.yaml");
        InputStreamReader streamReader = new InputStreamReader(stream, "UTF-8");
        YamlReader reader = new YamlReader(streamReader);
        Map<String, String> config = (Map<String, String>) reader.read();
        LOG.debug("Read configuration {}", config);
        return config;
    }
}
