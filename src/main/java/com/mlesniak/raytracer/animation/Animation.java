package com.mlesniak.raytracer.animation;

import com.mlesniak.raytracer.math.Raytracer;
import com.mlesniak.raytracer.scene.Scene;
import com.mlesniak.raytracer.util.GifWriter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.stream.FileImageOutputStream;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Control animation handling..
 *
 * @author Michael Lesniak (mlesniak@micromata.de)
 */
public class Animation {
    private static final Logger LOG = LoggerFactory.getLogger(Animation.class);

    private Scene scene;

    public Animation(Scene scene) {
        this.scene = scene;
    }

    public static boolean isAnimated(Scene scene) {
        return scene.getAnimation() != null;
    }

    public void animate() throws Exception {
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
}
