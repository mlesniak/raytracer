package com.mlesniak.raytracer;

import com.mlesniak.raytracer.math.Raytracer;
import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.util.GifWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
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

            // Check if we have animation settings.
            if (s.getAnimation() == null) {
                BufferedImage image = new Raytracer(s).raytrace();
                writeSingleImage(s, image);
            } else {
                animate(s);
            }
        }
        LOG.info("Application finished");
    }

    private static void animate(Scene scene) throws IOException, ScriptException, InterruptedException {
        LOG.info("Animation starting");
        Scene.AnimationSettings animation = scene.getAnimation();
        int ticks = animation.getTicks();

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("scene", scene);
        bindings.put("ticks", ticks);
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
        String code = FileUtils.readFileToString(new File(animation.getFile()));

        BufferedImage[] images = new BufferedImage[ticks];
        for (int tick = 0; tick < ticks; tick++) {
            // Modify scene in javascript.
            bindings.put("tick", tick);
            engine.eval(code, bindings);

            // Create and store image.
            BufferedImage image = new Raytracer(scene).raytrace();
            images[tick] = image;
        }

        // Write gif file.
        FileImageOutputStream output = new FileImageOutputStream(new File(scene.getFilename() + ".gif"));
        int time = animation.getDuration() / ticks;
        GifWriter gw = new GifWriter(output, images[0].getType(), time, animation.getLoop());
        for (BufferedImage image : images) {
            gw.writeToSequence(image);
        }
        gw.close();
        output.close();
        LOG.info("Animation written");
    }

    private static Optional<Scene> readScene(String[] args) throws IOException {
        if (args.length < 1) {
            LOG.error("No filename given. Aborting.");
            return Optional.empty();
        }
        return Optional.of(Scene.readScene(args[0]));
    }

    private static void writeSingleImage(Scene scene, BufferedImage image) throws IOException {
        final String pathname = scene.getFilename();
        ImageIO.write(image, "png", new File(pathname));
        LOG.info("Wrote image to file {}", pathname);
    }
}
